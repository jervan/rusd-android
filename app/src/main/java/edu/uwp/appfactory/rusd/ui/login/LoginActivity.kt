package edu.uwp.appfactory.rusd.ui.login

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.iid.FirebaseInstanceId
import edu.uwp.appfactory.rusd.R
import edu.uwp.appfactory.rusd.RUSDApplication
import edu.uwp.appfactory.rusd.data.local.RealmUserRepository
import edu.uwp.appfactory.rusd.data.model.User
import edu.uwp.appfactory.rusd.services.FirebaseRegistrationTask
import edu.uwp.appfactory.rusd.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private val TAG = "Login Activity"
    private val GOOGLE_PLAY_SERVICES_REQUEST = 222

    private var isFirebaseAvailable: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        password_edit_text.setImeActionLabel("Login", EditorInfo.IME_ACTION_DONE)
        password_edit_text.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                onLoginClick()
            }
            false
        }

        login_button.setOnClickListener { onLoginClick() }
    }

    fun onLoginClick() {
        if (isFirebaseAvailable) {
            val loginJSON = JSONObject(mapOf("username" to username_edit_text.text.toString(), "password" to password_edit_text.text.toString()))

            "/auth".httpPost().body(loginJSON.toString()).responseJson { _, response, result ->
                when (result) {
                    is Result.Failure -> {
                        Log.e(TAG, "ERROR: " + result.getAs())
                        Log.e(TAG, " Response Code: " + response.statusCode)
                        if (response.statusCode == 401 || response.statusCode == 500) {
                            password_edit_text.setText("")
                            Toast.makeText(this, "Username/Password Incorrect", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Must connect to the internet to login", Toast.LENGTH_LONG).show()
                        }
                    }
                    is Result.Success -> {
                        val authJson = result.get().obj()
                        Log.d(TAG, "SUCCESS: " + authJson.toString())

                        // save auth data
                        RUSDApplication.setAuthToken(this, authJson.getString("access_token"))
                        RUSDApplication.setExpiresAt(this, authJson.getLong("expires_in"))
                        RUSDApplication.setRefreshToken(this, authJson.getString("refresh_token"))

                        // send firebase ID token to server
                        val firebaseToken = FirebaseInstanceId.getInstance().token
                        if (firebaseToken != null) {
                            RUSDApplication.setNeedsSubscriptionsReset(this, true)
                            FirebaseRegistrationTask().execute(this, firebaseToken)
                        } else {
                            // save user to realm
                            val userJson = RUSDApplication.convertUserLocations(authJson.getJSONObject("user"))
                            val user = RUSDApplication.getGson().fromJson<User>(userJson.toString(), User::class.java)
                            val userRepo = RealmUserRepository(this)
                            userRepo.setUser(user)
                            userRepo.close()
                        }


                        // Start Main Activity
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Must have Google Play Services to login", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        isGooglePlayServicesAvailable()
    }

    fun isGooglePlayServicesAvailable(){
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (status != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(this, status, GOOGLE_PLAY_SERVICES_REQUEST).show()
            isFirebaseAvailable = false
        } else {
            isFirebaseAvailable = true
        }
    }
}
