package edu.uwp.appfactory.rusd.data.local

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.uwp.appfactory.rusd.data.model.Publication
import io.realm.Realm
import java.io.IOException
import java.io.InputStream

/**
 * Created by dakota on 7/4/17.
 * Edited by Jeremiah on 7/17/17.
 *
 * Repository Class for storing and retrieving Publications in a realm Database.
 */

class RealmPublicationsRepository(val context: Context) : PublicationsRepository {

    private val TAG = "Publication Repository"
    private var realm: Realm? = Realm.getDefaultInstance()
    private val VERSION = 1

    init {
        if (VERSION != getVersionFromPrefs()) {
            updatePublications()
        }
    }

    override fun initPublications(publications: List<Publication>) {
        if (realm != null) {
            realm?.executeTransaction {
                realm?.delete(Publication::class.java)
                publications.forEach { publication ->
                    realm?.copyToRealmOrUpdate(publication)
                }
            }
            saveVersionToPrefs()
        } else {
            Log.e(TAG, "Realm is closed cannot init " + publications.toString())
        }
    }

    override fun getAllPublications() : List<Publication>? {
        return realm?.where(Publication::class.java)?.contains("tags.name", "Publication")?.findAllSorted("name")?.orEmpty()
    }

    override fun getSupportPublications() : List<Publication>? {
        return realm?.where(Publication::class.java)?.contains("tags.name", "Support")?.findAllSorted("name")?.orEmpty()
    }

    override fun close() {
        if (realm != null) {
            realm?.close()
            realm = null
        }
    }

    // Not sure if we really need these or how we plan to implement could call setAllPublications to force UI update or just edit
    // the value on the correct item in the contacts list after committing to realm
    override fun add(item: Publication) {
        if (realm != null) {
            realm?.executeTransaction { realm?.copyToRealmOrUpdate(item)  }
        } else {
            Log.e(TAG, "Realm is closed cannot add " + item.toString())
        }
    }

    override fun remove(item: Publication) {
        if (realm != null) {
            realm?.executeTransaction { item.deleteFromRealm() }
        } else {
            Log.e(TAG, "Realm is closed cannot delete " + item.toString())
        }
    }

    override fun update(item: Publication) {
        if (realm != null) {
            realm?.executeTransaction { realm?.copyToRealmOrUpdate(item)  }
        } else {
            Log.e(TAG, "Realm is closed cannot update " + item.toString())
        }
    }

    override fun updatePublications() {
        var publications: List<Publication>? = null
        try {
            val tokenType = object : TypeToken<List<Publication>>() {}.type
            publications = Gson().fromJson<List<Publication>>(getString(context.assets.open("publications.json")), tokenType)
        } catch (e: IOException) {
            Log.e(TAG, "File read error: " + e.message)
        }
        if (publications != null) {
            initPublications(publications)
        } else {
            Log.e(TAG, "Publications update error!!!")
        }
    }

    @Throws(IOException::class)
    private fun getString(inputStream: InputStream): String {
        return inputStream.bufferedReader().use { it.readText() }
    }

    private fun saveVersionToPrefs() {
        val prefs = context.getSharedPreferences("prefs", 0)
        prefs.edit().putInt("PUBLICATIONS_VERSION", VERSION).apply()
        Log.d("Publications", "Version $VERSION saved to prefs.")
    }

    private fun getVersionFromPrefs() : Int {
        return context.getSharedPreferences("prefs", 0).getInt("PUBLICATIONS_VERSION", -1)
    }
}