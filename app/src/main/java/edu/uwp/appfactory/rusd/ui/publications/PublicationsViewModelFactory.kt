package edu.uwp.appfactory.rusd.ui.publications

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import edu.uwp.appfactory.rusd.data.local.PublicationsRepository

/**
 * Created by dakota on 7/4/17.
 *
 * Factory class used for creating Publications View Model
 */
class PublicationsViewModelFactory(val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PublicationsViewModel(context) as T
    }
}