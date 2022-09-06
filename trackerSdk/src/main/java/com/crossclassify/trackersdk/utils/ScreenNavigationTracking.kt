package com.crossclassify.trackersdk.utils

import com.crossclassify.trackersdk.utils.base.TrackerSdkApplication
import com.crossclassify.trackersdk.utils.objects.UiHandler.getTracker
import org.matomo.sdk.extra.TrackHelper
import timber.log.Timber

class ScreenNavigationTracking {
    // track screen navigation
    fun trackNavigation(screen: String, title: String) {
        TrackHelper.track()
            .screen("$screen ")
            .title(title)
            .with(getTracker(TrackerSdkApplication.myApp))

        Timber.tag("CrossClassify:")
            .i("You have successfully added screenNavigationTracking.")
    }

}