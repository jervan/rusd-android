package edu.uwp.appfactory.rusd.ui.support

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import edu.uwp.appfactory.rusd.data.model.Publication
import io.realm.RealmList
import io.realm.RealmResults

/**
 * Created by Marshall on 6/28/2017.
 *
 * Edited by Jeremiah on 7/21/2017.
 */

class SupportViewPagerAdapter(fm: FragmentManager, var publications: List<Publication>) : FragmentStatePagerAdapter(fm) {


    override fun getItem(position: Int): Fragment {
        val imageHolderFragment = ImageHolderFragment.newInstance(publications[position])
        return imageHolderFragment
    }

    override fun getCount(): Int {
//        val realmPublication = publications as RealmResults
//        if (realmPublication.isValid && realmPublication.isLoaded) {
//            return publications.size
//        } else {
//            return 0
//        }
        return publications.size
    }

    fun setPublicationsList(publications: List<Publication>) {
        this.publications = publications
        this.notifyDataSetChanged()
    }
}