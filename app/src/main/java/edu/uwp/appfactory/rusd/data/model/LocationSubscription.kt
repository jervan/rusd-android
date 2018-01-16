package edu.uwp.appfactory.rusd.data.model

import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by Jeremiah on 9/10/17.
 *
 * A Model Class for Location Subscription Objects
 */
open class LocationSubscription : RealmObject() {

    @SerializedName("_id") @PrimaryKey var id: String = ""
    @SerializedName("__v") var version: Int = 0
    var topic: String = ""
    var location: String = ""
    var subscribers: RealmList<RealmString> = RealmList()
    var createdAt: Date = Date()
    var updatedAt: Date = Date()
    var deletedAt: Date? = null

    override fun toString(): String {
        return "LocationSubscription(id='$id', version=$version, topic='$topic', location='$location', subscribers=" + Arrays.toString(subscribers.toArray()) + ", createdAt=$createdAt, updatedAt=$updatedAt, deletedAt=$deletedAt)"
    }


}