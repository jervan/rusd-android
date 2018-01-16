package edu.uwp.appfactory.rusd.data.local

import android.arch.lifecycle.LiveData
import edu.uwp.appfactory.rusd.data.model.Notification
import edu.uwp.appfactory.rusd.data.model.SectionNotification

/**
 * Interface for Notifications repository used for connecting to User Interface
 *
 * Created by dakota on 7/4/17.
 */
interface NotificationsRepository : BaseRepository<Notification> {
    /**
     * Define additional Notification related methods here
     */

    fun getAllNotifications() : LiveData<List<SectionNotification>>
    fun setAllNotifications()
    fun getAllSubscribedTopicNames() : Array<CharSequence>
    fun updateNotifications()
}