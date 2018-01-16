package edu.uwp.appfactory.rusd.data.local

import android.arch.lifecycle.LiveData
import edu.uwp.appfactory.rusd.data.model.Contact

/**
 * Interface for Contacts repository used for connecting to User Interface
 *
 * Created by dakota on 7/4/17.
 */
interface ContactRepository : BaseRepository<Contact> {
    /**
     * Define additional Contact related methods here
     */
    fun initContacts(contacts: List<Contact>)
    fun setAllContacts()
    fun getAllContacts(): LiveData<List<Contact>>
    fun updateContacts()
}