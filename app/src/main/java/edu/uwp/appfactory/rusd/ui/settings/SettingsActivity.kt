package edu.uwp.appfactory.rusd.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.activity_settings.*

import edu.uwp.appfactory.rusd.R
import edu.uwp.appfactory.rusd.data.local.RealmTopicRepository
import edu.uwp.appfactory.rusd.data.local.TopicRepository

class SettingsActivity : AppCompatActivity() {

    private lateinit var topicRepository: TopicRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val mInflater = LayoutInflater.from(this)
        val mCustomView = mInflater.inflate(R.layout.actionbar, null)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.customView = mCustomView
        supportActionBar?.setDisplayShowCustomEnabled(true)
        topicRepository = RealmTopicRepository(this)

        subscriptions_recycler_view.layoutManager = LinearLayoutManager(this)
        val subscriptionAdapter = SettingsSubscriptionAdapter(topicRepository, this)
        subscriptions_recycler_view.adapter = subscriptionAdapter
        registerForContextMenu(subscriptions_recycler_view)

        turn_off_notifications_layout.setOnClickListener {
            startSettings()
        }
    }

    override fun onResume() {
        super.onResume()
        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            turn_off_notifications_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.notification_enabled))
            show_notification.setBackgroundResource(R.drawable.ic_notification_enabled)
        } else {
            turn_off_notifications_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.notification_disabled))
            show_notification.setBackgroundResource(R.drawable.ic_notification_disabled)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        topicRepository.close()
    }

    fun startSettings() {
        val intent = Intent()

        // accessing notification settings is version dependant
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1){
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", packageName)
            intent.putExtra("app_uid", applicationInfo.uid)
        } else {
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:" + packageName)
        }

        startActivity(intent)
    }

    fun privacyPolicyClicked(view: View) {
        startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.privacy_policy_url))))
    }
}
