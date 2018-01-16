package edu.uwp.appfactory.rusd.data.model

import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by Jeremiah on 7/16/17.
 *
 * A Model Class for Topic Objects
 */


open class Topic : RealmObject() {

    @SerializedName("_id") @PrimaryKey var id: String = ""
    @SerializedName("__v") var version: Int = 0
    var name: String = "Unassigned"
    var locations: RealmList<Location>? = null
    var subscribers: RealmList<RealmString>? = null
    var createdAt: Date = Date()
    var updatedAt: Date = Date()
    var deletedAt: Date? = null

    override fun toString(): String {
        return "Topic(id='$id', version=$version, name='$name', subscribers=" + Arrays.toString(subscribers?.toArray()) + ", locations=" + Arrays.toString(locations?.toArray()) +  ", createdAt=$createdAt, updatedAt=$updatedAt, deletedAt=$deletedAt)"
    }

}
