package edu.uwp.appfactory.rusd.data.model


import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by dakota on 7/4/17.
 * Edited by jeremiah on 7/18/17.
 *
 * A Model Class for Contact Objects
 */

open class Contact : RealmObject() {

    @SerializedName("_id") @PrimaryKey var id: String = ""
    @SerializedName("__v") var version: Int = 0
    var name: String = ""
    var title: String = ""
    var phone: String? = null
    var email: String = ""
    var createdAt: Date = Date()
    var updatedAt: Date = Date()
    var deletedAt: Date? = null

    override fun toString(): String {
        return "Contact(id='$id', version=$version, job_title='$title', name='$name', phone=$phone, email='$email', createdAt=$createdAt, updatedAt=$updatedAt, deletedAt=$deletedAt)"
    }
}
