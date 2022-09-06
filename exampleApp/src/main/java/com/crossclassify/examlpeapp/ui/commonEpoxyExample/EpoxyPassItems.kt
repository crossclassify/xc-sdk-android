package com.crossclassify.examlpeapp.ui.commonEpoxyExample

import com.crossclassify.trackersdk.data.model.FieldMetaData

interface EpoxyPassItems{
    fun passMetaData(id: Long, fieldMetaData: FieldMetaData?)
    fun getMetaData(id: Long): FieldMetaData?
}