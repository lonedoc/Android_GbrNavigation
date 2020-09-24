package gbr.utils.servicess

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import androidx.core.content.ContextCompat.startActivity

import android.net.Uri;
import android.util.Base64;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;


class YandexNavigator {
    private val PRIVATE_KEY: String = "MIIBPQIBAAJBAMtE4ilZBK1K3lWn0NzOxik2e+QzrGkuk/gy3b93mBywcOVk2rqg"+
    "x1CeV5vlBkIhA8rZrnKZs37ME+4c+RTELAECAwEAAQJBAIPa2xoXNIF032SQx1t0"+
    "hfaV8SfGnUBdhn6qAE4DNhjk8uJv1Gwja4j1DTB1izvk03ojlSYiyS/8sO80xhht"+
    "cAECIQDx2CBxjtAotXSKpmOAEVb3aUc+/wMAfbAawNVcPAN90QIhANcqu9e1QhKh"+
    "53muGD2I2iR5hT1jQgMmdRdDNoblWmcxAiEAseDgRAXhrv9/v3cQWiLmz0T85SXV"+
    "xGetvo+0nol5m/ECIQC9bjAGTtwmz0edRvqsYim7Zwk47IQXheocWt5MkOuwYQIh"+
    "ANdFEe1xQyJoxyy3F7I7pk68qFnV9yTQtjUWHeEbYMSQ"

    // Формирует подпись с помощью ключа.
    @Throws(SecurityException::class)
    fun sha256rsa(key: String?, data: String): String? {
        val trimmedKey = key!!.replace(PRIVATE_KEY.toRegex(), "")
            .replace("\\s".toRegex(), "")
        return try {
            val result: ByteArray = Base64.decode(trimmedKey, Base64.DEFAULT)
            val factory: KeyFactory = KeyFactory.getInstance("RSA")
            val keySpec: EncodedKeySpec = PKCS8EncodedKeySpec(result)
            val signature: Signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(factory.generatePrivate(keySpec))
            signature.update(data.toByteArray())
            val encrypted: ByteArray = signature.sign()
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw SecurityException("Error calculating cipher data. SIC!")
        }
    }

    // Формирует URI с подписью и запускает Яндекс.Навигатор.
    fun buildRoute(context: Context,lat:String,lon:String) {
        try {
            var uri: Uri = Uri.parse("yandexnavi://build_route_on_map").buildUpon()
                .appendQueryParameter("lat_to", "$lat")
                .appendQueryParameter("lon_to", "$lon")
                .appendQueryParameter("client", "248").build()
            uri = uri.buildUpon()
                .appendQueryParameter("signature", sha256rsa(PRIVATE_KEY, uri.toString()))
                .build()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("ru.yandex.yandexnavi")
            val packageManager = context.packageManager
            val activities:List<ResolveInfo> = packageManager.queryIntentActivities(intent,0)
            val isIntentSafe = activities.isNotEmpty()
            if(isIntentSafe)
            {
                context.startActivity(intent)
            }
            else {
                val playMarket = Intent(Intent.ACTION_VIEW)
                playMarket.data = Uri.parse("market://details?id=ru.yandex.yandexnavi")
                context.startActivity(playMarket)
            }
        }catch (e:java.lang.Exception){
            val playMarket = Intent(Intent.ACTION_VIEW)
            playMarket.data = Uri.parse("market://details?id=ru.yandex.yandexnavi")
            context.startActivity(playMarket)
            e.printStackTrace()
        }

    }
}