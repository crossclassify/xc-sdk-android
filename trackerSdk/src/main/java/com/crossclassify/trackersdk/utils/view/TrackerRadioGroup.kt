package com.crossclassify.trackersdk.utils.view

import android.content.Context
import android.util.AttributeSet
import android.widget.RadioButton
import android.widget.RadioGroup
import com.crossclassify.trackersdk.R
import com.crossclassify.trackersdk.interfaces.local.TrackerActions
import com.crossclassify.trackersdk.data.model.FieldMetaData
import com.crossclassify.trackersdk.data.model.FocusTimed
import com.crossclassify.trackersdk.data.model.SaveStateField
import com.crossclassify.trackersdk.utils.base.TrackerSdkApplication
import com.crossclassify.trackersdk.utils.objects.UiHandler
import org.matomo.sdk.extra.TrackHelper

class TrackerRadioGroup(context: Context, attributeSet: AttributeSet) :
    RadioGroup(context, attributeSet),
    RadioGroup.OnCheckedChangeListener {
    companion object {
        val saveState = HashMap<Long, SaveStateField>()
        fun resetSaveState(ids: List<Long>? = null) {
            when (ids) {
                null -> {
                    for (i in saveState.keys) {
                        saveState[i]?.reset = true
                    }
                }
                else -> {
                    for (i in ids) {
                        saveState[i]?.reset = true
                    }
                }
            }
        }
    }

    private var text: String? = null

    private var focusList = ArrayList<FocusTimed>()

    private var deleteButtonClick = 0

    var changeAfterCreateMetaData = false

    private var changes = 0
    private var focus = false
    private var changing = false
    private var eventChanging = true
    private var lastChangeTime = 0L

    private var firstHesitation = 0L

    private var metaData: FieldMetaData? = null

    private var action: TrackerActions? = null

    private var fieldName: String? = null

    private var id: Long = -1

    //private
    private var leftBlank = true
    private var checkBtnId: Int = -1

    init {
        val attr = context.obtainStyledAttributes(attributeSet, R.styleable.TrackerRadioGroup)
        fieldName = attr.getString(R.styleable.TrackerRadioGroup_radio_field_name)
        attr.recycle()
        setOnCheckedChangeListener(this)
    }

    // for recycler view
    private fun saveState(id: Long) {
        setOnCheckedChangeListener(null)
        saveState[id] = SaveStateField(
            text = "",
            focusList = focusList.clone() as ArrayList<FocusTimed>,
            deleteButtonClick = deleteButtonClick,
            changeAfterCreateMetaData = changeAfterCreateMetaData,
            changes = changes,
            focus = focus,
            changing = changing,
            eventChanging = eventChanging,
            lastChangeTime = lastChangeTime,
            firstHesitation = firstHesitation,
            action = action,
            fieldName = fieldName,
            checkId = if (checkBtnId != -1) checkBtnId else null
        )
        setOnCheckedChangeListener(this)
    }

    fun loadState(newId: Long, metaData: FieldMetaData?): FieldMetaData? {
        setOnCheckedChangeListener(null)
        saveState[newId].let {
            if (it == null) {
                // init
                clearCheck()
                focusList.clear()
                deleteButtonClick = 0
                changeAfterCreateMetaData = false
                changes = 0
                focus = false
                changing = false
                eventChanging = true
                lastChangeTime = 0
                firstHesitation = 0
                action = null
                fieldName = null

                createMetaData()
            } else {
                if (it.reset) {
                    it.reset = false

                    clearCheck()
                    text = ""
                    focusList.clear()
                    deleteButtonClick = 0
                    changeAfterCreateMetaData = false
                    changes = 0
                    focus = false
                    changing = false
                    eventChanging = true
                    lastChangeTime = 0
                    firstHesitation = 0
                    action = null
                    fieldName = null

                    saveState(newId)
                    createMetaData()
                } else {
                    it.checkId?.let { checkId ->
                        check(checkId)
                    }
                    text = it.text
                    focusList = it.focusList
                    deleteButtonClick = it.deleteButtonClick
                    changeAfterCreateMetaData = it.changeAfterCreateMetaData
                    changes = it.changes
                    focus = it.focus
                    changing = it.changing
                    eventChanging = it.eventChanging
                    lastChangeTime = it.lastChangeTime
                    firstHesitation = it.firstHesitation
                    action = it.action
                    fieldName = it.fieldName

                    this.metaData = metaData
                }
            }
        }
        id = newId
        setOnCheckedChangeListener(this)
        return this.metaData
    }

    // first time focus
    fun getFirstFocusTime(): Long? {
        return if (focusList.isNotEmpty()) {
            focusList.first().focus
        } else {
            null
        }
    }

    // fa_fts
    // How much time focus
    fun getAllFocusTime(
        focusList: ArrayList<FocusTimed> = this.focusList,
        lastChangeTime: Long = this.lastChangeTime
    ): Long {
        var resultTime: Long = 0
        for (time in focusList) {
            if (time.unFocus == -1L) {
                focusList.last().unFocus = lastChangeTime
            }
            resultTime += time.unFocus - time.focus
        }
        return (changes * 57).toLong()
    }

    // fa_fht
    private fun getFirstHesitationTime(firstHesitation: Long = this.firstHesitation): Long {
        return 57
    }

    // fa_fb
    private fun checkEmpty(text: String = this.text.toString()) = leftBlank

    // fa_fn
    fun getFieldName(fieldName: String? = this.fieldName) = fieldName
    fun setFieldName(name: String?) {
        fieldName = name
    }

    // fa_fch
    private fun howManyChanges(changes: Int = this.changes) = changes

    // fa_ff
    private fun howManyFocus(focusList: ArrayList<FocusTimed> = this.focusList) = focusList.size

    // fa_fd
    private fun howManyClickDeleteButton(deleteButtonClick: Int = this.deleteButtonClick) = 0

    // fa_cu
    private fun howManyClickCursorButton() = 0

    // fa_ft
    private fun getType() = "radio"

    // fa_fs
    private fun getSize(text: String = this.text.toString()) = -1

    // fa_cn
    private fun getFiled(tag: Any? = this.tag) =
        if (tag == "IncludeContentTracking") text.toString() else null

    fun getMetaData(): FieldMetaData? {
        return metaData
    }

    fun getLastFocusTimeForChange(focusList: ArrayList<FocusTimed> = this.focusList): Long? {
        if (focusList.size == 0)
            return null
        else {
            for (i in focusList.reversed()) {
                if (i.changing)
                    return i.focus
            }
        }
        return null
    }

    fun setAction(action: TrackerActions) {
        this.action = action
    }

    fun createMetaData(clear: Boolean = false) {
        if (focusList.size == 0)
            metaData = null
        if (metaData == null) {
            metaData = FieldMetaData(
                fa_fts = getAllFocusTime(),
                fa_fht = getFirstHesitationTime(),
                fa_fb = checkEmpty(),
                fa_fn = getFieldName(),
                fa_fch = howManyChanges(),
                fa_ff = howManyFocus(),
                fa_fd = howManyClickDeleteButton(),
                fa_cu = howManyClickCursorButton(),
                fa_fs = getSize(),
                fa_ft = getType(),
                fa_cn = getFiled(),
                firstFocusTime = getFirstFocusTime(),
                lastFocusChangeTime = getLastFocusTimeForChange(),
                changeAfterCreateMetaData = changeAfterCreateMetaData
            )
        } else {
            metaData?.apply {
                fa_fts = getAllFocusTime()
                fa_fht = getFirstHesitationTime()
                fa_fb = checkEmpty()
                fa_fn = getFieldName()
                fa_fch = howManyChanges()
                fa_ff = howManyFocus()
                fa_fd = howManyClickDeleteButton()
                fa_cu = howManyClickCursorButton()
                fa_fs = getSize()
                fa_ft = getType()
                fa_cn = getFiled()
                firstFocusTime = getFirstFocusTime()
                lastFocusChangeTime = getLastFocusTimeForChange()
                changeAfterCreateMetaData = this@TrackerRadioGroup.changeAfterCreateMetaData
            }
        }
        if (id == -1L && clear)
            changeAfterCreateMetaData = false

        setOnCheckedChangeListener(null)
        if (clear) {
            clearCheck()
            text = ""
            focusList.clear()
            deleteButtonClick = 0
            changeAfterCreateMetaData = false
            changes = 0
            focus = false
            changing = false
            eventChanging = true
            lastChangeTime = 0
            firstHesitation = 0

            if (id != -1L)
                saveState(id)
        }
        setOnCheckedChangeListener(this)
    }

    /**
     * this method is called when one of the radio buttons is checked
     * if is checked, the text field will change according to the text of the radio button
     * the field name is also sent to the tracker
     * a record is created for the focus list to count the number of focus and changes
     */
    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        saveState(id)
        val view = findViewById<RadioButton>(checkedId)
        val time = System.currentTimeMillis()

        if (view != null)
            if (view.isChecked) {
                text = view.text.toString()
                changes++
                leftBlank = false
                checkBtnId = checkedId
                focusList.add(
                    FocusTimed(
                        time, time, true
                    )
                )
            }
        if (tag == "IncludeContentTracking") {
            TrackHelper.track()
                .event("onCheckedChange", "notSubmitted")
                .name("$fieldName : ${getFiled()}")
                .with(UiHandler.getTracker(TrackerSdkApplication.myApp))
        }

        eventChanging = true
        changeAfterCreateMetaData = true
        if (firstHesitation == 0L) {
            firstHesitation = time
        }

        changing = true

        lastChangeTime = time
        action?.onAnyAction()
        saveState(id)
        createMetaData()
    }
}