package com.crossclassify.examlpeapp.simpleUsageExample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.crossclassify.examlpeapp.R
import com.crossclassify.trackersdk.ScreenNavigationTracking
import com.crossclassify.trackersdk.TrackerFragment
import com.crossclassify.trackersdk.model.FieldMetaData
import kotlinx.android.synthetic.main.fragment_signup.*

//extend from TrackerFragment if you have form and need form content and behavior analysis in fragment
//override setFormName and define a name for your form
class SignUpFragment : TrackerFragment() {
    override fun getFormName(): String = "your-form-name"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //set onClickListener for your submit button and call trackerClickSubmitButton()
        btnSubmitFragment.setOnClickListener {
            trackerClickSubmitButton()
            Toast.makeText(
                activity,
                "Thanks for signing up. Welcome to our community.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getExternalMetaData(): List<FieldMetaData>? {
        return null
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