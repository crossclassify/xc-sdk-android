package com.crossclassify.examlpeapp


import com.crossclassify.trackersdk.utils.base.TrackerSdkApplication

//Add Base Class And Pass IdSite For Configuration
class MyApplication : TrackerSdkApplication() {
    override fun onCreate() {
        createDefaultConfig(301, "API_KEY_HERE")
        super.onCreate()
    }
}