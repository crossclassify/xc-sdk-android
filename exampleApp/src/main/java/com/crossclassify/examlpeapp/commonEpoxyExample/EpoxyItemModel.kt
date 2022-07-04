package com.crossclassify.examlpeapp.commonEpoxyExample

import com.crossclassify.examlpeapp.R
import com.crossclassify.trackersdk.interfaces.TrackerActions
import com.crossclassify.trackersdk.TrackerEditText

class EpoxyItemModel(
    private val id: Long,
    private val listener: EpoxyPassItems,
    private val initText: String = "",
    private val trackerListener: TrackerActions
) : KotlinModel(R.layout.item_epoxy) {
    private val et by bind<TrackerEditText>(R.id.trackerET)
    override fun bind() {
        val lastMetadata = listener.getMetaData(id)
        val newMetadata = et.loadState(id, lastMetadata, initText)
        listener.passMetaData(id, newMetadata)
        et.setFieldName(id.toString())
        et.tag = "IncludeContentTracking"
        et.setAction(trackerListener)
    }
}