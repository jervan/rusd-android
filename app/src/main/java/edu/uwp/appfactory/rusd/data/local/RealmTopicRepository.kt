package edu.uwp.appfactory.rusd.data.local

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.google.gson.reflect.TypeToken
import edu.uwp.appfactory.rusd.RUSDApplication
import edu.uwp.appfactory.rusd.data.model.*
import io.realm.Realm
import io.realm.RealmResults
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

/**
 * Created by Jeremiah on 8/25/17.
 *
 *  Repository Class for storing and retrieving Topics in a realm Database.
 */
class RealmTopicRepository(val context: Context) : TopicRepository {

    private val UWP_ADMIN_ID = "59b5d0219e2d1dc4d6679143"
    private val ADMINISTRATIVE_SERVICE_CAMPUS_ID = "59b5b2b8444dd9b00db4f756"

    private val TAG = "Topic Repository"
    private var realm: Realm? = Realm.getDefaultInstance()
    private val userRepo = RealmUserRepository(context)
    private var notificationsRepo: RealmNotificationRepository? = null

    fun setNotificationRepository(notificationRepository: RealmNotificationRepository) {
        notificationsRepo = notificationRepository
    }

    private fun initTopics(topics: List<Topic>) {
        if (realm != null) {
            realm?.executeTransaction {
                realm?.delete(Topic::class.java)
                realm?.copyToRealmOrUpdate(topics)

            }
        } else {
            Log.e(TAG, "Realm is closed cannot init " + topics.toString())
        }
    }

    private fun initLocationSubscriptions(locationSubscriptions: List<LocationSubscription>) {
        if (realm != null) {
            realm?.executeTransaction {
                realm?.delete(LocationSubscription::class.java)
                realm?.copyToRealmOrUpdate(locationSubscriptions)

            }
            if (RUSDApplication.getNeedsSubscriptionsReset(context)) {
                Log.d(TAG, "Resetting Subscriptions")
                resetSubscriptions {
                    Log.d(TAG, "Reset complete")
                    notificationsRepo?.setAllNotifications()
                }
            }
        } else {
            Log.e(TAG, "Realm is closed cannot init " + locationSubscriptions.toString())
        }
    }

    private fun resetSubscriptions(onComplete: () -> Unit) {
        val user = userRepo.getCurrentUser()
        if (user != null) {
            RUSDApplication.setNeedsSubscriptionsReset(context, false)
            val subscriptions = getSubscriptions()
            var completedCount = 0
            subscriptions.forEach { subscription ->
                val originalSubscriptionType = subscription.subscriptionType
                Log.d(TAG, user.toString())
                if (user.locations != null && user.locations!![0] != RealmString("")) {
                    subscription.subscriptionType = Subscription.SubscriptionType.LOCATION
                } else {
                    subscription.subscriptionType = Subscription.SubscriptionType.DISTRICT_WIDE
                }
                setSubscription(subscription, originalSubscriptionType) { success ->
                    if (!success) {
                        Log.e(TAG, "Newtork Error couldn't reset subscription for topic " + subscription.topic.name)
                        RUSDApplication.setNeedsSubscriptionsReset(context, true)
                    }
                    if (++completedCount == subscriptions.size) {
                        onComplete.invoke()
                    }
                }
            }
        } else {
            onComplete.invoke()
        }
    }

    override fun setSubscription(subscription: Subscription, originalSubscriptionType: Subscription.SubscriptionType,
                                 subscriptionCallback: (Boolean) -> Unit) {
        val user = userRepo.getCurrentUser()
        if (user != null) {
            val originalSubscription = Subscription(subscription.topic, originalSubscriptionType)
            when (subscription.subscriptionType) {

                Subscription.SubscriptionType.DISTRICT_WIDE -> {

                    Subscribe(this, subscription, subscriptionCallback)
                            .execute(context, getSubscriptionJson(true, null), subscription.topic.id)

                    if (originalSubscriptionType == Subscription.SubscriptionType.LOCATION) {
                        Unsubscribe(this, originalSubscription, null)
                                .execute(context, getSubscriptionJson(null, getSubscribedLocations(subscription.topic)), subscription.topic.id)
                    }
                }

                Subscription.SubscriptionType.LOCATION -> {
                    val userLocations = user.locations
                    if (userLocations != null) {
                        // check if user locations are null on server and not allow change to locations
                        if (userLocations[0] == RealmString("")) {
                            subscriptionCallback.invoke(false)
                            return
                        }
                        val locationsArray = arrayListOf<String>()
                        userLocations.forEach { location -> locationsArray.add(location.toString()) }

                        // if user is UWP Admin add Administrative Service Campus to locations
                        if (user.id == UWP_ADMIN_ID && !locationsArray.contains(ADMINISTRATIVE_SERVICE_CAMPUS_ID)) {
                            locationsArray.add(ADMINISTRATIVE_SERVICE_CAMPUS_ID)
                        }
                        if (originalSubscriptionType == Subscription.SubscriptionType.DISTRICT_WIDE) {
                            Subscribe(this, subscription) { success ->
                                if (success) {
                                    Unsubscribe(this, originalSubscription, subscriptionCallback)
                                            .execute(context, getSubscriptionJson(true, null), subscription.topic.id)
                                } else {
                                    subscriptionCallback(false)
                                }
                            }.execute(context, getSubscriptionJson(null, locationsArray.toTypedArray()), subscription.topic.id)

                        } else if (originalSubscriptionType == Subscription.SubscriptionType.LOCATION) {
                            Unsubscribe(this, originalSubscription) {
                                Subscribe(this, subscription, subscriptionCallback)
                                        .execute(context, getSubscriptionJson(null, locationsArray.toTypedArray()), subscription.topic.id)
                            }
                                    .execute(context, getSubscriptionJson(null, getSubscribedLocations(subscription.topic)), subscription.topic.id)
                        } else {
                            Subscribe(this, subscription, subscriptionCallback)
                                    .execute(context, getSubscriptionJson(null, locationsArray.toTypedArray()), subscription.topic.id)
                        }
                    } else {
                        subscriptionCallback(false)
                    }
                }

                Subscription.SubscriptionType.UNSUBSCRIBED -> {

                    if (originalSubscriptionType == Subscription.SubscriptionType.DISTRICT_WIDE) {
                        Unsubscribe(this, originalSubscription, subscriptionCallback)
                                .execute(context, getSubscriptionJson(true, null), subscription.topic.id)

                    } else if (originalSubscriptionType == Subscription.SubscriptionType.LOCATION) {
                        Unsubscribe(this, originalSubscription, subscriptionCallback)
                                .execute(context, getSubscriptionJson(null, getSubscribedLocations(subscription.topic)), subscription.topic.id)
                    }
                }
            }
        } else {
            subscriptionCallback(false)
        }
    }

    private fun getSubscriptionJson(districtWide: Boolean?, locations: Array<String>?) : JSONObject {
        val subscriptionJson = JSONObject()
        if ( districtWide != null) {
            subscriptionJson.put("districtWide", districtWide)
        }
        if (locations != null) {
            val locationsJson = JSONArray()
            locations.forEach { location -> locationsJson.put(location) }
            subscriptionJson.put("locations", locationsJson)
        }
        return subscriptionJson
    }

    override fun getSubscriptions(): MutableList<Subscription> {
        val subscriptions: MutableList<Subscription> = mutableListOf()
        if (realm != null) {
            val user = userRepo.getCurrentUser()

            if (user != null) {
                getTopics()?.forEach { topic ->
                    subscriptions.add(getSubscription(topic, user))
                }
            }
            return subscriptions
        } else {
            Log.e(TAG, "Realm is closed cannot get subscriptions")
            return subscriptions
        }
    }

    private fun getSubscription(topic: Topic, user: User): Subscription {

        val isDistrictWide = topic.subscribers?.contains(RealmString(user.id))!!
        val subscribedLocations = getSubscribedLocations(topic, user)
        val isLocations = subscribedLocations.isNotEmpty()

        if (user.registrationToken == null) {
            return Subscription(topic, Subscription.SubscriptionType.LOCATION)
        }

        if (isDistrictWide) {
            if (isLocations) {
                Unsubscribe(this, Subscription(topic, Subscription.SubscriptionType.LOCATION), null)
                        .execute(context, getSubscriptionJson(null, getSubscribedLocations(topic)), topic.id)
            }
            return Subscription(topic, Subscription.SubscriptionType.DISTRICT_WIDE)
        }

        if (isLocations) {
            updateUserLocationCheck(topic, user, subscribedLocations)
            return Subscription(topic, Subscription.SubscriptionType.LOCATION)
        }

        return Subscription(topic, Subscription.SubscriptionType.UNSUBSCRIBED)
    }

    private fun updateUserLocationCheck (topic: Topic, user: User, subscribedLocations: MutableList<String>) {
        if (user.id != UWP_ADMIN_ID) {
            var isCorrectLocations = true
            val userLocations = user.locations
            if (userLocations != null) {
                subscribedLocations.forEach { subscribedLocation ->
                    if (!userLocations.contains(RealmString(subscribedLocation))) {
                        isCorrectLocations = false
                    }
                }
                if (userLocations.size != subscribedLocations.size) {
                    isCorrectLocations = false
                }
            }
            if (!isCorrectLocations) {
                setSubscription(Subscription(topic, Subscription.SubscriptionType.LOCATION),
                        Subscription.SubscriptionType.LOCATION) { success ->
                    if (!success) {
                        Log.e(TAG, "Network Error will update subscribed locations next time")
                    }
                }
            }
        }
    }

    private fun getSubscribedLocations(topic: Topic, user: User): MutableList<String> {
        val locations = mutableListOf<String>()
        val locationSubscriptions = realm?.where(LocationSubscription::class.java)?.equalTo("topic", topic.id)?.findAll().orEmpty()

        locationSubscriptions.forEach { locationSubscription ->

            if (locationSubscription.subscribers.contains(RealmString(user.id))) {
                locations.add(locationSubscription.location)
            }
        }
        return locations
    }

    override fun getSubscribedLocations(topic: Topic): Array<String> {
        var subscribedLocations = mutableListOf<String>()
        val user = userRepo.getCurrentUser()
        if (user != null) {
            subscribedLocations = getSubscribedLocations(topic, user)
        }
        return subscribedLocations.toTypedArray()
    }

    override fun getTopics() : RealmResults<Topic>? {
        val topics = realm?.where(Topic::class.java)?.findAll()
        Log.d(TAG, "GET TOPICS " + topics)
        return topics
    }

    override fun updateTopics() {
        userRepo.updateUser()
        UpdateTopics(this).execute(context)
        UpdateLocationSubscriptions(this).execute(context)
    }

    override fun add(item: Topic) {
        if (realm != null) {
            realm?.executeTransaction { realm?.copyToRealmOrUpdate(item)  }
        } else {
            Log.e(TAG, "Realm is closed cannot add " + item.toString())
        }
    }

    override fun remove(item: Topic) {
        if (realm != null) {
            realm?.executeTransaction { item.deleteFromRealm() }
        } else {
            Log.e(TAG, "Realm is closed cannot delete " + item.toString())
        }
    }

    override fun update(item: Topic) {
        if (realm != null) {
            realm?.executeTransaction { realm?.copyToRealmOrUpdate(item)  }
        } else {
            Log.e(TAG, "Realm is closed cannot update " + item.toString())
        }
    }

    fun updateSubscribe(subscription: Subscription) {
        if (realm != null) {
            val user = userRepo.getCurrentUser()
            if (user != null) {
                val topic = subscription.topic
                if (topic.isValid && topic.isLoaded) {
                    if (subscription.subscriptionType == Subscription.SubscriptionType.DISTRICT_WIDE) {
                        realm?.executeTransaction {
                            topic.subscribers?.add(RealmString(user.id))
                            realm?.copyToRealmOrUpdate(topic)
                        }

                    } else if (subscription.subscriptionType == Subscription.SubscriptionType.LOCATION) {
                        val userLocations = user.locations
                        if (userLocations != null) {
                            realm?.executeTransaction {
                                // if user is UWP Admin add Administrative Service Campus to locations
                                if (user.id == UWP_ADMIN_ID && !userLocations.contains(RealmString(ADMINISTRATIVE_SERVICE_CAMPUS_ID))) {
                                    userLocations.add(RealmString(ADMINISTRATIVE_SERVICE_CAMPUS_ID))
                                }
                                val locationSubscriptions = mutableListOf<LocationSubscription>()
                                userLocations.forEach { location ->

                                    val locationSubscription = LocationSubscription()
                                    locationSubscription.id = UUID.randomUUID().toString()
                                    locationSubscription.topic = subscription.topic.id
                                    locationSubscription.location = location.toString()
                                    locationSubscription.subscribers.add(RealmString(user.id))

                                    locationSubscriptions.add(locationSubscription)
                                }
                                realm?.copyToRealmOrUpdate(locationSubscriptions)
                                Log.d(TAG, "Updated Subscription " + subscription)
                            }
                        }
                    }
                }
            }
        } else {
            Log.e(TAG, "Realm is closed cannot update subscription " + subscription.toString())
        }
    }

    fun updateUnsubscribe(subscription: Subscription) {
        if (realm != null) {
            val user = userRepo.getCurrentUser()
            if (user != null) {
                val topic = subscription.topic
                if (topic.isValid && topic.isLoaded) {
                    if (subscription.subscriptionType == Subscription.SubscriptionType.DISTRICT_WIDE) {
                        realm?.executeTransaction {
                            topic.subscribers?.remove(RealmString(user.id))
                            realm?.copyToRealmOrUpdate(topic)
                        }

                    } else if (subscription.subscriptionType == Subscription.SubscriptionType.LOCATION) {
                        val subscribedLocations = getSubscribedLocations(subscription.topic)
                        realm?.executeTransaction {
                            val locationSubscriptions = realm?.where(LocationSubscription::class.java)
                                    ?.equalTo("topic", subscription.topic.id)
                                    ?.`in`("location", subscribedLocations)
                                    ?.findAll()

                            locationSubscriptions?.deleteAllFromRealm()
                        }
                    }
                }
            }
        } else {
            Log.e(TAG, "Realm is closed cannot update subscription " + subscription.toString())
        }
    }

    override fun close() {
        if (realm != null) {
            realm?.close()
            userRepo.close()
            realm = null
        }
    }

    class Subscribe(val topicRepository: RealmTopicRepository, val subscription: Subscription, val callback:((Boolean) -> Unit)?) : AsyncTask<Any, Void, Boolean>() {

        private val TAG = "Update Subscribe"
        var retried = false

        override fun doInBackground(vararg params: Any?): Boolean {
            val context = params[0] as Context
            val body = params[1] as JSONObject
            val topicID = params[2] as String


            val (_, response, result) = "/topics/$topicID/subscribe".httpPost().body(body.toString()).responseString()
            when (result) {
                is Result.Failure -> {
                    Log.e(TAG, "ERROR: " + result.getAs())
                    Log.e(TAG, " Response Code: " + response.statusCode)
                    if (!retried && response.statusCode == 401 && params[0] != null &&
                            RUSDApplication.shouldRetryRequest(context)) {
                        retried = true
                        return doInBackground(params[0])
                    }
                }

                is Result.Success -> {
                    Log.d(TAG, "SUCCESS")
                    return true
                }
            }
            return false
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            if (result) {
                topicRepository.updateSubscribe(subscription)
            }
            callback?.invoke(result)
        }
    }

    class Unsubscribe(val topicRepository: RealmTopicRepository, val subscription: Subscription, val callback:((Boolean) -> Unit)?) : AsyncTask<Any, Void, Boolean>() {

        private val TAG = "Update Unsubscribe"
        var retried = false

        override fun doInBackground(vararg params: Any?): Boolean {
            val context = params[0] as Context
            val body = params[1] as JSONObject
            val topicID = params[2] as String


            val (_, response, result) = "/topics/$topicID/unsubscribe".httpPost().body(body.toString()).responseString()
            when (result) {
                is Result.Failure -> {
                    Log.e(TAG, "ERROR: " + result.getAs())
                    Log.e(TAG, " Response Code: " + response.statusCode)
                    if (!retried && response.statusCode == 401 && params[0] != null &&
                            RUSDApplication.shouldRetryRequest(context)) {
                        retried = true
                        return doInBackground(params[0])
                    }
                }

                is Result.Success -> {
                    Log.d(TAG, "SUCCESS")
                    return true
                }
            }
            return false
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            if (result) {
                topicRepository.updateUnsubscribe(subscription)
            }
            callback?.invoke(result)
        }
    }

    class UpdateTopics(val topicRepository: RealmTopicRepository) : AsyncTask<Context, Void, List<Topic>>() {

        private val TAG = "Update topics"
        var retried = false

        override fun doInBackground(vararg params: Context?): List<Topic>? {
            Log.d(TAG, "update started")
            var topics: List<Topic>? = null
            val (_, response, result) = "/topics".httpGet().responseJson()

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
                    val tokenType = object : TypeToken<List<Topic>>() {}.type
                    topics = RUSDApplication.getGson().fromJson<List<Topic>>(result.get().obj().getJSONArray("data").toString(), tokenType)
                    return topics
                }
            }
            return topics
        }

        override fun onPostExecute(result: List<Topic>?) {
            super.onPostExecute(result)
            if (result != null) {
                topicRepository.initTopics(result)
                Log.d(TAG, "update successful")
            } else {
                Log.e(TAG, "update failed")
            }
        }
    }

    class UpdateLocationSubscriptions(val topicRepository: RealmTopicRepository) : AsyncTask<Context, Void, List<LocationSubscription>>() {

        private val TAG = "Update loc. sub."
        var retried = false

        override fun doInBackground(vararg params: Context?): List<LocationSubscription>? {
            Log.d(TAG, "update started")
            var locationSubscriptions: List<LocationSubscription>? = null
            val (_, response, result) = "/subscriptions".httpGet().responseJson()

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
                    val tokenType = object : TypeToken<List<LocationSubscription>>() {}.type
                    locationSubscriptions = RUSDApplication.getGson().fromJson<List<LocationSubscription>>(result.get().obj().getJSONArray("data").toString(), tokenType)
                    return locationSubscriptions
                }
            }
            return locationSubscriptions
        }

        override fun onPostExecute(result: List<LocationSubscription>?) {
            super.onPostExecute(result)
            if (result != null) {
                topicRepository.initLocationSubscriptions(result)
                Log.d(TAG, "update successful")
            } else {
                Log.e(TAG, "update failed")
            }
        }
    }
}