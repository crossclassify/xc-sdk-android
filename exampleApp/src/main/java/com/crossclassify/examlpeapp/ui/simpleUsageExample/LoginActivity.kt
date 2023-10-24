package com.crossclassify.examlpeapp.ui.simpleUsageExample

import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.crossclassify.examlpeapp.R
import com.crossclassify.examlpeapp.ui.defaultRecyclerViewExample.RecyclerActivity
import com.crossclassify.examlpeapp.ui.commonEpoxyExample.Epoxy2Activity
import com.crossclassify.examlpeapp.ui.defaultEpoxyWithControllerExample.EpoxyActivity
import com.crossclassify.trackersdk.data.model.FieldMetaData
import com.crossclassify.trackersdk.utils.ScreenNavigationTracking
import com.crossclassify.trackersdk.utils.base.TrackerActivity
//import kotlinx.android.synthetic.main.activity_login.*

//extend from TrackerActivity if you have form in activity and need form content and behavior analysis
class LoginActivity : TrackerActivity() {
    //override getFormName and define a name for your form
    override fun getFormName(): String = "signup-home"
    //use it in case that you have recyclerView
    override fun getExternalMetaData(): List<FieldMetaData>? {
        return null
    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //set onClickListener for your submit button and call trackerClickSubmitButton
        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            trackerClickSubmitButton()
            Toast.makeText(this, "You are successfully logged in", Toast.LENGTH_SHORT).show()
            clearSubmittedData()
        }

        findViewById<TextView>(R.id.txtRegister).setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.goToRecycler).setOnClickListener {
            val intent = Intent(this, RecyclerActivity::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.goToEpoxy).setOnClickListener {
            val intent = Intent(this, EpoxyActivity::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.goToEpoxy2).setOnClickListener {
            val intent = Intent(this, Epoxy2Activity::class.java)
            startActivity(intent)
        }
    }

    fun clearSubmittedData(){
        findViewById<EditText>(R.id.editTextEmail).setText("")
        findViewById<EditText>(R.id.editTextPassword).setText("")
    }


    override fun onResume() {
        super.onResume()
        //screen navigation tracking
        //pass activity path and title to trackNavigation method for screen navigation purposes
        ScreenNavigationTracking().trackNavigation("/activity_splash/activity_login", "Login")
    }


}



