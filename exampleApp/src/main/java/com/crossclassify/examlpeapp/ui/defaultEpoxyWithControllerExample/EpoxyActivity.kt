package com.crossclassify.examlpeapp.ui.defaultEpoxyWithControllerExample


import android.os.Bundle
import com.crossclassify.examlpeapp.R
import com.crossclassify.trackersdk.data.model.FieldMetaData
import com.crossclassify.trackersdk.utils.ScreenNavigationTracking
import com.crossclassify.trackersdk.utils.base.TrackerActivity
import kotlinx.android.synthetic.main.activity_epoxy.*


class EpoxyActivity : TrackerActivity() {
    private var controller = EpoxyController(this)
    private val list= ArrayList<String>()
    override fun getFormName(): String = "signup-in-epoxy1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_epoxy)

        for(i in 0..20){
            list.add(i.toString())
        }

        epoxy_rv.adapter = controller.adapter
        controller.requestModelBuild()
        controller.submit(list)

        btn.setOnClickListener {
            trackerClickSubmitButton()
            clearData(editTexts=true, radioButtons = true, checkBox = true)
            controller= EpoxyController(this)
            epoxy_rv.adapter = controller.adapter
            controller.submit(list)

        }


    }

    override fun getExternalMetaData(): List<FieldMetaData> {
        val data = controller.getMetaData()
        val result = ArrayList<FieldMetaData>()
        for (metaData in data) {
            metaData?.let {
                result.add(metaData)
            }
        }
        return result
    }

    override fun onResume() {
        super.onResume()
        ScreenNavigationTracking().trackNavigation("/activity_epoxy","EpoxyRecyclerView")
    }

}


