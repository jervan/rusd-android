package edu.uwp.appfactory.rusd.ui.main

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import edu.uwp.appfactory.rusd.R
import edu.uwp.appfactory.rusd.data.local.RealmLinkRepository
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_layout.*
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.support.v4.view.GravityCompat
import android.text.SpannableString
import android.text.style.TextAppearanceSpan
import edu.uwp.appfactory.rusd.RUSDApplication
import edu.uwp.appfactory.rusd.data.local.RealmTopicRepository
import edu.uwp.appfactory.rusd.data.local.TopicRepository
import edu.uwp.appfactory.rusd.ui.settings.SettingsActivity
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.actionbar.view.*




class MainActivity : AppCompatActivity() {

    // Displays RUSD Logo in toolbar
    private val SHOULD_DISPLAY_LOGO = true
    val adapter = MainPagerAdapter(supportFragmentManager)
    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // if user is not logged in redirect to login
        val authToken = getSharedPreferences("prefs", 0).getString("authToken", null)
        if (authToken == null) {
            RUSDApplication.logout(this)
        }


        setContentView(R.layout.activity_main)

        initViewPager()

        val mCustomView = LayoutInflater.from(this).inflate(R.layout.actionbar, null)

        supportActionBar?.setDisplayHomeAsUpEnabled(!SHOULD_DISPLAY_LOGO)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.customView = mCustomView
        supportActionBar?.setDisplayShowCustomEnabled(true)

        val rusdLogo = mCustomView.rusd_logo
        if (SHOULD_DISPLAY_LOGO) {
            rusdLogo.visibility = View.VISIBLE
        } else {
            rusdLogo.visibility = View.GONE
        }

        val mainActivity = this

        mDrawerToggle = object:ActionBarDrawerToggle(this, main_nav_drawer, R.string.app_name, R.string.rusd_name) {
            override fun onDrawerOpened(drawerView: View?) {
                super.onDrawerOpened(drawerView)
                invalidateOptionsMenu()
            }

            override fun onDrawerStateChanged(newState: Int) {
                Log.d("DrawerState", newState.toString())
                if (newState == 2 && rusdLogo.visibility == View.VISIBLE) {
                    if (main_nav_drawer.isDrawerVisible(GravityCompat.START)) {
                        rusdLogo.startAnimation(
                                AnimationUtils.loadAnimation(mainActivity, R.anim.logo_rotate_counter_clockwise) )

                    } else {
                        rusdLogo.startAnimation(
                                AnimationUtils.loadAnimation(mainActivity, R.anim.logo_rotate_clockwise) )
                    }
                }
                super.onDrawerStateChanged(newState)
            }

            override fun onDrawerClosed(drawerView: View?) {
                super.onDrawerClosed(drawerView)
                invalidateOptionsMenu()
            }
        }

        rusdLogo.setOnClickListener {
            if (main_nav_drawer.isDrawerVisible(GravityCompat.START)) {
                main_nav_drawer.closeDrawer(GravityCompat.START)
            } else {
                main_nav_drawer.openDrawer(GravityCompat.START)
            }
        }

        main_nav_drawer.addDrawerListener(mDrawerToggle)
        val m: Menu = nav_view.menu
        nav_view.itemIconTintList = null
        val groupTitle = SpannableString("Links")
        groupTitle.setSpan(TextAppearanceSpan(this, R.style.NavigationDrawerGroupTitle), 0, groupTitle.length, 0)
        val menuGroup = m.addSubMenu(0, 0, 0, groupTitle)
        val repo = RealmLinkRepository(this)
        val links = repo.getAllLinks()
        val intent = Intent(Intent.ACTION_VIEW)
        for (link in links) {
            val url = link.url
            menuGroup.add(link.name).setIcon(R.drawable.link).setOnMenuItemClickListener {
            intent.data = Uri.parse(url)
                main_nav_drawer.closeDrawers()
            startActivity(intent)
            false
            }
        }
        repo.close()
        val settings = m.findItem(R.id.navdrawer_item_settings)
        settings.setOnMenuItemClickListener {
            main_nav_drawer.closeDrawers()
            startActivity(Intent(this, SettingsActivity::class.java))
            false
        }
        val logout = m.findItem(R.id.navdrawer_item_logout)
        logout.setOnMenuItemClickListener {
            main_nav_drawer.closeDrawers()
            RUSDApplication.logout(this)
            false
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun initViewPager() {
        if (main_viewpager.adapter == null) {
            main_viewpager.adapter = adapter
            main_tabLayout.setupWithViewPager(main_viewpager)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (main_nav_drawer.isDrawerOpen(GravityCompat.START)) {
            main_nav_drawer.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
