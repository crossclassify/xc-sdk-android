package com.crossclassify.trackersdk.utils.base

import android.app.Application
import android.util.Log
import com.crossclassify.trackersdk.utils.objects.Values
import com.fingerprintjs.android.fingerprint.Configuration
import com.fingerprintjs.android.fingerprint.Fingerprinter
import com.fingerprintjs.android.fingerprint.FingerprinterFactory
import com.fingerprintjs.android.fpjs_pro.FingerprintJSFactory
import org.matomo.sdk.Matomo
import org.matomo.sdk.TrackMe
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.DimensionQueue
import org.matomo.sdk.extra.DownloadTracker
import org.matomo.sdk.extra.TrackHelper
import org.matomo.sdk.tools.BuildInfo
import org.matomo.sdk.tools.PropertySource
import timber.log.Timber
import java.util.*

abstract class TrackerSdkApplication : Application() {
    companion object {
        /** Instance of the current application.  */
        private lateinit var instance: TrackerSdkApplication

        /**
         * Gets the application context.
         *
         * @return the application context
         */
        val myApp: TrackerSdkApplication
            get() = instance

        lateinit var userAgent :String
    }

    private var mMatomoTracker: Tracker? = null
    private var mDimensionQueue: DimensionQueue? = null
    private var mSiteId: Int = 0
    private var mMode: Int = 0

    init {
        instance = this
        Timber.tag("CrossClassify:")
            .i("You have successfully added TrackerSdkApplication to your application class.")

        Timber.tag("CrossClassify:")
            .i("Now you have to call createDefaultConfig() and pass siteId to it. ")
    }

    override fun onCreate() {
        super.onCreate()
        userAgent = getUserAgent()
        onInitTracker()
    }

    private fun getMatomo(): Matomo? {
        return Matomo.getInstance(this)
    }

    /**
     * Gives you an all purpose thread-safe persisted Tracker.
     *
     * @return a shared Tracker
     */
    @Synchronized
    fun getTracker(): Tracker? {
        if ((mMode != Values.CC_API) || mMatomoTracker == null) {
            mMatomoTracker = onCreateTrackerConfig().build(getMatomo()).apply {


                //TODO: PAID FINGERPRINT
                val factory = FingerprintJSFactory(applicationContext)
                val configuration = com.fingerprintjs.android.fpjs_pro.Configuration(
                    "nLvTePYiYEFERqTHoSZ7"
                )

                val fpjsClient = factory.createInstance(
                    configuration
                )
                val sharedPreferences = getSharedPreferences(
                    "org.matomo.sdk_FE8DB41078DFFC3D9751687595C3B837",
                    MODE_PRIVATE
                )
                val fingerprint: Fingerprinter = FingerprinterFactory
                    .getInstance(applicationContext, Configuration(version = 3))

                if (userId==null){
                    fpjsClient.getVisitorId {
                        userId = it.visitorId
                        Log.e("userId",userId)
                        sharedPreferences.edit().putString("tracker.fingerprint", userId).apply()
                    }
                }

                while(userId==null)
                {
                    Thread.sleep(1000)
                }
//                fingerprint.getFingerprint {
//                    userId = it.fingerprint
//                }
                fingerprint.getDeviceId { result ->
                    visitorId = result.deviceId
                    sharedPreferences.edit().putString("tracker.visitorid", visitorId).apply()
                }
            }

            mMode = Values.CC_API
        }
        return mMatomoTracker
    }

    /**
     * See [TrackerBuilder].
     * You may be interested in [TrackerBuilder.createDefault]
     *
     * @return the tracker configuration you want to use.
     */

    override fun onLowMemory() {
        if (mMatomoTracker != null) mMatomoTracker?.dispatch()
        super.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        if ((level == TRIM_MEMORY_UI_HIDDEN || level == TRIM_MEMORY_COMPLETE) && mMatomoTracker != null) {
            mMatomoTracker?.dispatch()
        }
        super.onTrimMemory(level)
    }

    open fun createDefaultConfig(siteId: Int) {
        this.mSiteId = siteId
        Values.SITE_ID = siteId

        Timber.tag("CrossClassify:")
            .i("You have successfully added siteId.")
    }

    /** Initialize Matomo EndPoint **/
    fun onCreateTrackerConfig(): TrackerBuilder {
        return TrackerBuilder.createDefault(
            "https://api.crossclassify.com/matomo/matomo.php",
            this.mSiteId
        )
    }

    open fun getUserAgent(): String {
        var mPropertySource = PropertySource()
        var mBuildInfo = BuildInfo()
        var httpAgent: String = mPropertySource.httpAgent!!
        if (httpAgent == null || httpAgent.startsWith("Apache-HttpClient/UNAVAILABLE (java")) {
            var dalvik: String = mPropertySource.jvmVersion!!
            if (dalvik == null) dalvik = "0.0.0"
            val android: String = mBuildInfo.release
            val model: String = mBuildInfo.model
            val build: String = mBuildInfo.buildId
            httpAgent = String.format(
                Locale.US,
                "Dalvik/%s (Linux; U; Android %s; %s Build/%s)",
                dalvik, android, model, build
            )
        }
        return httpAgent
    }

    /** Enable Timber For Logging **/
    private fun onInitTracker() {

        Timber.plant(Timber.DebugTree())
        TrackHelper.track().download().identifier(DownloadTracker.Extra.ApkChecksum(this))
            .with(getTracker())

        mDimensionQueue = DimensionQueue(getTracker())
        mDimensionQueue!!.add(0, "test")
        getTracker()?.addTrackingCallback { trackMe: TrackMe? ->
            Timber.i("Tracker.Callback.onTrack(%s)", trackMe)
            trackMe
        }

    }
    /**
     * Start time
     */
}