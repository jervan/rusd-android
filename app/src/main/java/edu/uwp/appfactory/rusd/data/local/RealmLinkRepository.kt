package edu.uwp.appfactory.rusd.data.local

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.uwp.appfactory.rusd.data.model.Link
import io.realm.Realm
import java.io.IOException
import java.io.InputStream

/**
 * Created by dakota on 7/23/17.
 *
 * Edited by Jeremiah on 8/3/17.
 */
class RealmLinkRepository(val context: Context) : LinkRepository {

    private val TAG = "Link Repository"
    private var realm: Realm? = Realm.getDefaultInstance()
    val VERSION = 1

    override fun getAllLinks() : List<Link> {
        return realm?.where(Link::class.java)?.findAll().orEmpty()
    }

    init {
        if (VERSION != getVersionFromPrefs()) {
            updateLinks()
        }
    }

    override fun close() {
        if (realm != null) {
            realm?.close()
            realm = null
        }
    }

    override fun initLinks(links: List<Link>) {
        if (realm != null) {
            realm?.executeTransaction {
                realm?.delete(Link::class.java)
                links.forEach { link ->
                    realm?.copyToRealmOrUpdate(link)
                }
            }
            saveVersionToPrefs()
        } else {
            Log.e(TAG, "Realm is closed cannot init " + links.toString())
        }
    }

    @Throws(IOException::class)
    private fun getString(inputStream: InputStream): String {
        return inputStream.bufferedReader().use { it.readText() }
    }

    override fun updateLinks() {
        var links: List<Link>? = null
        try {
            val tokenType = object : TypeToken<List<Link>>() {}.type
            links = Gson().fromJson<List<Link>>(getString(context.assets.open("links.json")), tokenType)
        } catch (e: IOException) {
            Log.e(TAG, "File read error: " + e.message)
        }

        if (links != null) {
            initLinks(links)
        } else {
            Log.e(TAG, "Links update error!!!")
        }
    }

    private fun saveVersionToPrefs() {
        val prefs = context.getSharedPreferences("prefs", 0)
        prefs.edit().putInt("LINKS_VERSION", VERSION).apply()
        Log.d("Links", "Version $VERSION saved to prefs.")
    }

    private fun getVersionFromPrefs() : Int {
        return context.getSharedPreferences("prefs", 0).getInt("LINKS_VERSION", -1)
    }
}