package newVersion.callback

import newVersion.utils.Alarm

interface CommonCallback {
    fun applyAlarm(alarm: Alarm)
}