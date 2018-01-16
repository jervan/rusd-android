package edu.uwp.appfactory.rusd.data.model

/**
 * Created by dakota on 8/29/17.
 *
 * Subscription object is used for storing a users subscription for a topic in an enum
 */
data class Subscription(var topic: Topic, var subscriptionType: SubscriptionType) {
    enum class SubscriptionType(val string: String) {
        UNSUBSCRIBED("Unsubscribed from these alerts"),
        DISTRICT_WIDE("All district alerts"),
        LOCATION("Alerts for my building only")
    }

    override fun toString(): String {
        return "Subscription(subscriptionType=$subscriptionType, topic=$topic)\n"
    }


}