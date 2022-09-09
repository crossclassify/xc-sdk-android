# matomo-android-sdk



## Getting started

To make it easy for you to get started with crossclassify SDK, here's a list of recommended next steps.

[Set SDK To Gradle](https://github.com/FatemeZahraFeyzi/cc-sdk-android-after-feedback/edit/master/README.md#Set-SDK-To-Gradle) \
[Set Base Class](https://github.com/FatemeZahraFeyzi/cc-sdk-android-after-feedback/edit/master/README.md#Set-Base-Class) \
[Set Screen Navigation Analysis](https://github.com/FatemeZahraFeyzi/cc-sdk-android-after-feedback/edit/master/README.md#Set-Screen-Navigation-Analysis) \
[Set Form Content and Behavior Analysis](https://github.com/FatemeZahraFeyzi/cc-sdk-android-after-feedback/edit/master/README.md#Set-Form-Content-and-Behavior-Analysis)

#### 1.Set SDK To Gradle
First, add this code on your `settings.gradle` and project `build.gradle` file.
```kotlin
repositories {
    maven { url 'https://jitpack.io' }
}
```
Then, add the code below to your app module `build.gradle` file.
```kotlin
defaultConfig {
        ...
        targetSdk 31
    }
    
dependencies {
    implementation 'com.github.lana2882:crossclassify:0.8'
}
```
Then,  press "Sync now" in the bar that appears in Android Studio:
![image](https://user-images.githubusercontent.com/69571791/156897183-48bf4849-54c0-4f8c-beec-f22434920e50.png)

:warning: **If you have duplicate library error**: add the code below to your `gradle.properties` and sync again.
```kotlin
android.enableJetifier=true
```

#### 2.Set Base Class
First, Create MyApplication class on separate file  
Then, you need to extend **TrackerSdkApplication** and override `onCreate()` method and pass your sideId like below.
```kotlin
class MyApplication : TrackerSdkApplication() {
    override fun onCreate() {
        //place your siteId here(siteId = 19)
        createDefaultConfig( <SITE-ID> )  //CHANGE BEFORE COMPILE
        super.onCreate()
    }
}
```
Don't forget to add the code below to your `AndroidManifest.xml`
```xml
<uses-permission android:name="android.permission.INTERNET" />
<application
...
    android:name=".MyApplication"
    android:usesCleartextTraffic="true">
```

#### 3.Set Screen Navigation Analysis

For [Screen Navigation Tracking](https://gitlab.com/abdal1/crossclassify/matomo-android-sdk/-/settings/integrations) override `onResume()` method of your class (Activity,Fragment, BottomSheetDialogFragment or DialogFragment) and add the code below.
```kotlin
override fun onResume() {
   super.onResume()
    //screen navigation tracking
    //pass class path and title to trackNavigation method.
   ScreenNavigationTracking().trackNavigation( </ACTIVITY_SAMPLE> ,<SAMPLE>) //CHANGE BEFORE COMPILE
}
```
#### 4.Set Form Content and Behavior Analysis
For the form that you need form content and behavior analysis in it, do the following.
##### XML Part:
- We have supported editText, radioGroup and checkBox for form fields.
- Use `TrackerEditText`, `TrackerRadioGroup` and `TrackerCheckBox` for your fields in XML file and set each `field name`, `radio_field_name` and `check_box_field_name` to fieldName that allows us to provide [Form Behavioral Tracking](https://gitlab.com/abdal1/crossclassify/matomo-android-sdk/-/settings/integrations).

:warning: **for field that contain email, field name must be "email" string in lowercase.**
```xml
    <!--CHANGE BEFORE COMPILE-->
    <com.crossclassify.trackersdk.utils.view.TrackerEditText
      app:fieldName= <YOUR_FIELD_NAME>
    />

    <com.crossclassify.trackersdk.utils.view.TrackerRadioGroup
        app:radio_field_name= <YOUR_FIELD_NAME>

        <RadioButton
            android:text= <YOUR_TEXT>
                     ...
            />

        <RadioButton
            android:text= <YOUR_TEXT>
                     ...
            />
    </com.crossclassify.trackersdk.utils.view.TrackerRadioGroup>

    <com.crossclassify.trackersdk.utils.view.TrackerCheckBox
      android:text= <YOUR_TEXT>
      app:check_box_field_name= <YOUR_FIELD_NAME>
    />
```
- Field contents are not captured unless you add IncludeContentTracking Tag, regardless to field type, for the field that you need [Field Content Tracking](https://gitlab.com/abdal1/crossclassify/matomo-android-sdk/-/settings/integrations).
```xml
<!--CHANGE BEFORE COMPILE--> 
<com.crossclassify.trackersdk.utils.view.TrackerEditText
    android:tag="IncludeContentTracking"                                         
    app:fieldName= <YOUR_FIELD_NAME>
   />
```
##### Kotlin Part:
- Depend on where you need to implement form
- Extend from `TrackerActivity`, `TrackerFragment`, `BottomSheetDialogFragment` or  `DialogFragment`
-  Override `getFormName()` method and define a name for your form
-  Override `getExternalMetaData()` (if you don't have recyclerview just return null).

:warning: **for sign up forms , your form name must contain "singnup" string without space.**
```kotlin
class MainActivity : TrackerActivity() {
    override fun getFormName(): String = <YOUR_FORM_NAME> //CHANGE BEFORE COMPILE
    override fun getExternalMetaData(): List<FieldMetaData>? = null
}
```
- Both behavioral and content tracking rely on setting `onclicklistener` for your submit button and call `trackerClickSubmitButton()` in the last line of it, because it will erase field contents. as below:
```kotlin
submitButton.setOnClickListener {
    ...
    ...
    ...
    trackerClickSubmitButton()
}
```
In case that you need recyclerview or epoxy recyclerview follow some more steps [here](https://github.com/FatemeZahraFeyzi/cc-sdk-android-after-feedback/edit/master/README.md#Form-Analytics-with-recyclerView).

**Here is a complete example for your reference**
<br/> activity_login.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <!--Go ahead and design your xml just don't forget these notes -->
    <!--Set fieldName for all form fields, no matter where they are -->
 
    <!--Remember by setting tag for field we have access to its data -->
    <com.crossclassify.trackersdk.utils.view.TrackerEditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:fieldName="email"
        android:tag="IncludeContentTracking"/>

    <!--Remember without tag we don't collect field data  -->
    <com.crossclassify.trackersdk.utils.view.TrackerEditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:fieldName="password"/>

    <!--Use onClickListener on your submit button  -->
    <Button
        android:id="@+id/btnSubmit"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        />

</LinearLayout>

```

LoginActivity.kt
```kotlin


/** extend from TrackerActivity if you have form in activity
and need form content and behavior analysis, then
override getFormName and define a name for your form **/
class LoginActivity : TrackerActivity() {
    override fun getFormName(): String = "login-form"
    override fun getExternalMetaData(): List<FieldMetaData>? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        /** set onClickListener for your submit button
        call trackerClickSubmitButton() **/
        btnSubmit.setOnClickListener {
            trackerClickSubmitButton()
        }
    }

    override fun onResume() {
        super.onResume()
        //screen navigation tracking
        /** pass activity path and title to trackNavigation method
        for screen navigation purposes **/
        ScreenNavigationTracking().trackNavigation(
            "/activity_splash/activity_login", "Login"
        )
    }

}
```
#### Form Analytics with recyclerView
More steps to integrate with recycler view.
##### Adapter
- Implement your `recyclerView Adapter` which should take the following listener as input parameter.
    - Listener: Used to send information when the user is in idle mode, and sent to views via the `setAction()` method.
```kotlin
class RecyclerViewAdapter(private val listener: TrackerActions) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {}
```
- In adapter go ahead and create a hashmap list.
    - Hashmap list: To store the form metadata, we need a collection that is filled through the `loadState()` method and finally sent to the SDK through the `getExternalMetaData()` method that has been overridden.
```kotlin
private val metaData: HashMap<Int, FieldMetaData?> = HashMap()
```
- In `onBindViewHolder()` go ahead and do the following for each field
    - Call `loadState()`, This method is used to bind the views and load their latest state, it takes the following three parameters as input then it returns updated metadata and you have to update hashmap list with this value to store the current metadata state of each field
        - id: This parameter is used to store the user's state while interacting with the form and must have a unique value in entire project
        - metadata: This parameter is used to update metadata, if metadata already exists we send it to the method, otherwise we send null value to it.
        - text: This parameter is for editText and is used to set the initial text
    - Call `setFieldName()` and pass a name for fields
    - Call `setAction()` and pass TrackerAction listener to it
    - In case that you need content tracking add IncludeContentTracking tag
```kotlin
override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    // for example for TrackerEditText
    val editText = holder.findViewById<TrackerEditText>(R.id.editText)
            val md = editText.loadState( <YOUR_UNIQUE_ID> , metaData[ <UNIQUE_ID> ], <YOUR_DEFAULT_TEXT> )  //CHANGE BEFORE COMPILE
            metaData[ <UNIQUE_ID> ] = md  //CHANGE BEFORE COMPILE
            editText.setFieldName( <YOUR_FIELD_NAME> )  //CHANGE BEFORE COMPILE
            editText.setAction(listener)
            editText.tag="IncludeContentTracking"
    }
```
- Define a method that returns this collected fields matadata(hashmap list) like below.
```kotlin
fun getMetaData(): List<FieldMetaData?> {
    return metaData.values.toList()
}
```
###### well done; recyclerview adapter is completed.
##### recyclerView Host
- Override `getExternalMetaData()`; You must send the field metadata to the SDK through this method, which first takes the metadata of the fields by calling `getMetaData()` from the adapter and then sends it to the SDK.
```kotlin
    override fun getExternalMetaData(): List<FieldMetaData>? {
        val data = adapter.getMetaData()
        val result = ArrayList<FieldMetaData>()
        for (metaData in data) {
            metaData?.let {
                result.add(metaData)
            }
        }
        return result
    }
```

#### Form Analytics With EpoxyRecyclerView
More steps to integrate with recycler view.
##### EpoxyModelClass
- Here is an idea for integration with EpoxyRecyclerView
- Create your model with these attributes
```kotlin
@EpoxyModelClass(layout = R.layout.user_list_item)
abstract class UserModel: EpoxyModelWithHolder<UserModel.Holder>() {
    // optional attributes
    @field:EpoxyAttribute var title: String? = null
    @field:EpoxyAttribute var value: String? = null
    
    // required attributes
    @field:EpoxyAttribute var position: Int = 0
    @field:EpoxyAttribute var fieldMetaData: FieldMetaData? = null
    @field:EpoxyAttribute var updateMetaData: (FieldMetaData?) -> Unit = {}
    @field:EpoxyAttribute var listener: TrackerActions? = null
 }
```
- Implement your Holder class as always
```kotlin
class Holder: EpoxyHolder() {
        
        var et: TrackerEditText? = null

        override fun bindView(itemView: View) {
            et = itemView.findViewById(R.id.et)
        }
  }
```
- In `bind()` go ahead and do the following for each field
    - Call `loadState()`, This method is used to bind the views and load their latest state, it takes the following three parameters as input then it returns updated metadata and you have to update hashmap list with this value to store the current metadata state of each field
        - id: This parameter is used to store the user's state while interacting with the form and must have a unique value in entire project
        - metadata: This parameter is used to update metadata, if metadata already exists we send it to the method, otherwise we send null value to it.
        - text: This parameter is for editText and is used to set the initial text
    - Call `setFieldName()` and pass a name for fields
    - Call `setAction()` and pass TrackerAction listener to it
    - In case that you need content tracking add IncludeContentTracking tag
```kotlin
override fun bind(holder: Holder) {
    val md = holder.et?.loadState( <YOUR_UNIQUE_ID> , fieldMetaData, null)  //CHANGE BEFORE COMPILE
    updateMetaData(md)
    holder.et?.setFieldName( <YOUR_FIELD_NAME> )  //CHANGE BEFORE COMPILE
    holder.et?.setAction(listener)
    holder.et?.tag="IncludeContentTracking"
}
```
#### Controller
- Implement your `controller` which should take the following listener as input parameter.
    - Listener: Used to send information when the user is in idle mode, and sent to views via the `setAction()` method.
```kotlin
class UserController (private val listener: TrackerActions): EpoxyController() {}
```
- In controller go ahead and create a hashmap list.
    - Hashmap list: To store the form metadata, we need a collection that is filled through the `loadState()` method and finally sent to the SDK through the `getExternalMetaData()` method that has been overridden.
```kotlin
private val metaData: HashMap<Int, FieldMetaData?> = HashMap()
```
- Define a method that returns this collected fields matadata(hashmap list) like below.
```kotlin
fun getMetaData(): List<FieldMetaData?> {
    return metaData.values.toList()
}
```
- In `buildModels()` method for each model object initialize attributes
```kotlin
    override fun buildModels() {
    _users.forEachIndexed { index, s ->
        user {
            id(index)
            title(index.toString())
            value(this@UserController._users[index])
            position(index)
            listener(this@UserController.listener)
            fieldMetaData(this@UserController.metaData[index])
            updateMetaData { fieldMetaData -> this@UserController.metaData[index] = fieldMetaData }
        }

    }
}
```
#### EpoxyRecyclerView Host
- Override `getExternalMetaData()`; You must send the field metadata to the SDK through this method, which first takes the metadata of the fields by calling `getMetaData()` from the controller and then sends it to the SDK.
```kotlin
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
```
- Check out the sample app that came with the library for more details on EpoxyRecyclerView
#### Clear Data
When you need to clear data from items in recyclerView or Epoxy use `clearData()` method and notify adapter that data has been changed.
- Reset all items, you can reset all of items by pass `true` to it as parameter.
```kotlin
clearData(editTexts=true, radioButtons = true, checkBox = true)
// notify changes in epoxy
controller.requestModelBuild()
// notify changes in recyclerview
adapter.notifyDataSetChanged()
```
- Just reset some of items by passing their id list as parameter; notice that the ids are unique in entire project
```kotlin
clearData(  editTexts = true,editTextIds = listIds,
    checkBox = true,checkBoxIds = listIds,
    radioButtons = true,radioIds = listIds)
// notify changes in epoxy
controller.requestModelBuild()
// notify changes in recyclerview
adapter.notifyDataSetChanged()
```
#### Done
Adding CrossClassify to your App Done successfuly.
Now we are waiting for the first data.

