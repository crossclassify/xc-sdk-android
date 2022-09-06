package com.crossclassify.trackersdk.utils.objects

import androidx.annotation.RestrictTo


object Values {

    //TODO: it doesn't work
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    var SITE_ID :Int =0
    internal set

    //this variable must be accessible outside library for our application
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    var CC_API :Int = 1
}