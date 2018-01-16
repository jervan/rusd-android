package edu.uwp.appfactory.rusd.ui.support

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context

/**
 * Created by dakota on 7/4/17.
 *
 * Factory class used for creating Support view model
 */
class SupportViewModelFactory(val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SupportViewModel(context) as T
    }
}