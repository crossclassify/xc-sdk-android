package com.crossclassify.trackersdk.data.model
// Data collected from the form
class FormMetaData(
    //formView id
    val fa_vid: String,
    //pageView id
    var pv_id: String,
    //form id
    val fa_id: String,
    //first interacted field
    val fa_ef: String?,
    //last interacted field
    val fa_lf: String?,
    //total time spent
    val fa_ts: Long,
    //hesitation time
    var fa_ht: Long?,
    //form first view
    val fa_st: Int,
    //time to first submit
    val fa_tts: Long?,
    //form submitted
    val fa_su: Int,
    //fields metadata
    var fieldsMetaData: List<FieldMetaData>,
    //device id
    val _id: String?,
    // fingerprint
    val uid:String?,
    //site id
    val id_site: Int,
    //page resolution
    var resolustion: String?
) {

    var ca: Int = 1
    var rec: Int = 1
    var r: Int = 44152
    //hour
    var h: Int = 44
    //minutes
    var m: Int = 44
    //seconds
    var s: Int = 44
    var _idn: Int = 0
    var _refts: Int = 0
    var sendImage: Int = 0
    var pdf: Int = 0
    var qt: Int = 0
    var realp: Int = 0
    var wma: Int = 0
    var fla: Int = 0
    var java: Int = 0
    var ag: Int = 0
    var cookie: Int = 0
    var url: String = ""
}