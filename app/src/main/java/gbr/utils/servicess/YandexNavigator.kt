package gbr.utils.servicess

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo

import android.net.Uri;
import android.util.Base64;
import java.lang.Exception

import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

private const val PRIVATE_KEY =
    "-----BEGIN RSA PRIVATE KEY-----\n" +
    "MIIBPQIBAAJBAMtE4ilZBK1K3lWn0NzOxik2e+QzrGkuk/gy3b93mBywcOVk2rqg\n" +
    "x1CeV5vlBkIhA8rZrnKZs37ME+4c+RTELAECAwEAAQJBAIPa2xoXNIF032SQx1t0\n" +
    "hfaV8SfGnUBdhn6qAE4DNhjk8uJv1Gwja4j1DTB1izvk03ojlSYiyS/8sO80xhht\n" +
    "cAECIQDx2CBxjtAotXSKpmOAEVb3aUc+/wMAfbAawNVcPAN90QIhANcqu9e1QhKh\n" +
    "53muGD2I2iR5hT1jQgMmdRdDNoblWmcxAiEAseDgRAXhrv9/v3cQWiLmz0T85SXV\n" +
    "xGetvo+0nol5m/ECIQC9bjAGTtwmz0edRvqsYim7Zwk47IQXheocWt5MkOuwYQIh\n" +
    "ANdFEe1xQyJoxyy3F7I7pk68qFnV9yTQtjUWHeEbYMSQ\n" +
    "-----END RSA PRIVATE KEY-----\n"

class YandexNavigator {

    fun sha256rsa(key: String, data: String): String? {
        val trimmedKey = key
            .replace(Regex("-----(\\w+ )*PRIVATE KEY-----"), "")
            .replace(Regex("\\s"), "")

        return try {
            val result = Base64.decode(trimmedKey, Base64.DEFAULT)
            val factory = KeyFactory.getInstance("RSA")
            val keySpec = PKCS8EncodedKeySpec(result)
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(factory.generatePrivate(keySpec))
            signature.update(data.toByteArray())

            val encrypted = signature.sign()
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (ex: Exception) {
            null
        }
    }

    fun buildRoute(context: Context, latitude: String, longitude: String) {
        var uri = Uri.parse("yandexnavi://build_route_on_map").buildUpon()
            .appendQueryParameter("lat_to", latitude)
            .appendQueryParameter("lon_to", longitude)
            .appendQueryParameter("client", "248")
            .build()

        sha256rsa(PRIVATE_KEY, uri.toString())?.let { signature ->
            uri = uri.buildUpon()
                .appendQueryParameter("signature", signature)
                .build()
        }

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("ru.yandex.yandexnavi")
        }

        val activitiesForIntent = context.packageManager.queryIntentActivities(intent, 0)

        if (activitiesForIntent.isNotEmpty()) {
            context.startActivity(intent)
        } else {
            openPlayMarketPage(context)
        }
    }

    private fun openPlayMarketPage(context: Context) {
        val playMarket = Intent(Intent.ACTION_VIEW)
        playMarket.data = Uri.parse("market://details?id=ru.yandex.yandexnavi")
        context.startActivity(playMarket)
    }

}