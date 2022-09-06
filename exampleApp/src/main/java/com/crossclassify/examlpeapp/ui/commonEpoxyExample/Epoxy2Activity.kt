package com.crossclassify.examlpeapp.ui.commonEpoxyExample

import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.crossclassify.examlpeapp.R
import com.crossclassify.trackersdk.data.model.FieldMetaData
import com.crossclassify.trackersdk.utils.ScreenNavigationTracking
import com.crossclassify.trackersdk.utils.base.TrackerActivity

class Epoxy2Activity : TrackerActivity(), EpoxyPassItems {
    private lateinit var epoxyList: EpoxyRecyclerView
    private val epoxyListMetadata = HashMap<Long, FieldMetaData>()
    private lateinit var controller:EpoxyController

    override fun getFormName(): String = "epoxy2-rv-list"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_epoxy2)
        epoxyList = findViewById(R.id.epoxy_rv)
        epoxyList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        epoxyList.withModels {
            controller = this
            listOf(0L, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10).forEach {
                EpoxyItemModel(it, this@Epoxy2Activity, "item $it", this@Epoxy2Activity).id(it)
                    .addTo(this)
            }
        }
        findViewById<Button>(R.id.btn).setOnClickListener {
            trackerClickSubmitButton()
            clearData(editTexts = true, checkBox = true, radioButtons = true)
            epoxyList.setControllerAndBuildModels(controller)
        }
    }

    override fun getExternalMetaData(): List<FieldMetaData> {
        return epoxyListMetadata.values.toList()
    }

    override fun passMetaData(id: Long, fieldMetaData: FieldMetaData?) {
        fieldMetaData?.let {
            epoxyListMetadata[id] = it
        }
    }

    override fun getMetaData(id: Long): FieldMetaData? {
        return epoxyListMetadata[id]
    }

    override fun onResume() {
        super.onResume()
        ScreenNavigationTracking().trackNavigation("/activity_epoxy", "Epoxy2Activity")
    }
}