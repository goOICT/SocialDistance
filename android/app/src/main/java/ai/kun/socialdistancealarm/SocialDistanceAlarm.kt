package ai.kun.socialdistancealarm

import ai.kun.opentracesdk_fat.BLETrace
import android.app.Application

class SocialDistanceAlarm : Application() {
    override fun onCreate() {
        super.onCreate()
        BLETrace.init(applicationContext!!)
        BLETrace.start(true)
    }
}