package edu.uwp.appfactory.rusd.data.model

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by dakota on 7/4/17.
 *
 * A Model Class for Notification Objects
 */

open class Notification : RealmObject() {

    @SerializedName("_id") @PrimaryKey var id: String = ""
    @SerializedName("__v") var version: Int = 0
    var title: String = ""
    var body: String = ""
    var districtWide: Boolean = false
    var location: String? = null
    var topic: String? = null
    var sender: String = ""
    var multicastId: Long = 0
    var createdAt: Date = Date()
    var updatedAt: Date = Date()

    override fun toString(): String {
        return "Notification(id='$id', version=$version, title='$title', body='$body', districtWide=$districtWide, location=$location, topic=$topic, sender='$sender', multicastId=$multicastId, createdAt=$createdAt, updatedAt=$updatedAt)"
    }


}
