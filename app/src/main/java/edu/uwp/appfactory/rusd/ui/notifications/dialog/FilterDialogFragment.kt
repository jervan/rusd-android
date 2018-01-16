package edu.uwp.appfactory.rusd.ui.notifications.dialog

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
import edu.uwp.appfactory.rusd.R
import edu.uwp.appfactory.rusd.data.local.NotificationsRepository


/**
 * Created by dakota on 7/15/17.
 *
 * Dialog Used for filtering notifications
 */
class FilterDialogFragment : DialogFragment() {

    private val TAG = "Filter Dialog"
    private lateinit var notificationsRepo: NotificationsRepository
    private lateinit var topics: Array<CharSequence>
    private lateinit var selection: BooleanArray

    companion object {

        @JvmStatic
        fun newInstance(notificationsRepository: NotificationsRepository) : FilterDialogFragment {
            val filterFragment: FilterDialogFragment = FilterDialogFragment()
            filterFragment.notificationsRepo = notificationsRepository
            filterFragment.topics = notificationsRepository.getAllSubscribedTopicNames()
            return filterFragment
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode ) {
            dismissAllowingStateLoss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setFilter()
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMultiChoiceItems(topics, selection, { _, which, isChecked -> selection[which] = isChecked })
        builder.setPositiveButton(android.R.string.ok, { _, _ ->  saveFilter() })
        builder.setNegativeButton(android.R.string.cancel, { _, _ -> dismiss() })
        builder.setTitle(R.string.filter_dialog_text)
        return builder.create()
    }

    private fun setFilter() {
        val keySet = getFilterFromPrefs()
        selection = BooleanArray(topics.size, { i ->  !keySet.contains(topics[i])})
    }

    private fun saveFilter() {
        val filterSet = mutableSetOf<String>()
        (0..selection.size - 1).forEach { i ->
            if (!selection[i]) {
                filterSet.add(topics[i].toString())
            }
        }
        saveFilterToPrefs(filterSet)
        notificationsRepo.setAllNotifications()
        dismiss()
    }

    private fun saveFilterToPrefs(filterSet: Set<String>) {
        val prefs = context.getSharedPreferences("prefs", 0)
        prefs.edit().putStringSet("FILTER_SET", filterSet).apply()
        Log.d(TAG, "Filter set $filterSet saved to prefs.")
    }

    private fun getFilterFromPrefs() : Set<String> {
        return context.getSharedPreferences("prefs", 0).getStringSet("FILTER_SET", mutableSetOf<String>())
    }
}