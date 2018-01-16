package edu.uwp.appfactory.rusd.ui.notifications

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.databinding.library.baseAdapters.BR
import edu.uwp.appfactory.rusd.R
import edu.uwp.appfactory.rusd.data.model.Notification
import edu.uwp.appfactory.rusd.data.model.SectionNotification
import edu.uwp.appfactory.rusd.databinding.ListItemNotificationsBinding
import edu.uwp.appfactory.rusd.databinding.SectionHeaderNotificationsBinding
import org.zakariya.stickyheaders.SectioningAdapter

/**
 * Created by Jeremiah on 8/8/17.
 *
 * Sticky Header Section Adaptor for Notifications RecyclerView
 */

class NotificationsSectionsAdapter(var sections: List<SectionNotification>) : SectioningAdapter() {

    class ItemViewHolder(private val binding: ListItemNotificationsBinding)
        : SectioningAdapter.ItemViewHolder(binding.root) {

        fun bind(notification: Notification) {
            binding.setVariable(BR.notification, notification)
            binding.executePendingBindings()
        }
    }

    class HeaderViewHolder(private val binding: SectionHeaderNotificationsBinding)
        : SectioningAdapter.HeaderViewHolder(binding.root) {

        fun bind(section: SectionNotification) {
            binding.setVariable(BR.section, section)
            binding.executePendingBindings()
        }
    }

    fun updateNotifications(sections: List<SectionNotification>) {
        this.sections = sections
        notifyAllSectionsDataSetChanged()
    }

    override fun getNumberOfSections(): Int {
        return sections.size
    }

    override fun getNumberOfItemsInSection(sectionIndex: Int): Int {
        return sections[sectionIndex].notifications.size
    }

    override fun doesSectionHaveHeader(sectionIndex: Int): Boolean {
        return true
    }

    override fun doesSectionHaveFooter(sectionIndex: Int): Boolean {
        return false
    }

    override fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): SectioningAdapter.ItemViewHolder {
        val binding: ListItemNotificationsBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.list_item_notifications, parent, false)
        return ItemViewHolder(binding)
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup, headerUserType: Int): SectioningAdapter.HeaderViewHolder {
        val binding: SectionHeaderNotificationsBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.section_header_notifications, parent, false)
        return HeaderViewHolder(binding)
    }

    override fun onBindItemViewHolder(viewHolder: SectioningAdapter.ItemViewHolder?, sectionIndex: Int, itemIndex: Int, itemUserType: Int) {
        if (viewHolder is ItemViewHolder) {
            viewHolder.bind(sections[sectionIndex].notifications[itemIndex])
        }
    }

    override fun onBindHeaderViewHolder(viewHolder: SectioningAdapter.HeaderViewHolder?, sectionIndex: Int, headerUserType: Int) {
        if (viewHolder is HeaderViewHolder) {
            viewHolder.bind(sections[sectionIndex])
        }
    }

}