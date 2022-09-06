package com.crossclassify.trackersdk.utils.objects

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView

import com.crossclassify.trackersdk.interfaces.local.TrackerActions
import com.crossclassify.trackersdk.interfaces.local.TrackerFun
import com.crossclassify.trackersdk.data.model.FieldMetaData
import com.crossclassify.trackersdk.data.model.FormMetaData
import com.crossclassify.trackersdk.utils.base.TrackerSdkApplication
import com.crossclassify.trackersdk.data.repository.FormsRepository
import com.crossclassify.trackersdk.utils.view.TrackerCheckBox
import com.crossclassify.trackersdk.utils.view.TrackerEditText
import com.crossclassify.trackersdk.utils.view.TrackerRadioGroup
import org.matomo.sdk.extra.TrackHelper
import java.util.*

class TrackerFunctions {
    private lateinit var view: View
    private lateinit var trackerFun: TrackerFun
    private lateinit var trackerActions: TrackerActions
    private lateinit var formRepo: FormsRepository
    fun initValues(
        context: Context,
        fragmentActivity: FragmentActivity,
        view: View,
        trackerFun: TrackerFun,
        trackerAction: TrackerActions,
        formsRepository: FormsRepository
    ) {
        this.view = view
        this.trackerFun = trackerFun
        this.trackerActions = trackerAction
        this.formRepo = formsRepository
        this.bitaApplicationContext = fragmentActivity.applicationContext
        this.deviceId = getDeviceId(context)
        this.faId = trackerFun.getFormName()
        this.fingerprint = getFingerPrint(context)
        this.resolution = getResolution(fragmentActivity)
        checkChildren(view as ViewGroup, false)
    }

    var changeStartTime = false

    private val PREF_KEY_TRACKER_VISITORID = "tracker.visitorid"
    private val PREF_KEY_TRACKER_FINGERPRINT = "tracker.fingerprint"

    private lateinit var bitaApplicationContext: Context

    var startTime: Long = System.currentTimeMillis()
    private var submitTime: Long = 0

    var firstTimeSeeForm = true

    // save fields metadata in form
    private val fieldMetaData = ArrayList<FieldMetaData>()
    private var formMetaData: FormMetaData? = null

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

    private var faId:String= "default"
    private var deviceId: String? = null
    private var fingerprint: String? = null
    private var resolution: String? = null
    private var idSite = Values.SITE_ID

    fun getMetaData(): FormMetaData? {
        return formMetaData
    }

    // create device id
    private fun getDeviceId(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(
            "org.matomo.sdk_FE8DB41078DFFC3D9751687595C3B837",
            Context.MODE_PRIVATE
        )
        deviceId = sharedPreferences.getString(PREF_KEY_TRACKER_VISITORID, null)
        return deviceId

    }

    private fun getFingerPrint(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(
            "org.matomo.sdk_FE8DB41078DFFC3D9751687595C3B837",
            Context.MODE_PRIVATE
        )
        fingerprint = sharedPreferences.getString(PREF_KEY_TRACKER_FINGERPRINT, null)
        return fingerprint
    }

    // get device resolution
    private fun getResolution(fragmentActivity: FragmentActivity): String {
        val displayMetrics = DisplayMetrics()
        fragmentActivity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        return "$height" + "x$width"
    }

    fun createFormMetaData(trackerAction: TrackerActions, submitButtonClick: Boolean) {
        initProperty()
        // get metadata from form fields
        if (view is ViewGroup)
            checkChildren(view as ViewGroup, true, submitButtonClick)

        val resultFieldMetaData = ArrayList<FieldMetaData>()
        resultFieldMetaData.addAll(fieldMetaData)

        // if use fields in recycler view
        // get all metadata in recycler view and merge with fields out of recycler view
        trackerFun.getExternalMetaData()?.let { metaData ->

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
            uid = fingerprint,
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
    fun checkChildren(
        viewGroup: ViewGroup = view as ViewGroup,
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
//                        if (view.changeAfterCreateMetaData) {
                        view.createMetaData(isSubmitted)
                        view.getMetaData()?.let {
                            fieldMetaData.add(it)
                        }
//                        }
                    } else {
                        // if we do not have Get Data, we will pass the action to the timer
                        if (view.changeAfterCreateMetaData && !changeStartTime) {
                            changeStartTime = true
                        }
                        view.setAction(trackerActions)
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
                        view.setAction(trackerActions)
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
                        view.setAction(trackerActions)
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

    fun trackerClickSubmitButton() {
        timer?.cancel()
        trackerFun.clearFocus()

        submitTime = System.currentTimeMillis()
        timeToSubmission = submitTime - startTime

        createFormMetaData(trackerActions, true)
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
        pvId = RandomStringGenerator.getRandomString()
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

    fun cancelTimer() {
        timer?.cancel()
    }

    fun runTimer() {
        timer?.cancel()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                createFormMetaData(trackerActions, false)
                getMetaData()?.let { it1 -> formRepo.sendData(it1, faId) }
                firstTimeSeeForm = false
            }
        }, 30000)
    }

    fun clearData(
        editTexts: Boolean,
        editTextIds: List<Long>? = null,
        checkBox: Boolean = true,
        checkBoxIds: List<Long>? = null,
        radioButtons: Boolean = true,
        radioIds: List<Long>? = null
    ) {
        trackerFun.clearFocus()
        if (editTexts)
            TrackerEditText.resetSaveState(editTextIds)
        if (radioButtons)
            TrackerRadioGroup.resetSaveState(radioIds)
        if (checkBox)
            TrackerCheckBox.resetSaveState(checkBoxIds)
        timer?.cancel()
    }

    fun onResume(){
        checkChildren(view as ViewGroup)
        if (!changeStartTime && !temptest())
            startTime = System.currentTimeMillis()
        changeStartTime = false
    }

    fun onPause(){
        trackerFun.clearFocus()
        cancelTimer()
    }

    fun onDestroy(){
        firstTimeSeeForm = false
    }

    private fun temptest(): Boolean {
        trackerFun.getExternalMetaData()?.let {
            for (metadata in it) {
                if (metadata.changeAfterCreateMetaData)
                    return true
            }
        }
        return false
    }
}