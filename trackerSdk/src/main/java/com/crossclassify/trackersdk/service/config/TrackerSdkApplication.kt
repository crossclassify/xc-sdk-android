package com.crossclassify.trackersdk.service.config

import android.app.Application
import com.crossclassify.trackersdk.utils.objects.Values
import com.fingerprintjs.android.fingerprint.Configuration
import com.fingerprintjs.android.fingerprint.Fingerprinter
import com.fingerprintjs.android.fingerprint.FingerprinterFactory
import org.matomo.sdk.Matomo
import org.matomo.sdk.TrackMe
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.DimensionQueue
import org.matomo.sdk.extra.DownloadTracker
import org.matomo.sdk.extra.TrackHelper
import timber.log.Timber

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
    }
    private var mMatomoTracker: Tracker? = null
    private var mDimensionQueue: DimensionQueue? = null
    private var mSiteId: Int = 0

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
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
        if (mMatomoTracker == null) {
            mMatomoTracker = onCreateTrackerConfig().build(getMatomo()).apply {

                val fingerprint: Fingerprinter = FingerprinterFactory
                    .getInstance(applicationContext, Configuration(version = 3))

                fingerprint.getFingerprint {
                    userId=it.fingerprint
                }

                fingerprint.getDeviceId { result ->
                    visitorId = result.deviceId
                    val sharedPreferences = getSharedPreferences(
                        "org.matomo.sdk_FE8DB41078DFFC3D9751687595C3B837",
                        MODE_PRIVATE
                    )
                    sharedPreferences.edit().putString("tracker.visitorid", visitorId).apply()
                    sharedPreferences.edit().putString("tracker.fingerprint",userId).apply()
                }
            }
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
        if (mMatomoTracker != null) mMatomoTracker!!.dispatch()
        super.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        if ((level == TRIM_MEMORY_UI_HIDDEN || level == TRIM_MEMORY_COMPLETE) && mMatomoTracker != null) {
            mMatomoTracker!!.dispatch()
        }
        super.onTrimMemory(level)
    }

    open fun createDefaultConfig(siteId: Int) {
        this.mSiteId = siteId
        Values.SITE_ID=siteId
    }

    /** Initialize Matomo EndPoint **/
    private fun onCreateTrackerConfig(): TrackerBuilder {
        return TrackerBuilder.createDefault(
            "https://7afy3zglhe.execute-api.ap-southeast-2.amazonaws.com/matomo.php",
            this.mSiteId
        )
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