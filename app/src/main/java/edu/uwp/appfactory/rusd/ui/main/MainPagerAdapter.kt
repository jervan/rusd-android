package edu.uwp.appfactory.rusd.ui.main

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import edu.uwp.appfactory.rusd.ui.notifications.NotificationsFragment
import edu.uwp.appfactory.rusd.ui.publications.PublicationsFragment
import edu.uwp.appfactory.rusd.ui.support.SupportFragment

/**
 * Created by dakota on 6/10/17.
 *
 * Main Activities View Pager used for displaying main fragments
 */
class MainPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    val notificationFragment = NotificationsFragment.newInstance()
    val supportFragment = SupportFragment.newInstance()
    val publicationsFragment = PublicationsFragment.newInstance()

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> return notificationFragment
            1 -> return supportFragment
            2 -> return publicationsFragment
            else -> return notificationFragment
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        var title = ""
        when (position) {
            0 -> title = "NOTIFICATIONS"
            1 -> title = "SUPPORT"
            2 -> title = "PUBLICATIONS"
        }
        return title
    }
}