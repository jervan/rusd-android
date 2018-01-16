package edu.uwp.appfactory.rusd.data.model

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by dakota on 8/12/17.
 *
 * A Model Class for Location Objects
 */

open class Location : RealmObject() {

    @SerializedName("_id") @PrimaryKey var id: String = ""
    @SerializedName("__v") var version: Int = 0
    var name: String = ""
    var createdAt: Date = Date()
    var updatedAt: Date = Date()
    var deletedAt: Date? = null

    override fun toString(): String {
        return "Location(id='$id', version=$version, name='$name', createdAt=$createdAt, updatedAt=$updatedAt, deletedAt=$deletedAt)"
    }
}