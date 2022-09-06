package com.crossclassify.examlpeapp.ui.defaultRecyclerViewExample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crossclassify.examlpeapp.R
import com.crossclassify.trackersdk.data.model.FieldMetaData
import com.crossclassify.trackersdk.interfaces.local.TrackerActions
import com.crossclassify.trackersdk.utils.view.TrackerCheckBox
import com.crossclassify.trackersdk.utils.view.TrackerEditText
import com.crossclassify.trackersdk.utils.view.TrackerRadioGroup

class RecyclerViewAdapter(
    private val mList: List<ItemsViewModel>,
    private val listener: TrackerActions
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val metaData: HashMap<Int, FieldMetaData?> = HashMap()

    companion object {
        const val VIEW_TYPE_ONE = 0
        const val VIEW_TYPE_TWO = 1
        const val VIEW_TYPE_THREE = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_ONE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_item, parent, false)
                return View1Holder(view)
            }
            VIEW_TYPE_TWO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_item_radio, parent, false)
                return View2Holder(view)
            }
            VIEW_TYPE_THREE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_item_checkbox, parent, false)
                return View3Holder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_item, parent, false)
                return View1Holder(view)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (mList[position].viewType) {
            VIEW_TYPE_ONE -> {
                (holder as View1Holder).bind(position)
            }
            VIEW_TYPE_TWO -> {
                (holder as View2Holder).bind(position)
            }
            VIEW_TYPE_THREE -> {
                (holder as View3Holder).bind(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun getMetaData(): List<FieldMetaData?> {
        return metaData.values.toList()
    }

    override fun getItemViewType(position: Int): Int {
        return mList[position].viewType
    }

    inner class View1Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {

            val itemsViewModel = mList[position]
            val editText = itemView.findViewById<TrackerEditText>(R.id.editText)
            editText.hint = itemsViewModel.text
            val md = editText.loadState(position.toLong(), metaData[position], itemsViewModel.text)
            metaData[position] = md
            editText.tag = "IncludeContentTracking"
            editText.setFieldName(itemsViewModel.text)
            editText.setAction(listener)
        }

    }

    inner class View2Holder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        fun bind(position: Int) {
            val itemsViewModel = mList[position]
            val radioGroup = itemView.findViewById<TrackerRadioGroup>(R.id.radioGroup)
            val md = radioGroup.loadState(position.toLong(), metaData[position])
            metaData[position] = md
            radioGroup.setFieldName(itemsViewModel.text)
            radioGroup.setAction(listener)
        }
    }

    inner class View3Holder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        fun bind(position: Int) {
            val itemsViewModel = mList[position]
            val checkbox = itemView.findViewById<TrackerCheckBox>(R.id.checkbox)
            val md = checkbox.loadState(position.toLong(), metaData[position])
            metaData[position] = md
            checkbox.setFieldName(itemsViewModel.text)
            checkbox.setAction(listener)
            checkbox.text = itemsViewModel.text
        }
    }


}

data class ItemsViewModel(val viewType: Int, val text: String) {
}
