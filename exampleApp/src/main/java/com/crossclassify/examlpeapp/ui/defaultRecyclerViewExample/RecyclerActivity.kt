package com.crossclassify.examlpeapp.ui.defaultRecyclerViewExample

import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crossclassify.examlpeapp.R
import com.crossclassify.trackersdk.data.model.FieldMetaData
import com.crossclassify.trackersdk.utils.ScreenNavigationTracking
import com.crossclassify.trackersdk.utils.base.TrackerActivity

class RecyclerActivity : TrackerActivity() {
    private lateinit var adapter: RecyclerViewAdapter
    override fun getFormName(): String {
        return "signup-in-recycler"
    }

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)


        recyclerView = findViewById<RecyclerView>(R.id.testRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val data = ArrayList<ItemsViewModel>()
        for (i in 0..20) {
            data.add(ItemsViewModel(i % 3, "Item $i"))
        }

        adapter = RecyclerViewAdapter(data, this)
        recyclerView.adapter = adapter


        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            trackerClickSubmitButton()
            clearData(  editTexts = true,
                        checkBox=true,
                        radioButtons = true)
            adapter.notifyDataSetChanged()
        }
    }
    override fun getExternalMetaData(): List<FieldMetaData> {
        val data = adapter.getMetaData()
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
        ScreenNavigationTracking().trackNavigation(
            "/activity_default_recycler",
            "DefaultRecyclerView"
        )
    }
}
