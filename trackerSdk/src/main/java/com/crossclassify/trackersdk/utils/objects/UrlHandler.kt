package com.crossclassify.trackersdk.utils.objects

import com.crossclassify.trackersdk.data.model.FormMetaData
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

object UrlHandler {

    fun generateNewFormUrl(formMetaData: FormMetaData, formName: String): String {

        val calendar: Calendar = Calendar.getInstance()
        val date = iso8601Format()
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes: Int = calendar.get(Calendar.MINUTE)
        val seconds: Int = calendar.get(Calendar.SECOND)
        return "matomo.php?action_name=formAutoGenerate" +
                "&idsite=${formMetaData.id_site}&rec=1&r=511664&h=$hour&m=$minutes&s=$seconds&date=${date}" +
                "&url=$formName" +
                "&_id=${formMetaData._id}&_uid=${formMetaData.uid}&_idn=0&_refts=0&send_image=0&pdf=1&qt=0&realp=0&wma=0&fla=0&java=0&ag=0&cookie=1&res=${formMetaData.resolustion}&fa_pv=1" +
                "&fa_fp[0][fa_id]=$formName&fa_fp[0][fa_fv]=1&pf_net=0&pf_srv=4&pf_tfr=2&pf_dm1=63" +
                "&pv_id=${formMetaData.pv_id}&fa_fp[0][fa_vid]=${formMetaData.fa_vid}"
    }

    fun generateMetaDataUrl(formMetaData: FormMetaData): String {

        val fieldsAttr = generateFieldsUrl(formMetaData)
        val fields = URLEncoder.encode(
            fieldsAttr,
            "utf-8"
        )

        val calendar: Calendar = Calendar.getInstance()
        val date = iso8601Format()
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes: Int = calendar.get(Calendar.MINUTE)
        val seconds: Int = calendar.get(Calendar.SECOND)

        var path = "matomo.php?fa_vid=${formMetaData.fa_vid}" +
                "&fa_id=${formMetaData.fa_id}&fa_ef=${formMetaData.fa_ef}" +
                "&fa_lf=${formMetaData.fa_lf}&fa_fields=$fields&fa_ts=${formMetaData.fa_ts}&ca=${formMetaData.ca}" +
                "&idsite=${formMetaData.id_site}&rec=${formMetaData.rec}" +
                "&r=${formMetaData.r}&h=$hour&m=$minutes" +
                "&s=$seconds&date=${date}&url=${formMetaData.fa_id}" +
                "&_id=${formMetaData._id}&_uid=${formMetaData.uid}&_idn=${formMetaData._idn}&_refts=${formMetaData._refts}" +
                "&send_image=${formMetaData.sendImage}&pdf=${formMetaData.pdf}" +
                "&qt=${formMetaData.qt}&realp=${formMetaData.realp}&wma=${formMetaData.wma}" +
                "&fla=${formMetaData.fla}&java=${formMetaData.java}&ag=${formMetaData.ag}" +
                "&cookie=${formMetaData.cookie}&res=${formMetaData.resolustion}" +
                "&pv_id=${formMetaData.pv_id}"

        if (formMetaData.fa_tts != null) {
            path += "&fa_tts=${formMetaData.fa_tts}"
        }
        if (formMetaData.fa_ht != null) {
            path += "&fa_ht=${formMetaData.fa_ht}"
        }
        if (formMetaData.fa_st == 1) {
            path += "&fa_st=${formMetaData.fa_st}"
        }
        if (formMetaData.fa_su == 1) {
            path += "&fa_su=${formMetaData.fa_su}"

        }


        return path

    }


    private fun generateFieldsUrl(formMetaData: FormMetaData): String {

        var fieldsAttr = "["

        for ((i, field) in formMetaData.fieldsMetaData.withIndex()!!) {

            var str =
                "{\"fa_fts\":${field.fa_fts},\"fa_fht\":${field.fa_fht}," +
                        "\"fa_fb\":${field.fa_fb},\"fa_fn\":\"${field.fa_fn}\"," +
                        "\"fa_fch\":${field.fa_fch},\"fa_ff\":${field.fa_ff}," +
                        "\"fa_fd\":${field.fa_fd},\"fa_fcu\":${field.fa_cu}," +
                        "\"fa_ft\":\"${field.fa_ft}\",\"fa_fs\":${field.fa_fs}"

            str += if (field.fa_cn != null) {
                ",\"fa_cn\":\"${field.fa_cn}\"}"
            } else {
                "}"
            }
            if (i != formMetaData.fieldsMetaData.size - 1) {
                str += ","
            }

            fieldsAttr += str

        }

        fieldsAttr += "]"

        return fieldsAttr
    }

    private fun iso8601Format(): String {

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        var date = sdf.format(Date()).toString()
        val timeZone = TimeZone.getDefault().rawOffset
        val timeZoneMinutes = floor((timeZone / (1000 * 60) % 60).toDouble()).toInt()
        val timeZoneHours = floor((timeZone / (1000 * 60 * 60) % 24).toDouble()).toInt()

        date += if (timeZone > 0) {
            "+$timeZoneHours:$timeZoneMinutes"
        } else {
            "-$timeZoneHours:$timeZoneMinutes"
        }
        return date
    }

}