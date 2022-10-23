package com.crossclassify.examlpeapp


import com.crossclassify.trackersdk.utils.base.TrackerSdkApplication

//Add Base Class And Pass IdSite For Configuration
class MyApplication : TrackerSdkApplication() {
    override fun onCreate() {
        createDefaultConfig(137, "Wz5C96h5dg37j4tlmVt3b6UD4O1GDLv34fHmfp6l")
        super.onCreate()
    }
}