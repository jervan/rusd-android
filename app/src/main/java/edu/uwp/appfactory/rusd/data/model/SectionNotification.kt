package edu.uwp.appfactory.rusd.data.model

/**
 *  Created by Jeremiah on 8/25/17.
 *
 *  Used for the notifications recycler view for displaying notification grouped by topics
 */

data class SectionNotification(var title: String, var notifications: List<Notification>) {

    override fun toString(): String {
        return "SectionNotification(title='$title', notifications=$notifications)"
    }
}