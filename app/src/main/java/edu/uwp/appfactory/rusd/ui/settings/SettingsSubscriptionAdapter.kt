package edu.uwp.appfactory.rusd.ui.settings

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.android.databinding.library.baseAdapters.BR
import edu.uwp.appfactory.rusd.R
import edu.uwp.appfactory.rusd.data.local.TopicRepository
import edu.uwp.appfactory.rusd.data.model.Subscription
import edu.uwp.appfactory.rusd.databinding.SettingsSubscriptionItemBinding

/**
 * Created by dakota on 8/25/17.
 *
 * Adaptor Class for settings recycler view
 */
class SettingsSubscriptionAdapter(val topicRepository: TopicRepository, val context: Context) : RecyclerView.Adapter<SettingsSubscriptionAdapter.SettingsSubscriptionViewHolder>() {

    val subscriptions: MutableList<Subscription> = topicRepository.getSubscriptions()

    override fun onBindViewHolder(holder: SettingsSubscriptionViewHolder, position: Int) {
        holder.bind(subscriptions[position], position, this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsSubscriptionViewHolder {
        val binding: SettingsSubscriptionItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.settings_subscription_item, parent, false)
        return SettingsSubscriptionViewHolder(topicRepository, binding, context)
    }

    override fun getItemCount(): Int = subscriptions.size

    class SettingsSubscriptionViewHolder(val topicRepository: TopicRepository, val binding: SettingsSubscriptionItemBinding, val context: Context) : RecyclerView.ViewHolder(binding.root) {

        fun bind(subscription: Subscription, position: Int, adapter: SettingsSubscriptionAdapter) {
            binding.setVariable(BR.subscription, subscription)
            binding.root.setOnClickListener { v ->
                val popup = PopupMenu(context, v)
                popup.inflate(R.menu.subscribe_options)
                popup.menu.removeItem(getMenuItemId(subscription.subscriptionType))
                popup.setOnMenuItemClickListener { item ->
                    val originalSubscriptionType = subscription.subscriptionType
                    when (item.itemId) {
                        R.id.unsubscribe -> {
                            subscription.subscriptionType = Subscription.SubscriptionType.UNSUBSCRIBED
                        }

                        R.id.district_wide -> {
                            subscription.subscriptionType = Subscription.SubscriptionType.DISTRICT_WIDE
                        }

                        R.id.building_only -> {
                            subscription.subscriptionType = Subscription.SubscriptionType.LOCATION
                        }
                    }
                    adapter.notifyItemChanged(position)
                    topicRepository.setSubscription(subscription, originalSubscriptionType) { success ->
                        if (!success) {
                            subscription.subscriptionType = originalSubscriptionType
                            adapter.notifyItemChanged(position)
                            Toast.makeText(context, "Can not change subscriptions try again later", Toast.LENGTH_LONG).show()
                        }
                    }
                    false
                }
                popup.show()
            }
            binding.executePendingBindings()
        }

        fun getMenuItemId(subscriptionType: Subscription.SubscriptionType): Int {
            when (subscriptionType) {
                Subscription.SubscriptionType.UNSUBSCRIBED -> {
                    return R.id.unsubscribe
                }
                Subscription.SubscriptionType.DISTRICT_WIDE -> {
                    return R.id.district_wide
                }
                Subscription.SubscriptionType.LOCATION -> {
                    return R.id.building_only
                }
            }
        }
    }
}