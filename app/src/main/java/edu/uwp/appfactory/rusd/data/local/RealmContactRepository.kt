package edu.uwp.appfactory.rusd.data.local

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.uwp.appfactory.rusd.RUSDApplication
import edu.uwp.appfactory.rusd.data.model.Contact
import io.realm.Realm

/**
 * Created by dakota on 7/4/17.
 * Edited by Jeremiah on 7/17/17.
 *
 * Repository Class for storing and retrieving Contacts in a realm Database.
 */

class RealmContactRepository(val context: Context) : ContactRepository {

    private val TAG = "Contact Repository"
    private val contacts = MutableLiveData<List<Contact>>()
    private var realm: Realm? = Realm.getDefaultInstance()

    init {
        setAllContacts()
    }

    override fun updateContacts() {
        UpdateContacts(this).execute(context)
    }

    override fun initContacts(contacts: List<Contact>) {
        if (realm != null) {
            realm?.executeTransaction {
                realm?.delete(Contact::class.java)
                contacts.forEach { contact ->
                    realm?.copyToRealmOrUpdate(contact)
                }
            }
            setAllContacts()
        } else {
            Log.e(TAG, "Realm is closed cannot init " + contacts.toString())
        }
    }

    override fun setAllContacts() {
        contacts.value = realm?.where(Contact::class.java)?.isNull("deletedAt")?.findAll()?.sort("title").orEmpty()
    }

    override fun getAllContacts(): LiveData<List<Contact>> {
        return contacts
    }

    override fun close() {
        if (realm != null) {
            realm?.close()
            realm = null
        }
    }

    // Not sure if we really need these or how we plan to implement could call setAllContacts to force UI update or just edit
    // the value on the correct item in the contacts list after committing to realm
    override fun add(item: Contact) {
        if (realm != null) {
            realm?.executeTransaction { realm?.copyToRealmOrUpdate(item)  }
        } else {
            Log.e(TAG, "Realm is closed cannot add " + item.toString())
        }
    }

    override fun remove(item: Contact) {
        if (realm != null) {
            realm?.executeTransaction { item.deleteFromRealm() }
        } else {
            Log.e(TAG, "Realm is closed cannot delete " + item.toString())
        }
    }

    override fun update(item: Contact) {
        if (realm != null) {
            realm?.executeTransaction { realm?.copyToRealmOrUpdate(item)  }
        } else {
            Log.e(TAG, "Realm is closed cannot update " + item.toString())
        }
    }

    // used to update realm with new contact data
    class UpdateContacts(val contactRepository: RealmContactRepository): AsyncTask<Context, Void, List<Contact>?>() {
        val TAG = "update contacts"
        var retried = false

        override fun doInBackground(vararg params: Context?): List<Contact>? {
            Log.d(TAG, "update started")
            var contacts: List<Contact>? = null
            val (_, response, result) = "/contacts?ignorePagination=true".httpGet().responseJson()

            when(result) {
                is Result.Failure -> {
                    Log.e(TAG, "ERROR: " + result.getAs())
                    Log.e(TAG, " Response Code: " + response.statusCode)
                    if (!retried && response.statusCode == 401 && params[0] != null &&
                            RUSDApplication.shouldRetryRequest(params[0]!!)) {
                        retried = true
                        return doInBackground(params[0])
                    }
                }
                is Result.Success -> {
                    Log.d(TAG, "SUCCESS: " + result.get().obj().toString())
                    val tokenType = object : TypeToken<List<Contact>>() {}.type
                    contacts = Gson().fromJson<List<Contact>>(result.get().obj().getJSONArray("data").toString(), tokenType)
                }
            }

            return contacts
        }


        override fun onPostExecute(result: List<Contact>?) {
            super.onPostExecute(result)
            if (result != null) {
                contactRepository.initContacts(result)
                Log.d(TAG, "update successful")
            } else {
                Log.e(TAG, "update failed")
            }
        }
    }
}