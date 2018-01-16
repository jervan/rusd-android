package edu.uwp.appfactory.rusd.data.model

import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by dakota on 8/11/17.
 *
 * A Model Class for User Objects
 */
open class User : RealmObject() {

    @SerializedName("_id") @PrimaryKey var id: String = ""
    @SerializedName("__v") var version: Int = 0
    var dn: String = ""
    var company: String? = null
    var employeeID: Int? = null
    var sAMAccountName: String = ""
    var displayName: String? = null
    var registrationToken: String? = null
    var memberOf: RealmList<RealmString>? = null
    var locations: RealmList<RealmString>? = null
    var createdAt: Date = Date()
    var updatedAt: Date = Date()
    var deletedAt: Date? = null

    override fun toString(): String {
        return "User(id='$id', version=$version, dn='$dn', company=$company, employeeID=$employeeID, sAMAccountName='$sAMAccountName', displayName=$displayName, registrationToken=$registrationToken, memberOf=" + Arrays.toString(memberOf?.toArray()) + ", locations=" + Arrays.toString(locations?.toArray()) + ", createdAt=$createdAt, updatedAt=$updatedAt, deletedAt=$deletedAt)"
    }


}