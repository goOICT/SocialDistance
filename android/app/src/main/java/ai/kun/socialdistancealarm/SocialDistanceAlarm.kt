package ai.kun.socialdistancealarm

import ai.kun.socialdistancealarm.util.NotificationUtils
import ai.kun.socialdistancealarm.alarm.BLETrace
import android.app.Application

class SocialDistanceAlarm : Application() {
    override fun onCreate() {
        super.onCreate()
        BLETrace.init(applicationContext!!)
        NotificationUtils.init(applicationContext!!)
    }
}