package com.crossclassify.trackersdk

import com.crossclassify.trackersdk.service.config.TrackerSdkApplication
import com.crossclassify.trackersdk.utils.objects.UiHandler.getTracker
import org.matomo.sdk.extra.TrackHelper

class ScreenNavigationTracking {
    // track screen navigation
    fun trackNavigation(screen: String, title: String) {
        TrackHelper.track()
            .screen("$screen ")
            .title(title)
            .with(getTracker(TrackerSdkApplication.myApp))
    }

}