package com.crossclassify.examlpeapp.ui.defaultEpoxyWithControllerExample

import com.airbnb.epoxy.EpoxyController
import com.crossclassify.trackersdk.data.model.FieldMetaData
import com.crossclassify.trackersdk.interfaces.local.TrackerActions


class EpoxyController (private val listener: TrackerActions): EpoxyController() {

    private var _users = ArrayList<String>()
    private val metaData: HashMap<Int, FieldMetaData?> = HashMap()

    fun getMetaData(): List<FieldMetaData?> {
        return metaData.values.toList()
    }

    override fun buildModels() {
        _users.forEachIndexed { index, s ->
            epoxy {
                id(index)
                title(index.toString())
                value(this@EpoxyController._users[index])
                position(index)
                listener(this@EpoxyController.listener)
                fieldMetaData(this@EpoxyController.metaData[index])
                updateMetaData { fieldMetaData -> this@EpoxyController.metaData[index] = fieldMetaData }
            }
        }
    }

    fun submit(items: List<String>) {
        _users = ArrayList(items)
        requestModelBuild()
    }
}