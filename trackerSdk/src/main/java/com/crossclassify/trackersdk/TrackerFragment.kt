package com.crossclassify.trackersdk

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.crossclassify.trackersdk.interfaces.TrackerActions
import com.crossclassify.trackersdk.model.FieldMetaData
import com.crossclassify.trackersdk.model.FormMetaData
import com.crossclassify.trackersdk.service.config.TrackerSdkApplication
import com.crossclassify.trackersdk.service.repository.FormsRepository
import com.crossclassify.trackersdk.utils.objects.RandomStringGenerator
import com.crossclassify.trackersdk.utils.objects.UiHandler
import com.crossclassify.trackersdk.utils.objects.Values
import org.matomo.sdk.extra.TrackHelper
import java.util.*

abstract class TrackerFragment : Fragment(),
    TrackerActions {
    private var changeStartTime = false
    abstract fun getFormName(): String
    private var faId = getFormName()

    private val PREF_KEY_TRACKER_VISITORID = "tracker.visitorid"
    private val PREF_KEY_TRACKER_FINGERPRINT = "tracker.fingerprint"

    private lateinit var bitaApplicationContext: Context

    private var startTime: Long = System.currentTimeMillis()
    private var submitTime: Long = 0

    private var firstTimeSeeForm = true

    // save fields metadata in form
    private val fieldMetaData = ArrayList<FieldMetaData>()
    private var formMetaData: FormMetaData? = null

    private val focusView: View by lazy {
        View(requireContext()).apply {
            isFocusable = true
            isFocusableInTouchMode = true
        }
    }

    private var firstFocusFieldTime: Long = Long.MAX_VALUE
    private var firstFocusFieldName: String? = null
    private var timeToSubmission: Long = 0

    private var lastChangingFieldTime: Long = 0
    private var lastChangingFieldName: String? = null

    private var interactionTime: Long = 0

    private var timer: Timer? = null

    // page view id
    private var pvId = RandomStringGenerator.getRandomString()

    // form view id
    private var faVid = RandomStringGenerator.getRandomString()


    private var deviceId: String? = null
    private var fingerprint:String?=null
    private var resolution: String? = null
    private var idSite = Values.SITE_ID

    private lateinit var formRepo: FormsRepository

    // get all data
    fun getMetaData(): FormMetaData? {
        return formMetaData
    }

    // create device id
    private fun getDeviceId(): String? {
        val sharedPreferences = requireContext().getSharedPreferences(
            "org.matomo.sdk_FE8DB41078DFFC3D9751687595C3B837",
            Context.MODE_PRIVATE
        )
        deviceId = sharedPreferences.getString(PREF_KEY_TRACKER_VISITORID, null)
        return deviceId

    }
    private fun getFingerPrint():String?{
        val sharedPreferences = requireContext().getSharedPreferences(
            "org.matomo.sdk_FE8DB41078DFFC3D9751687595C3B837",
            Context.MODE_PRIVATE
        )
        fingerprint = sharedPreferences.getString(PREF_KEY_TRACKER_FINGERPRINT, null)
        return fingerprint
    }

    // get device resolution
    private fun getResolution(): String {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        return "$height" + "x$width"
    }

    fun createFormMetaData(submitButtonClick: Boolean) {
        initProperty()
        // get metadata from form fields
        if (view is ViewGroup)
            checkChildren(view as ViewGroup, true, submitButtonClick)

        val resultFieldMetaData = ArrayList<FieldMetaData>()
        resultFieldMetaData.addAll(fieldMetaData)

        // if use fields in recycler view
        // get all metadata in recycler view and merge with fields out of recycler view
        getExternalMetaData()?.let { metaData ->

            for (index in metaData.indices) {
                metaData[index].firstFocusTime?.let { firstFocusTime ->
                    if (firstFocusFieldTime > firstFocusTime) {
                        firstFocusFieldTime = firstFocusTime
                        firstFocusFieldName = metaData[index].fa_fn
                    }
                }
                metaData[index].lastFocusChangeTime?.let { lastFocusTimeWithChanging ->
                    if (lastChangingFieldTime < lastFocusTimeWithChanging) {
                        lastChangingFieldTime = lastFocusTimeWithChanging
                        lastChangingFieldName = metaData[index].fa_fn
                    }
                }
                interactionTime += metaData[index].fa_fts
                if (metaData[index].changeAfterCreateMetaData) {
                    resultFieldMetaData.add(metaData[index])
                    metaData[index].changeAfterCreateMetaData = false
                }
            }
        }

//        var tts = 0L
//        for (i in resultFieldMetaData) {
//            tts += i.fa_fts
//        }

        formMetaData = FormMetaData(
            fa_vid = faVid,
            pv_id = pvId,
            fa_ef = getFirstFiledFocusName(),
            fa_lf = getLastFiledFocusName(),
            fa_ts = getInteractionTime(),
            fa_ht = getHesitationBeforeFocus(),
            fa_st = if (isFirstTime()) 1 else 0,
            fa_su = if (submitButtonClick) 1 else 0,
            fa_tts = if (submitButtonClick) getTimeToSubmission() else null,
            fa_id = faId,
            _id = deviceId,
            uid=fingerprint,
            id_site = idSite,
            fieldsMetaData = resultFieldMetaData.toList(),
            resolustion = resolution
        )
    }

    /**
     * initialize property before create metadata
     */
    private fun initProperty() {
        fieldMetaData.clear()

        firstFocusFieldName = null
        firstFocusFieldTime = Long.MAX_VALUE

        lastChangingFieldName = null
        lastChangingFieldTime = 0L

        interactionTime = 0

        formMetaData = null
    }

    /**
     * This function use for init fields when create view(getData = false)
     * and use when we create metadata(getData = true)
     */
    private fun checkChildren(
        viewGroup: ViewGroup,
        getData: Boolean = false,
        isSubmitted: Boolean = false
    ) {
        for (view in viewGroup.children) {
            when (view) {
                is TrackerEditText -> {
                    if (getData) {
                        // we check if it is the first field that has been interacted with
                        view.getFirstFocusTime()?.let { firstFocusTime ->
                            if (firstFocusFieldTime > firstFocusTime) {
                                firstFocusFieldTime = firstFocusTime
                                firstFocusFieldName = view.getFieldName()
                            }
                        }
                        // we check if it is the last field that has been interacted with
                        view.getLastFocusTimeForChange()?.let { lastFocusTimeWithChanging ->
                            if (lastChangingFieldTime < lastFocusTimeWithChanging) {
                                lastChangingFieldTime = lastFocusTimeWithChanging
                                lastChangingFieldName = view.getFieldName()
                            }
                        }
                        // total time spent on field
                        interactionTime += view.getAllFocusTime()
                        // if there is a change after taking the metadata, the metadata will be rebuilt and the metadata field will be added.
                        if (view.changeAfterCreateMetaData) {
                            view.createMetaData(isSubmitted)
                            view.getMetaData()?.let {
                                fieldMetaData.add(it)
                            }
                        }
                    } else {
                        // if we do not have Get Data, we will pass the action to the timer
                        if (view.changeAfterCreateMetaData && !changeStartTime) {
                            changeStartTime = true
                        }
                        view.setAction(this)
                    }
                }
                is TrackerRadioGroup -> {
                    if (getData) {
                        // we check if it is the first field that has been interacted with
                        view.getFirstFocusTime()?.let { firstFocusTime ->
                            if (firstFocusFieldTime > firstFocusTime) {
                                firstFocusFieldTime = firstFocusTime
                                firstFocusFieldName = view.getFieldName()
                            }
                        }
                        // we check if it is the last field that has been interacted with
                        view.getLastFocusTimeForChange()?.let { lastFocusTimeWithChanging ->
                            if (lastChangingFieldTime < lastFocusTimeWithChanging) {
                                lastChangingFieldTime = lastFocusTimeWithChanging
                                lastChangingFieldName = view.getFieldName()
                            }
                        }
                        // total time spent on field
                        interactionTime += view.getAllFocusTime()
                        // if there is a change after taking the metadata, the metadata will be rebuilt and the metadata field will be added.
                        if (view.changeAfterCreateMetaData) {
                            view.createMetaData(isSubmitted)
                            view.getMetaData()?.let {
                                fieldMetaData.add(it)
                            }
                        }

                    } else {
                        // if we do not have Get Data, we will pass the action to the timer
                        if (view.changeAfterCreateMetaData && !changeStartTime) {
                            changeStartTime = true
                        }
                        view.setAction(this)
                    }
                }
                is TrackerCheckBox -> {
                    if (getData) {
                        // we check if it is the first field that has been interacted with
                        view.getFirstFocusTime()?.let { firstFocusTime ->
                            if (firstFocusFieldTime > firstFocusTime) {
                                firstFocusFieldTime = firstFocusTime
                                firstFocusFieldName = view.getFieldName()
                            }
                        }
                        // we check if it is the last field that has been interacted with
                        view.getLastFocusTimeForChange()?.let { lastFocusTimeWithChanging ->
                            if (lastChangingFieldTime < lastFocusTimeWithChanging) {
                                lastChangingFieldTime = lastFocusTimeWithChanging
                                lastChangingFieldName = view.getFieldName()
                            }
                        }
                        // total time spent on field
                        interactionTime += view.getAllFocusTime()
                        // if there is a change after taking the metadata, the metadata will be rebuilt and the metadata field will be added.
                        if (view.changeAfterCreateMetaData) {
                            view.createMetaData(isSubmitted)
                            view.getMetaData()?.let {
                                fieldMetaData.add(it)
                            }
                        }
                    } else {
                        // if we do not have Get Data, we will pass the action to the timer
                        if (view.changeAfterCreateMetaData && !changeStartTime) {
                            changeStartTime = true
                        }
                        view.setAction(this)
                    }
                }
                is ViewGroup -> {
                    //if the current view is a viewGroup (including other views), it enters its children and checks them. Except for Recycler View
                    if (view is RecyclerView)
                        continue
                    checkChildren(view, getData, isSubmitted)
                }
            }
        }
    }

    // fa_ef
    private fun getFirstFiledFocusName() = firstFocusFieldName

    // fa_lf
    private fun getLastFiledFocusName() = lastChangingFieldName

    // fa_ts
    private fun getInteractionTime() = interactionTime

    //fa_tts
    private fun getTimeToSubmission() = timeToSubmission

    // fa_ht
    /** compares the start time of an activity with the start time of the interaction */
    private fun getHesitationBeforeFocus(): Long? {
        return if (firstFocusFieldTime == Long.MAX_VALUE)
            null
        else
            firstFocusFieldTime - startTime
    }

    // fa_st
    private fun isFirstTime() = firstTimeSeeForm

    /**
     * override setContentView for:
     * 1- Add focus view
     * 2- Init other property
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (view is ViewGroup)
            view.addView(focusView)

        formRepo = FormsRepository(requireContext())

        this.bitaApplicationContext = requireActivity().applicationContext
        this.deviceId = getDeviceId()
        this.fingerprint=getFingerPrint()
        this.resolution = getResolution()

        checkChildren(view as ViewGroup, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        firstTimeSeeForm = false
    }

    /** when the screen is paused,
     * the focus is removed from all fields
     * and the timer is deactivated to prevent
     * additional data from being generated.
     */
    override fun onPause() {
        super.onPause()
        clearFocus()
        timer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        checkChildren(view as ViewGroup)
        if (!changeStartTime && !temptest())
            startTime = System.currentTimeMillis()
        changeStartTime = false
    }
    fun temptest(): Boolean {
        getExternalMetaData()?.let {
            for (metadata in it) {
                if (metadata.changeAfterCreateMetaData)
                    return true
            }
        }
        return false
    }

    /**
     * Init timer for idle time
     */
    override fun onAnyAction() {
        timer?.cancel()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                createFormMetaData(false)
                getMetaData()?.let { it1 -> formRepo.sendData(it1, faId) }
                firstTimeSeeForm = false
            }
        }, 30000)
    }

    /**
     * this method is used to send data to Matomo.
     * if the created metadata is not null, it will be sent to Matomo.
     */
    fun trackerClickSubmitButton() {
        timer?.cancel()
        clearFocus()

        submitTime = System.currentTimeMillis()
        timeToSubmission = submitTime - startTime

        createFormMetaData(true)
        if (formMetaData != null)
            for (field in formMetaData?.fieldsMetaData!!) {
                if (field.fa_cn != null) {
                    TrackHelper.track()
                        .event("onSubmit", "submitted")
                        .name("${field.fa_fn} : ${field.fa_cn}")
                        .with(UiHandler.getTracker(TrackerSdkApplication.myApp))

                }
            }
        getMetaData()?.let { it1 -> formRepo.sendData(it1, faId) }
        faVid = RandomStringGenerator.getRandomString()
        firstTimeSeeForm = false

        fieldMetaData.clear()
        formMetaData = null

        firstFocusFieldTime = Long.MAX_VALUE
        firstFocusFieldName = null

        lastChangingFieldTime = 0
        lastChangingFieldName = null

        interactionTime = 0

        startTime = System.currentTimeMillis()
    }

    abstract fun getExternalMetaData(): List<FieldMetaData>?

    private fun clearFocus(viewGroup: View? = this.view) {
        focusView.requestFocus()
    }

    /**
     * Use for clear save state on the TrackerViews
     * used only for Recycler View, not elsewhere
     */
    fun clearData(
        editTexts: Boolean,
        editTextIds: List<Long>? = null,
        checkBox: Boolean = true,
        checkBoxIds: List<Long>? = null,
        radioButtons: Boolean = true,
        radioIds: List<Long>? = null
    ) {
        clearFocus()
        if (editTexts)
            TrackerEditText.resetSaveState(editTextIds)
        if (radioButtons)
            TrackerRadioGroup.resetSaveState(radioIds)
        if (checkBox)
            TrackerCheckBox.resetSaveState(checkBoxIds)
        timer?.cancel()
    }
}