package edu.uwp.appfactory.rusd.ui.notifications

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import edu.uwp.appfactory.rusd.data.local.NotificationsRepository

/**
 * Created by dakota on 7/4/17.
 *
 * Factory Class for creating Notifications View Model
 */
class NotificationsViewModelFactory(val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NotificationsViewModel(context) as T
    }
}