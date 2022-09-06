package com.crossclassify.trackersdk.utils.base

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.crossclassify.trackersdk.interfaces.local.TrackerActions
import com.crossclassify.trackersdk.interfaces.local.TrackerFun
import com.crossclassify.trackersdk.data.model.FieldMetaData
import com.crossclassify.trackersdk.data.repository.FormsRepository
import com.crossclassify.trackersdk.utils.objects.TrackerFunctions
import com.crossclassify.trackersdk.utils.view.TrackerCheckBox
import com.crossclassify.trackersdk.utils.view.TrackerEditText
import com.crossclassify.trackersdk.utils.view.TrackerRadioGroup
import timber.log.Timber

abstract class TrackerActivity : AppCompatActivity(),
    TrackerActions, TrackerFun {
    private val trackerTools = TrackerFunctions()

    // focus view for unfocused all view in activity
    private val focusView: View by lazy {
        View(this).apply {
            isFocusable = true
            isFocusableInTouchMode = true
        }
    }

    /**
     * override setContentView for:
     * 1- Add focus view
     * 2- Init other property
     */
    override fun setContentView(view: View) {
        super.setContentView(view)

        Timber.tag("CrossClassify:")
            .i("You have successfully added TrackerActivity.")

        Timber.tag("CrossClassify:")
            .i("Now please override getFormName().")

        Timber.tag("CrossClassify:")
            .i("Now please override getExternalMetaData().")

        trackerTools.initValues(
            this,
            this,
            view,
            this,
            this,
            FormsRepository(this)
        )
        if (view is ViewGroup) (view as ViewGroup).addView(focusView, 0)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)

        Timber.tag("CrossClassify:")
            .i("You have successfully added TrackerActivity.")

        Timber.tag("CrossClassify:")
            .i("Now please override getFormName().")

        Timber.tag("CrossClassify:")
            .i("Now please override getExternalMetaData().")

        val mView = findViewById<ViewGroup>(android.R.id.content).rootView
        (mView as ViewGroup).addView(focusView, 0)
        trackerTools.initValues(
            this,
            this,
            mView,
            this,
            this,
            FormsRepository(this)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        trackerTools.onDestroy()
    }

    /** when the screen is paused,
     * the focus is removed from all fields
     * and the timer is deactivated to prevent
     * additional data from being generated.
     */
    override fun onPause() {
        super.onPause()
        trackerTools.onPause()
    }

    override fun onResume() {
        super.onResume()
        trackerTools.onResume()
    }


    /**
     * Init timer for idle time
     */
    override fun onAnyAction() {
        trackerTools.runTimer()
    }

    override fun clearFocus() {
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
        trackerTools.cancelTimer()
    }

    override fun trackerClickSubmitButton(){
        trackerTools.trackerClickSubmitButton()
    }

    /**
     * this method is used to send data to Matomo.
     * if the created metadata is not null, it will be sent to Matomo.
     */
    abstract override fun getExternalMetaData(): List<FieldMetaData>?
    abstract override fun getFormName(): String
}