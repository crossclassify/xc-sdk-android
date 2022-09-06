package com.crossclassify.trackersdk.interfaces.local

import com.crossclassify.trackersdk.data.model.FieldMetaData

interface TrackerFun {
    fun getFormName(): String
    fun getExternalMetaData(): List<FieldMetaData>?
    fun clearFocus()
    fun trackerClickSubmitButton()
}