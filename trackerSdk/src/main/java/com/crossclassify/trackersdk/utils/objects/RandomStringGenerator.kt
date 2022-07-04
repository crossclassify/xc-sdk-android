package com.crossclassify.trackersdk.utils.objects

object RandomStringGenerator {
//random string generator for pageView id and formView id
    fun getRandomString(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..6)
            .map { allowedChars.random() }
            .joinToString("")
    }

}