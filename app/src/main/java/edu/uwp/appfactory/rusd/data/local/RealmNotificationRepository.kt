package edu.uwp.appfactory.rusd.data.local

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.uwp.appfactory.rusd.RUSDApplication
import edu.uwp.appfactory.rusd.data.model.Notification
import edu.uwp.appfactory.rusd.data.model.SectionNotification
import edu.uwp.appfactory.rusd.data.model.Subscription
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

/**
 * Created by dakota on 7/4/17.
 * Edited by Jeremiah on 7/17/17.
 *
 * Repository Class for storing and retrieving Notifications in a realm Database.
 */

class RealmNotificationRepository(val context: Context) : NotificationsRepository {

    private val TAG = "Notification Repository"
    private val sectionNotifications = MutableLiveData<List<SectionNotification>>()
    private val topicRepository = RealmTopicRepository(context)
    private var realm: Realm? = Realm.getDefaultInstance()

    init {
        topicRepository.setNotificationRepository(this)
        topicRepository.updateTopics()
        setAllNotifications()
    }

    override fun updateNotifications() {
        UpdateNotifications(this).execute(context)
    }

    private fun initNotifications(notifications: List<Notification>) {
        if (realm != null) {
            realm?.executeTransaction {
                realm?.delete(Notification::class.java)
                realm?.copyToRealmOrUpdate(notifications)
            }
        } else {
            Log.e(TAG, "Realm is closed cannot init " + notifications.toString())
        }
        setAllNotifications()
    }

    override fun setAllNotifications() {
        val filterSet = getFilterFromPrefs().toTypedArray()
        val subscriptions = topicRepository.getSubscriptions()
        val sectionNotifications = ArrayList<SectionNotification>()

        subscriptions.forEach { (topic, subscriptionType) ->
            Log.d(TAG, "Setting notifications for " + topic.name + " type " + subscriptionType)
            if (!filterSet.contains(topic.name)) {

                val notifications: RealmResults<Notification>?
                val subscribedLocations = topicRepository.getSubscribedLocations(topic)

                when(subscriptionType) {

                    Subscription.SubscriptionType.DISTRICT_WIDE ->
                            notifications = realm
                                    ?.where(Notification::class.java)
                                    ?.equalTo("topic", topic.id)
                                    ?.findAllSorted("createdAt", Sort.DESCENDING)

                    Subscription.SubscriptionType.LOCATION ->
                            notifications = realm
                                    ?.where(Notification::class.java)
                                    ?.equalTo("topic", topic.id)
                                    ?.beginGroup()
                                        ?.equalTo("districtWide", true)
                                        ?.or()
                                        ?.`in`("location", subscribedLocations)
                                    ?.endGroup()
                                    ?.findAllSorted("createdAt", Sort.DESCENDING)

                    Subscription.SubscriptionType.UNSUBSCRIBED ->
                            notifications = null
                }
                if (notifications != null && notifications.isNotEmpty()) {
                    sectionNotifications.add(SectionNotification(topic.name, notifications))
                }
            }
        }
        this.sectionNotifications.value = sectionNotifications
    }

    private fun getFilterFromPrefs() : Set<String> {
        return context.getSharedPreferences("prefs", 0).getStringSet("FILTER_SET", mutableSetOf<String>())
    }

    override fun getAllNotifications(): LiveData<List<SectionNotification>> {
        return sectionNotifications
    }

    override fun getAllSubscribedTopicNames(): Array<CharSequence> {
        val subscriptions = topicRepository.getSubscriptions()
        val returnList = mutableListOf<CharSequence>()
        subscriptions.forEach { subscription ->
            if (subscription.subscriptionType != Subscription.SubscriptionType.UNSUBSCRIBED) {
                returnList.add(subscription.topic.name)
            }
        }
        return returnList.toTypedArray()
    }

    fun toastUpdateFailure() {
        setAllNotifications()
        Toast.makeText(context, "Connect to the network for the latest Notifications", Toast.LENGTH_LONG).show()
    }

    override fun close() {
        if (realm != null) {
            realm?.close()
            topicRepository.close()
            realm = null
        }
    }

    // Not sure if we really need these or how we plan to implement could call setAllNotifications to force UI update or just edit
    // the value on the correct item in the contacts list after committing to realm
    override fun add(item: Notification) {
        if (realm != null) {
            realm?.executeTransaction { realm?.copyToRealmOrUpdate(item)  }
        } else {
            Log.e(TAG, "Realm is closed cannot add " + item.toString())
        }
    }

    override fun remove(item: Notification) {
        if (realm != null) {
            realm?.executeTransaction { item.deleteFromRealm() }
        } else {
            Log.e(TAG, "Realm is closed cannot delete " + item.toString())
        }
    }

    override fun update(item: Notification) {
        if (realm != null) {
            realm?.executeTransaction { realm?.copyToRealmOrUpdate(item)  }
        } else {
            Log.e(TAG, "Realm is closed cannot update " + item.toString())
        }
    }

    class UpdateNotifications(val notificationRepository: RealmNotificationRepository): AsyncTask<Context, Void, List<Notification>?>() {
        val TAG = "update Notifications"
        var retried = false

        override fun doInBackground(vararg params: Context?): List<Notification>? {
            Log.d(TAG, "update started")
            var notifications: List<Notification>? = null
            val (_, response, result) = "/notifications?ignorePagination=true".httpGet().responseJson()

            when(result) {
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
                    val tokenType = object : TypeToken<List<Notification>>() {}.type
                    notifications = Gson().fromJson<List<Notification>>(result.get().obj().getJSONArray("data").toString(), tokenType)
                }
            }

            return notifications
        }


        override fun onPostExecute(result: List<Notification>?) {
            super.onPostExecute(result)
            if (result != null) {
                notificationRepository.initNotifications(result)
                Log.d(TAG, "update successful")
            } else {
                notificationRepository.toastUpdateFailure()
                Log.e(TAG, "update failed")
            }
        }
    }
}