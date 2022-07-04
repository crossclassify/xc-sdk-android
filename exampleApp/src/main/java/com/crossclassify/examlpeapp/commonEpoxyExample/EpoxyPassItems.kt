package com.crossclassify.examlpeapp.commonEpoxyExample

import com.crossclassify.trackersdk.model.FieldMetaData

interface EpoxyPassItems{
    fun passMetaData(id: Long, fieldMetaData: FieldMetaData?)
    fun getMetaData(id: Long): FieldMetaData?
}