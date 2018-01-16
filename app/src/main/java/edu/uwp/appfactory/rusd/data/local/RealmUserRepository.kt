package edu.uwp.appfactory.rusd.data.local

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.google.gson.reflect.TypeToken
import edu.uwp.appfactory.rusd.RUSDApplication
import edu.uwp.appfactory.rusd.data.model.User
import io.realm.Realm

/**
 * Created by dakota on 8/11/17.
 *
 *  Repository Class for storing and retrieving the user in a realm Database.
 */
class RealmUserRepository(val context: Context) : UserRepository {

    private val TAG = "User Repository"
    private var realm: Realm? = Realm.getDefaultInstance()

    override fun getCurrentUser() : User? {
        return realm?.where(User::class.java)?.findFirst()
    }

    override fun setUser(user: User) {
        if (realm != null) {
            realm?.executeTransaction {
                realm?.delete(User::class.java)
                realm?.copyToRealmOrUpdate(user)
            }

            Log.d(TAG, "User saved: " + user)

        } else {
            Log.e(TAG, "Realm is closed cannot add " + user.toString())
        }
    }

    override fun close() {
        if (realm != null) {
            realm?.close()
            realm = null
        }
    }

    private fun update(user: User) {
        if (realm != null) {
            val oldUser = getCurrentUser()
            if (oldUser != null && oldUser.id == user.id) {
                realm?.executeTransaction {
                    realm?.copyToRealmOrUpdate(user)
                }

                Log.d(TAG, "User updated: " + user)
            } else {
                setUser(user)
            }
        } else {
            Log.e(TAG, "Realm is closed cannot update " + user.toString())
        }
    }

    fun updateUser() {
        UpdateUser(this).execute(context)
    }

    class UpdateUser(val userRepository: RealmUserRepository) : AsyncTask<Context, Void, User?>() {

        private val TAG = "Update User"
        var retried = false

        override fun doInBackground(vararg params: Context?): User? {
            Log.d(TAG, "update started")
            var user: User? = null
            val (_, response, result) = "/user".httpGet().responseJson()

            when (result) {
                is Result.Failure -> {
                    Log.e(TAG, "ERROR: " + result.getAs())
                    Log.e(TAG, " Response Code: " + response.statusCode)
                    if (!retried && response.statusCode == 401 && params[0] != null &&
                            RUSDApplication.shouldRetryRequest(params[0]!!)) {
                        retried = true
                        return doInBackground(params[0])
                    }
                }

                is Result.Success -> {
                    Log.d(TAG, "SUCCESS: " + result.get().obj().toString())
                    val tokenType = object : TypeToken<User>() {}.type
                    val userJson = RUSDApplication.convertUserLocations(result.get().obj().getJSONObject("data"))
                    user = RUSDApplication.getGson().fromJson<User>(userJson.toString(), tokenType)
                    return user
                }
            }
            return user
        }

        override fun onPostExecute(result: User?) {
            super.onPostExecute(result)
            if (result != null) {
                userRepository.update(result)
                Log.d(TAG, "update successful")
            } else {
                Log.e(TAG, "update failed")
            }
        }
    }
}