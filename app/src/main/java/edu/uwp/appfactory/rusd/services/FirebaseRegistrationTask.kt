package edu.uwp.appfactory.rusd.services

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpPatch
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.google.gson.reflect.TypeToken
import edu.uwp.appfactory.rusd.RUSDApplication
import edu.uwp.appfactory.rusd.data.local.RealmUserRepository
import edu.uwp.appfactory.rusd.data.model.User
import org.json.JSONObject

/**
 * Created by Jeremiah on 8/22/17.
 *
 * AsyncTask for uploading a users firebase registration token
 */
class FirebaseRegistrationTask: AsyncTask<Any, Void, Void>() {

    private val TAG = "FirebaseRegistration"
    private var retried = false

    override fun doInBackground(vararg params: Any?): Void? {
        val context = params[0] as Context
        val registrationToken = params[1]
        Log.d(TAG, "token: " + registrationToken)
        val registrationJSON = JSONObject(mapOf("registrationToken" to registrationToken))
        val (_, response, result) = "/user".httpPatch().body(registrationJSON.toString()).responseJson()
        when (result) {
            is Result.Failure -> {
                Log.e(TAG, "ERROR: " + result.getAs())
                Log.e(TAG, " Response Code: " + response.statusCode)
                if (!retried && response.statusCode == 401 &&
                        RUSDApplication.shouldRetryRequest(context)) {
                    retried = true
                    return doInBackground(params[0])
                }
            }

            is Result.Success -> {
                // parse user from response
                val userJson = RUSDApplication.convertUserLocations(result.get().obj().getJSONObject("data"))
                Log.d(TAG, "SUCCESS: " + userJson.toString())
                val tokenTypeUser = object : TypeToken<User>() {}.type
                val user = RUSDApplication.getGson().fromJson<User>(userJson.toString(), tokenTypeUser)

                // save user data
                val userRepository = RealmUserRepository(context)
                userRepository.setUser(user)
                userRepository.close()
            }
        }

        return null
    }
}