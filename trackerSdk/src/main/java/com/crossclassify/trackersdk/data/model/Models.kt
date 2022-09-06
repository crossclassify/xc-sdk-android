package com.crossclassify.trackersdk.data.model

import com.crossclassify.trackersdk.interfaces.local.TrackerActions

data class FocusTimed(
    var focus: Long,
    var unFocus: Long,
    var changing: Boolean
)

// Data collected from the field
data class FieldMetaData(
    //total time spent
    var fa_fts: Long,
    //hesitation time
    var fa_fht: Long?,
    //left blank
    var fa_fb: Boolean?,
    //name
    var fa_fn: String?,
    //number of changes
    var fa_fch: Int?,
    //number of focus
    var fa_ff: Int?,
    //number of deletes
    var fa_fd: Int?,
    //number of cursor
    var fa_cu: Int?,
    //type
    var fa_ft: String? = null,
    //size
    var fa_fs: Int?,
    //content --> in case that you use content tracking tag for field
    var fa_cn: String? = null,
    var firstFocusTime: Long?,
    var lastFocusChangeTime: Long?,
    var changeAfterCreateMetaData: Boolean
)

data class SaveStateField(
    val text: String? = null,
    val focusList: ArrayList<FocusTimed>,
    val deleteButtonClick: Int = 0,
    val changeAfterCreateMetaData: Boolean = false,
    val changes: Int = 0,
    val focus: Boolean = false,
    val changing: Boolean = false,
    val eventChanging: Boolean = true,
    val lastChangeTime: Long = 0L,
    val firstHesitation: Long = 0L,
    val action: TrackerActions? = null,
    val fieldName: String? = null,
    val checked: Boolean? = null,
    val checkId: Int? = null,
    var reset: Boolean = false
)