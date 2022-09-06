package com.crossclassify.trackersdk.utils.view

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.crossclassify.trackersdk.R
import com.crossclassify.trackersdk.interfaces.local.TrackerActions
import com.crossclassify.trackersdk.data.model.FieldMetaData
import com.crossclassify.trackersdk.data.model.FocusTimed
import com.crossclassify.trackersdk.data.model.SaveStateField
import com.crossclassify.trackersdk.utils.base.TrackerSdkApplication
import com.crossclassify.trackersdk.utils.objects.UiHandler
import org.matomo.sdk.extra.TrackHelper

/**
 * For use this view in Recycler view
 * you should generate id for all item(you can use item position)
 * in onBindViewHolder you should use trackerEditText.loadState(newItemId)
 * for initialize item
 * At last you should call getAllMetaData in ONE VIEW in recycler view and create metadata for all
 * fields in recycler view
 */
class TrackerEditText(context: Context, attributeSet: AttributeSet?) :
    androidx.appcompat.widget.AppCompatEditText(context, attributeSet), TextWatcher {

    companion object {
        // used to maintain the current status of views with a specific id
        val saveState = HashMap<Long, SaveStateField>()

        // to reset views state in Recycler
        // if you have a list of ids, only those ids will be reset, otherwise all the items will be reset.
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

    // to keep the focus and unfocus time of the view
    private var focusList = ArrayList<FocusTimed>()

    // to hold the number of delete button presses
    private var deleteButtonClick = 0

    // to check for changes after creating metadata
    var changeAfterCreateMetaData = false

    // number of field changes
    private var changes = 0

    // field is focused or not
    private var focus = false

    // is the field changing?
    private var changing = false
    private var eventChanging = true

    // last time that field has changed
    private var lastChangeTime = 0L

    // the amount of time between focus and interaction
    private var firstHesitation = 0L

    // metadata to send fields to Matomo
    private var metaData: FieldMetaData? = null

    // to report happened actions (change and focus) to parent (activity, fragment, etc.)
    private var action: TrackerActions? = null

    // to save the field name
    private var fieldName: String? = null

    // to save the state in saveState
    private var id: Long = -1

    // to perform calculations of number of clicks on the delete button
    private var lastText: String = ""

    // get field name from xml
    init {
        val attr = context.obtainStyledAttributes(attributeSet, R.styleable.TrackerEditText)
        fieldName = attr.getString(R.styleable.TrackerEditText_fieldName)
        attr.recycle()
    }

    /** in this method, after performing the focus, the necessary variables are set
     * in case of unfocus feild name will be sent to the tracker along with the text inside it,
     * and the unfocus time will also be saved.
     * if the view is inside the recyclerView, its status is also saved */
    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        val time = System.currentTimeMillis()
        focus = focused
        if (focused) {
            changeAfterCreateMetaData = true
            focusList.add(FocusTimed(time, -1, false))
            lastChangeTime = time
        } else {
            if (focusList.isNotEmpty())
                focusList.last().unFocus = lastChangeTime
            if ((getSize() != 0) and eventChanging and (tag != null)) {
                if (tag == "IncludeContentTracking") {
                    TrackHelper.track()
                        .event("onFocusChange", "notSubmitted")
                        .name("$fieldName : $text")
                        .with(UiHandler.getTracker(TrackerSdkApplication.myApp))
                    eventChanging = false
                }
            }
            changing = false
        }
        action?.onAnyAction()
        saveState(id)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    /** in this method, first is calculated if first Time Hesitation is not set
     * in the next step, the text changes are checked */
    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        val time = System.currentTimeMillis()
        eventChanging = true
        if (focus) {
            changeAfterCreateMetaData = true
            if (!changing) {
                if (firstHesitation == 0L)
                    for (i in focusList.indices) {
                        if (focusList[i].unFocus == -1L) {
                            firstHesitation += time - focusList[i].focus
                            break
                        }
                        if (!focusList[i].changing) {
                            firstHesitation += focusList[i].unFocus - focusList[i].focus
                        }
                    }
                changes++
                changing = true
                focusList.last().changing = true
            }
        }
        // اif the text size is reduced, it calculates the number of delete clicks
        if (lengthAfter < lengthBefore && lastText != text.toString()) {
            deleteButtonClick++
        }
        // if has focused on View for more than 30 seconds and then continues typing again, a new record will be set for her.
        if (time - lastChangeTime > 30000) {
            if (focusList != null && focusList.isNotEmpty()) {
                focusList.last().unFocus = lastChangeTime
                changes++
                focusList.add(FocusTimed(time, -1, true))
            }
        }
        lastChangeTime = time
        // if it is focused on view, action is called
        if (focus)
            action?.onAnyAction()
        lastText = text.toString()
    }

    // if used in Recycler View, the metadata will be updated after each step
    override fun afterTextChanged(s: Editable?) {
        if (id != -1L) {
            createMetaData()
        }
    }

    // for recycler view
    // this method stores all the properties of the view according to an id in a class
    private fun saveState(id: Long) {
        removeTextChangedListener(this)
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
            fieldName = fieldName
        )
        addTextChangedListener(this)
    }

    // this method is for loading View state
    /** If the id given to the function does not already exist, the values will set
     * Otherwise, it is checked that if the reset is  true in the State model,
     * it means that the view is reset and the properties must get the initial values,
     * otherwise the previous values will be loaded.**/
    fun loadState(newId: Long, metaData: FieldMetaData?, text: String?): FieldMetaData? {
        clearFocus()
        removeTextChangedListener(this)
        saveState[newId].let {
            if (it == null) {
                // init
                lastText = text ?: ""
                setText(lastText)
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

                    lastText = text ?: ""
                    setText(lastText)
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
                    setText(it.text)
                    lastText = it.text ?: ""
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
        addTextChangedListener(this)
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
        lastChangeTime: Long = this.lastChangeTime,
        setLastTime: Boolean = true
    ): Long {
        var resultTime: Long = 0
        for (time in focusList) {
            if (time.unFocus == -1L) {
                if (setLastTime) {
                    focusList.last().unFocus = lastChangeTime
                }
                resultTime += lastChangeTime - time.focus
            } else {
                resultTime += time.unFocus - time.focus
            }
        }
        return resultTime
    }

    // fa_fht
    private fun getFirstHesitationTime(firstHesitation: Long = this.firstHesitation): Long {
        return firstHesitation
    }

    // fa_fb
    private fun checkEmpty(text: String = this.text.toString()) = text.isEmpty()

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
    private fun howManyClickDeleteButton(deleteButtonClick: Int = this.deleteButtonClick) =
        deleteButtonClick

    // fa_cu
    private fun howManyClickCursorButton() = null

    // fa_ft
    private fun getType() = "text"

    // fa_fs
    private fun getSize(text: String = this.text.toString()) = text.length

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

    fun setAction(action: TrackerActions?) {
        this.action = action
    }

    /** in this function, metadata is created
     * if the metadata field is already set,
     * the same object will be updated,
     * but if it is null, a new object will be created with the new data.*/
    fun createMetaData(clear: Boolean = false) {
        if (focusList.size == 0)
            metaData = null
        if (metaData == null) {
            metaData = FieldMetaData(
                fa_fts = getAllFocusTime(setLastTime = clear),
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
                fa_fts = getAllFocusTime(setLastTime = clear)
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
                changeAfterCreateMetaData = this@TrackerEditText.changeAfterCreateMetaData
            }
        }
        if (id == -1L && clear)
            changeAfterCreateMetaData = false

        if (clear) {
            removeTextChangedListener(this)
            lastText = ""
            setText(lastText)
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
            addTextChangedListener(this)
        }
    }
}