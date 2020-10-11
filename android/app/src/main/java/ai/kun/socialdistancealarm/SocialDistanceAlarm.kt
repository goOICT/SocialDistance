package ai.kun.socialdistancealarm

import ai.kun.opentracesdk_fat.BLETrace
import android.app.Application

/**
 * We want to make sure that the library is started any time the application is started, so we
 * override the default application with one that tries to start the underling lib.
 */
class SocialDistanceAlarm : Application() {
    override fun onCreate() {
        super.onCreate()
        BLETrace.init(applicationContext!!)
        BLETrace.start(true)
    }
}