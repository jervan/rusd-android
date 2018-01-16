package edu.uwp.appfactory.rusd.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by dakota on 7/23/17.
 *
 * A Model Class for Link Objects
 */
open class Link : RealmObject() {

    @PrimaryKey
    var name: String = ""
    var url: String = ""

    override fun toString(): String {
        return "Link(name='$name', url='$url')"
    }


}