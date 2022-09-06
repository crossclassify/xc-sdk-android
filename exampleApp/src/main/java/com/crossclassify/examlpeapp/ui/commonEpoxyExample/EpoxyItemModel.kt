package com.crossclassify.examlpeapp.ui.commonEpoxyExample

import android.widget.TextView
import com.crossclassify.examlpeapp.R
import com.crossclassify.trackersdk.interfaces.local.TrackerActions
import com.crossclassify.trackersdk.utils.view.TrackerEditText

class EpoxyItemModel(
    private val id: Long,
    private val listener: EpoxyPassItems,
    private val initText: String = "",
    private val trackerListener: TrackerActions
) : KotlinModel(R.layout.epoxy_recycler_item) {
    private val et by bind<TrackerEditText>(R.id.et)
    private val tv by bind<TextView>(R.id.tv)
    override fun bind() {
        val lastMetadata = listener.getMetaData(id)
        val newMetadata = et.loadState(id, lastMetadata, initText)
        listener.passMetaData(id, newMetadata)
        et.setFieldName(id.toString())
        tv.text=id.toString()
        et.tag = "IncludeContentTracking"
        et.setAction(trackerListener)
    }
}