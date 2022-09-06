package com.crossclassify.examlpeapp.ui.simpleUsageExample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crossclassify.examlpeapp.R
import com.crossclassify.examlpeapp.ui.defaultRecyclerViewExample.ItemsViewModel
import com.crossclassify.examlpeapp.ui.defaultRecyclerViewExample.RecyclerViewAdapter
import com.crossclassify.trackersdk.data.model.FieldMetaData
import com.crossclassify.trackersdk.utils.ScreenNavigationTracking
import com.crossclassify.trackersdk.utils.base.TrackerFragment

//extend from TrackerFragment if you have form and need form content and behavior analysis in fragment
//override setFormName and define a name for your form
class RecyclerFragment : TrackerFragment() {
    override fun getFormName(): String = "fragment"
    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recycler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = requireView().findViewById<RecyclerView>(R.id.testRecyclerfragment)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        val data = ArrayList<ItemsViewModel>()
        for (i in 0..20) {
            data.add(ItemsViewModel(i % 3, "Item $i"))
        }

        adapter = RecyclerViewAdapter(data, this)
        recyclerView.adapter = adapter


        requireView().findViewById<Button>(R.id.btnSubmitfragment).setOnClickListener {
            trackerClickSubmitButton()
            clearData(  editTexts = true,
                checkBox = true,
                radioButtons = true,)
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
        //screen navigation tracking
        //pass fragment path and title to trackNavigation method for screen navigation purposes
        ScreenNavigationTracking().trackNavigation(
            "/activity_login/activity_signup/fragment_signup",
            "SignUp"
        )
    }
}