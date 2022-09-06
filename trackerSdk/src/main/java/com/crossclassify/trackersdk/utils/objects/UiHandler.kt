package com.crossclassify.trackersdk.utils.objects

import android.view.View
import android.view.ViewGroup
import com.crossclassify.trackersdk.utils.base.TrackerSdkApplication
import org.matomo.sdk.Tracker

object UiHandler {

    /** Find Views By Specific Tag For Send Events **/
    private fun getViewsByTag(root: ViewGroup): ArrayList<View> {
        val views = ArrayList<View>()
        val childCount = root.childCount

        for (i in 0 until childCount) {

            val child = root.getChildAt(i)
            if (child is ViewGroup) {
                views.addAll(getViewsByTag(child))
            }

            val tagObj = child.tag
            if (tagObj != null && tagObj == "IncludeContentTracking") {
                views.add(child)
            }
        }

        return views
    }

    /** Get Tracker To Start Track **/
    fun getTracker(application: TrackerSdkApplication): Tracker? {
        return application.getTracker()
    }
}