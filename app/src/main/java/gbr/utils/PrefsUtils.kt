package gbr.utils

import android.content.Context
import android.content.SharedPreferences
import gbr.utils.models.Preferencess

class PrefsUtils(context: Context) : Preferencess {
    private val prefsId = "ru.rubeg38.gbrNavigation"
    private val prefs: SharedPreferences = context.getSharedPreferences(prefsId, Context.MODE_PRIVATE)

    override var serverAddress: ArrayList<String>
        get() {
            val arrayList: ArrayList<String> = ArrayList()
            val string = prefs.getString("ip", "")
            for (w in string?.split(", ")!!) {
                if (w.isNotEmpty()) {
                    arrayList.add(w)
                }
            }
            return arrayList
        }
        set(value) {
            val editor = prefs.edit()
            editor.putString("ip", value.joinToString { it }).apply()
        }

    override var serverPort: Int
        get() = prefs.getInt("port", -1)
        set(value) {
            val editor = prefs.edit()
            editor.putInt("port", value).apply()
        }

    override var imei: String?
        get() = prefs.getString("imei", null)
        set(value) {
            val editor = prefs.edit()
            editor.putString("imei", value).apply()
        }

    override var fcmtoken: String?
        get() = prefs.getString("fcmToken", null)
        set(value) {
            val editor = prefs.edit()
            editor.putString("fcmToken", value).apply()
        }

    override var version:String?
        get() = prefs.getString("version",null)
        set(value) {
            val editor = prefs.edit()
            editor.putString("version",value).apply()
        }

    override var login:String?
        get() = prefs.getString("login",null)
        set(value){
            val editor = prefs.edit()
            editor.putString("login",value).apply()
        }

    override var password:String?
        get() = prefs.getString("password",null)
        set(value){
            val editor = prefs.edit()
            editor.putString("password",value).apply()
        }

    override val containsAddress: Boolean
        get() = prefs.contains("ip")

    override val containsPort: Boolean
        get() = prefs.contains("port")

    override val containsImei: Boolean
        get() = prefs.contains("imei")

    override val containsFcmToken: Boolean
        get() = prefs.contains("fcmToken")

    override val containsLogin:Boolean
        get() = prefs.contains("login")

    override val containsPassword:Boolean
        get() = prefs.contains("password")
}