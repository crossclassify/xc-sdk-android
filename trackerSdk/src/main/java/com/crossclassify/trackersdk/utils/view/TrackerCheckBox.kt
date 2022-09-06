package com.crossclassify.trackersdk.utils.view

import android.content.Context
import android.util.AttributeSet
import android.widget.CompoundButton
import com.crossclassify.trackersdk.R
import com.crossclassify.trackersdk.interfaces.local.TrackerActions
import com.crossclassify.trackersdk.data.model.FieldMetaData
import com.crossclassify.trackersdk.data.model.FocusTimed
import com.crossclassify.trackersdk.data.model.SaveStateField
import com.crossclassify.trackersdk.utils.base.TrackerSdkApplication
import com.crossclassify.trackersdk.utils.objects.UiHandler
import org.matomo.sdk.extra.TrackHelper

class TrackerCheckBox(context: Context, attributeSet: AttributeSet) :
    androidx.appcompat.widget.AppCompatCheckBox(context, attributeSet),
    CompoundButton.OnCheckedChangeListener {
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

    private var lastState = false

    //private
    private var leftBlank = true

    init {
        val attr = context.obtainStyledAttributes(attributeSet, R.styleable.TrackerCheckBox)
        fieldName = attr.getString(R.styleable.TrackerCheckBox_check_box_field_name)
        attr.recycle()
        setOnCheckedChangeListener(this)
    }

    // for recycler view
    private fun saveState(id: Long) {
        setOnCheckedChangeListener(null)
        saveState[id] = SaveStateField(
            text = text.toString(),
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
            checked = isChecked
        )
        setOnCheckedChangeListener(this)
    }

    fun loadState(newId: Long, metaData: FieldMetaData?): FieldMetaData? {
        setOnCheckedChangeListener(null)
        saveState[newId].let {
            if (it == null) {
                // init
                isChecked = false
                lastState = false
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

                    isChecked = false
                    lastState = false
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
                    it.checked?.let { checked ->
                        isChecked = checked
                        lastState = checked
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
    fun getAllFocusTime(): Long {
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
    private fun getType() = "checkBox"

    // fa_fs
    private fun getSize(text: String = this.text.toString()) = -1

    // fa_cn
    private fun getFiled(tag: Any? = this.tag) = if (tag == "IncludeContentTracking") {
        this.isChecked.toString()
    } else null

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
                changeAfterCreateMetaData = this@TrackerCheckBox.changeAfterCreateMetaData
            }
        }
        if (id == -1L && clear)
            changeAfterCreateMetaData = false

        if (clear) {
            setOnCheckedChangeListener(null)
            isChecked = false
            lastState = false
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
            setOnCheckedChangeListener(this)
        }
    }

    /**
     * this method is called when one of the checkbox is checked
     * if is checked, the text field will change according to the text of the checkbox
     * the field name is also sent to the tracker
     * a record is created for the focus list to count the number of focus and changes
     */
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        val time = System.currentTimeMillis()
        eventChanging = true

        changeAfterCreateMetaData = true
        if (firstHesitation == 0L) {
            firstHesitation = time
        }
        if (lastState != isChecked)
        focusList.add(
            FocusTimed(
                time, time, true
            )
        )
        leftBlank = !this.isChecked
        if (tag == "IncludeContentTracking") {
            TrackHelper.track()
                .event("onCheckedChange", "notSubmitted")
                .name("$fieldName : ${getFiled()}")
                .with(UiHandler.getTracker(TrackerSdkApplication.myApp))
        }
        changes++
        changing = true

        lastChangeTime = time
        action?.onAnyAction()
        saveState(id)
        createMetaData()
        lastState = isChecked
    }
}