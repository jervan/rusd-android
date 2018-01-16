package edu.uwp.appfactory.rusd.services

import android.os.Handler
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import edu.uwp.appfactory.rusd.RUSDApplication

/**
 * Created by Jeremiah on 8/22/17.
 *
 * Service for updating a users firebase registration token
 */
class RUSDFirebaseInstanceIDService: FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        Log.d("FireBaseId Service", "Refreshing firebase Id")
        val refreshedToken = FirebaseInstanceId.getInstance().token
        val authToken = getSharedPreferences("prefs", 0).getString("authToken", null)

        if (authToken != null) {
            // This is so the Async task gets added to the main thread Async task pool. Keeps Multiple Async Tasks from
            // being executed at the same time.
            val mainHandler = Handler(mainLooper)
            val runnable = Runnable {
                RUSDApplication.setNeedsSubscriptionsReset(this, true)
                FirebaseRegistrationTask().execute(this, refreshedToken)
            }
            mainHandler.post(runnable)

        }
    }
}