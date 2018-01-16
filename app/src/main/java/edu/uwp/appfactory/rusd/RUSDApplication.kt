package edu.uwp.appfactory.rusd

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import edu.uwp.appfactory.rusd.data.model.RealmString
import edu.uwp.appfactory.rusd.ui.login.LoginActivity
import edu.uwp.appfactory.rusd.ui.main.MainActivity
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmList
import io.realm.RealmObject
import org.json.JSONObject
import java.io.IOException
import java.util.*
import org.json.JSONArray


/**
 * Created by Jeremiah on 7/7/17.
 *
 * Application class used for Application level operations
 */
class RUSDApplication : Application() {

    val TAG = "MyApplication"

    override fun onCreate() {
        super.onCreate()
        // init realm and set default config to delete realm if migration is needed
        Log.d(TAG, "Init Realm")
        Realm.init(this)
        val config = RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build()
        Realm.setDefaultConfiguration(config)

        // init fuel here for the application
        val fuel = FuelManager.instance

        // API base url
        fuel.basePath = "https://isapp.rusd.org/api"

        // add base headers here
        fuel.baseHeaders = mapOf("Content-Type" to "application/json")


        // used for debugging and auth
        fuel.addRequestInterceptor { next: (Request) -> Request ->
            { req: Request ->
                val token = getAuthToken(this, req)
                if (token != null) {
                    req.header(mapOf("Authorization" to "Bearer $token"))
                }

                Log.d("FUEL REQUEST", req.toString())
                next(req)
            }
        }

        fuel.addResponseInterceptor  { next: (Request, Response) -> Response ->
            { req: Request, res: Response ->
                Log.d("FUEL RESPONSE", res.toString())
                next(req, res)
            }
        }
    }

    companion object {

        private val TAG = "Authenticator"

        /**
         * Interceptor method for getting auth token do not call from main thread!!!
         */
        @JvmStatic
        @Synchronized
        private fun getAuthToken(context: Context, request: Request): String? {
            val authToken = context.getSharedPreferences("prefs", 0).getString("authToken", null)
            val expiresAt = getExpiresAt(context)
            val refreshToken = getRefreshToken(context)

            // auth token is invalid or we are refreshing the token
            if (authToken == null || refreshToken == null || expiresAt == 0L
                    || request.path == "/auth/refresh") {
                return null

            // auth token needs to be refreshed
            } else if (expiresAt < Date().time) {
                return sendRefreshToken(context, refreshToken)

            // auth token should be valid return it
            } else {
                return authToken
            }
        }

        @JvmStatic
        @Synchronized
        private fun sendRefreshToken(context: Context, refreshToken: String?): String? {
            val refreshTokenJson = JSONObject(mapOf("refresh_token" to refreshToken))
            val (_, response, result) = "/auth/refresh".httpPost().body(refreshTokenJson.toString())
                    .responseJson()
            when(result) {
                is Result.Failure -> {
                    Log.e(TAG, "ERROR: " + result.getAs())
                    Log.e(TAG, " Response Code: " + response.statusCode)
                    if (response.statusCode == 401 || response.statusCode == 500) {
                        // log user out refresh failed
                        logout(context)
                    }
                    return null
                }
                is Result.Success -> {
                    val authJson = result.get().obj()
                    Log.d(TAG, "SUCCESS: " + authJson.toString())

                    RUSDApplication.setAuthToken(context, authJson.getString("access_token"))
                    RUSDApplication.setExpiresAt(context, authJson.getLong("expires_in"))

                    return authJson.getString("access_token")
                }
            }
        }

        @JvmStatic
        private fun getExpiresAt(context: Context): Long {
            return context.getSharedPreferences("prefs", 0).getLong("expiresAt", 0)
        }

        @JvmStatic
        private fun getRefreshToken(context: Context): String? {
            return context.getSharedPreferences("prefs", 0).getString("refreshToken", null)
        }

        @JvmStatic
        @Synchronized
        fun shouldRetryRequest(context: Context): Boolean {
            val refreshToken = getRefreshToken(context)
            if (sendRefreshToken(context, refreshToken) != null) {
                return true
            }
            return false
        }

        @JvmStatic
        fun setAuthToken(context: Context, token: String?) {
            val prefs = context.getSharedPreferences("prefs", 0)
            prefs.edit().putString("authToken", token).apply()
        }

        @JvmStatic
        fun setExpiresAt(context: Context, expiresIn: Long) {
            val expiresAt = Date().time + (expiresIn * 1000)
            val prefs = context.getSharedPreferences("prefs", 0)
            prefs.edit().putLong("expiresAt", expiresAt).apply()
        }

        @JvmStatic
        fun setRefreshToken(context: Context, token: String) {
            val prefs = context.getSharedPreferences("prefs", 0)
            prefs.edit().putString("refreshToken", token).apply()
        }

        @JvmStatic
        fun setNeedsSubscriptionsReset(context: Context, needsReset: Boolean) {
            val prefs = context.getSharedPreferences("prefs", 0)
            prefs.edit().putBoolean("needsSubscriptionsReset", needsReset).apply()
        }

        @JvmStatic
        fun getNeedsSubscriptionsReset(context: Context): Boolean {
            return context.getSharedPreferences("prefs", 0).getBoolean("needsSubscriptionsReset", true)
        }

        @JvmStatic
        fun logout(context: Context) {
            val prefs = context.getSharedPreferences("prefs", 0)
            prefs.edit().putString("authToken", null).apply()
            prefs.edit().putLong("expiresAt", 0).apply()
            prefs.edit().putString("refreshToken", null).apply()
            setNeedsSubscriptionsReset(context, true)

            // Start Login Activity
            if (context is MainActivity) {
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
                context.finish()
            }
        }

        @JvmStatic
        fun getGson() : Gson {
            val gson = GsonBuilder()
                    .setExclusionStrategies(object : ExclusionStrategy {
                        override fun shouldSkipField(f: FieldAttributes): Boolean {
                            return f.declaringClass == RealmObject::class.java
                        }

                        override fun shouldSkipClass(clazz: Class<*>): Boolean {
                            return false
                        }
                    })
                    .registerTypeAdapter(object : TypeToken<RealmList<RealmString>>() {

                    }.type, object : TypeAdapter<RealmList<RealmString>>() {

                        @Throws(IOException::class)
                        override fun write(out: JsonWriter, value: RealmList<RealmString>) {
                            // Ignore
                        }

                        @Throws(IOException::class)
                        override fun read(`in`: JsonReader): RealmList<RealmString> {
                            val list = RealmList<RealmString>()
                            `in`.beginArray()
                            while (`in`.hasNext()) {
                                try {
                                    list.add(RealmString(`in`.nextString()))
                                } catch (e: Exception) {
                                    list.add(RealmString())
                                    `in`.skipValue()
                                    Log.e("GSON", e.message)
                                    break
                                }
                            }
                            `in`.endArray()
                            return list
                        }
                    })
                    .create()
            return gson
        }

        @JvmStatic
        fun convertDp(context: Context, dp: Float) : Float {
            val metrics = context.getResources().getDisplayMetrics()
            val fpixels = metrics.density * dp
             return fpixels + 0.5f
        }

        @JvmStatic
        fun convertUserLocations(userJson: JSONObject): JSONObject {
            Log.d(TAG, userJson.toString())
            val locations = userJson.getJSONArray("locations")
            userJson.remove("locations")
            val locationStrings = JSONArray()
            var i = 0
            while (i < locations.length()) {
                locationStrings.put((locations.get(i) as JSONObject).getString("_id"))
                i++
            }
            userJson.put("locations", locationStrings)
            return userJson
        }
    }
}