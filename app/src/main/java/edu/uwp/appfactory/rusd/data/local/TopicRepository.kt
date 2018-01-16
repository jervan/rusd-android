package edu.uwp.appfactory.rusd.data.local

import edu.uwp.appfactory.rusd.data.model.Subscription
import edu.uwp.appfactory.rusd.data.model.Topic

/**
 * Interface for Topics repository used for connecting to User Interface
 *
 * Created by Jeremiah on 8/25/17.
 */
interface TopicRepository : BaseRepository<Topic> {

    fun updateTopics()
    fun getTopics() : List<Topic>?
    fun setSubscription(subscription: Subscription, originalSubscriptionType: Subscription.SubscriptionType,
                        subscriptionCallback: (Boolean) -> Unit)
    fun getSubscriptions() : MutableList<Subscription>
    fun getSubscribedLocations(topic: Topic) : Array<String>
}