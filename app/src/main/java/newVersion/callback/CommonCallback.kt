package newVersion.callback

import oldVersion.workservice.Alarm

interface CommonCallback {
    fun applyAlarm(alarm: Alarm)
}