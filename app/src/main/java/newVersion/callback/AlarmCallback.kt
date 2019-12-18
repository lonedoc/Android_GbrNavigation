package newVersion.callback

import newVersion.utils.Alarm

interface AlarmCallback {
    fun applyAlarm(alarm: Alarm)
}