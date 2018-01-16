package edu.uwp.appfactory.rusd.ui.notifications

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.databinding.BindingAdapter
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.widget.TextView
import edu.uwp.appfactory.rusd.R
import edu.uwp.appfactory.rusd.RUSDApplication
import edu.uwp.appfactory.rusd.data.local.NotificationsRepository
import edu.uwp.appfactory.rusd.data.local.RealmNotificationRepository
import edu.uwp.appfactory.rusd.data.model.SectionNotification
import edu.uwp.appfactory.rusd.services.RUSDFirebaseMessagingService
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by dakota on 7/4/17.
 *
 * Edited by jeremiah on 7/21/17.
 */
class NotificationsViewModel(context: Context) : ViewModel() {

    val notificationsRepository: NotificationsRepository

    init {
        notificationsRepository = RealmNotificationRepository(context)
        RUSDFirebaseMessagingService.setNotificationRepository(notificationsRepository)
    }

    fun getNotifications() : LiveData<List<SectionNotification>> {
        return notificationsRepository.getAllNotifications()
    }

    fun updateNotifications() {
        notificationsRepository.updateNotifications()
    }

    override fun onCleared() {
        super.onCleared()
        RUSDFirebaseMessagingService.setNotificationRepository(null)
        notificationsRepository.close()
    }

    companion object {

        @JvmStatic
        @BindingAdapter("setDate")
        fun setDate(textView: TextView, date: Date) {
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy hh:mm aa", Locale.getDefault())
            textView.text = dateFormat.format(date)
        }

        @JvmStatic
        @BindingAdapter("setBackground")
        fun setBackground(cardView: CardView, date: Date) {
            val dayMilliseconds = 1000L * 60L * 60L * 24L
            val millisecondsOld = Date().time - date.time
            if (millisecondsOld <= dayMilliseconds) {
                cardView.setCardBackgroundColor(Color.WHITE)
                cardView.cardElevation = RUSDApplication.convertDp(cardView.context, 8F)
            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, R.color.notification_shaded))
                cardView.cardElevation = RUSDApplication.convertDp(cardView.context, 4F)
            }
        }
    }
}