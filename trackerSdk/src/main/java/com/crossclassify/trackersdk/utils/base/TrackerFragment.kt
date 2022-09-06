package com.crossclassify.trackersdk.utils.base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.crossclassify.trackersdk.interfaces.local.TrackerActions
import com.crossclassify.trackersdk.interfaces.local.TrackerFun
import com.crossclassify.trackersdk.data.model.FieldMetaData
import com.crossclassify.trackersdk.data.repository.FormsRepository
import com.crossclassify.trackersdk.utils.objects.TrackerFunctions
import com.crossclassify.trackersdk.utils.view.TrackerCheckBox
import com.crossclassify.trackersdk.utils.view.TrackerEditText
import com.crossclassify.trackersdk.utils.view.TrackerRadioGroup
import timber.log.Timber


abstract class TrackerFragment : Fragment(),
    TrackerActions, TrackerFun {
    private val trackerTools = TrackerFunctions()

    private val focusView: View by lazy {
        View(requireContext()).apply {
            isFocusable = true
            isFocusableInTouchMode = true
        }
    }

    /**
     * override setContentView for:
     * 1- Add focus view
     * 2- Init other property
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.tag("CrossClassify:")
            .i("You have successfully added TrackerActivity.")

        Timber.tag("CrossClassify:")
            .i("Now please override getFormName().")

        Timber.tag("CrossClassify:")
            .i("Now please override getExternalMetaData().")

        trackerTools.initValues(
            requireContext(),
            requireActivity(),
            view,
            this,
            this,
            FormsRepository(requireContext())
        )
        if (view is ViewGroup) view.addView(focusView, 0)
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

    override fun trackerClickSubmitButton() {
        trackerTools.trackerClickSubmitButton()
    }

    abstract override fun getExternalMetaData(): List<FieldMetaData>?
    abstract override fun getFormName(): String
}