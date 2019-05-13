package kobramob.rubeg38.ru.gbrnavigation.resource

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

@SuppressLint("StaticFieldLeak")
object SharedPreferencesState {
    private val STORAGE_NAME = "state"

    private var settings: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    @SuppressLint("StaticFieldLeak")
    private var context: Context? = null

    fun init(context: Context) {
        SharedPreferencesState.context = context
    }

    @SuppressLint("CommitPrefEdits")
    private fun init() {
        settings = context!!.getSharedPreferences(
            STORAGE_NAME, Context.MODE_PRIVATE)
        editor = settings!!.edit()
    }

    fun addPropertyString(name: String, value: String) {
        if (settings == null) {
            init()
        }
        editor!!.putString(name, value)
        editor!!.apply()
    }
    fun addPropertyFloat(name: String, value: Float) {
        if (settings == null) {
            init()
        }
        editor!!.putFloat(name, value)
        editor!!.apply()
    }
    fun addPropertyBoolean(name: String, value: Boolean) {
        if (settings == null) {
            init()
        }
        editor!!.putBoolean(name, value)
        editor!!.apply()
    }
    fun addPropertyInt(name: String, value: Int) {
        if (settings == null) {
            init()
        }
        editor!!.putInt(name, value)
        editor!!.apply()
    }
}
