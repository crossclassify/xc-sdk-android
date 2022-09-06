package com.crossclassify.examlpeapp.ui.defaultEpoxyWithControllerExample

import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.crossclassify.examlpeapp.R
import com.crossclassify.trackersdk.data.model.FieldMetaData
import com.crossclassify.trackersdk.interfaces.local.TrackerActions
import com.crossclassify.trackersdk.utils.view.TrackerEditText

@EpoxyModelClass(layout = R.layout.epoxy_recycler_item)
abstract class EpoxyModel : EpoxyModelWithHolder<EpoxyModel.Holder>() {
    @field:EpoxyAttribute
    var position: Int = 0

    @field:EpoxyAttribute
    var title: String? = null

    @field:EpoxyAttribute
    var value: String? = null

    //    @field:EpoxyAttribute var listener: TextWatcher? = null
    @field:EpoxyAttribute
    var fieldMetaData: FieldMetaData? = null

    @field:EpoxyAttribute
    var updateMetaData: (FieldMetaData?) -> Unit = {}

    @field:EpoxyAttribute
    var listener: TrackerActions? = null
    override fun bind(holder: Holder) {
        val md = holder.et?.loadState(position.toLong(), fieldMetaData, null)
        holder.tv?.text = title
        updateMetaData(md)
        holder.et?.setFieldName(position.toString())
        holder.et?.tag = "IncludeContentTracking"
        holder.et?.setAction(listener)
    }

    class Holder : EpoxyHolder() {

        var tv: TextView? = null
        var et: TrackerEditText? = null

        override fun bindView(itemView: View) {
            tv = itemView.findViewById(R.id.tv)
            et = itemView.findViewById(R.id.et)
        }
    }
}