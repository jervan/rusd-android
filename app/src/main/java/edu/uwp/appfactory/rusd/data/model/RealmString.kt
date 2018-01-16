package edu.uwp.appfactory.rusd.data.model

import io.realm.RealmObject

/**
 * Created by dakota on 8/19/17.
 *
 * A Model Class for String Objects
 * needed for storing lists of strings in realm
 */
open class RealmString() : RealmObject() {

    constructor(string: String) : this() {
        this.string = string
    }

    private var string: String = ""

    override fun toString(): String {
            return string
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RealmString) return false

        if (string != other.string) return false

        return true
    }

    override fun hashCode(): Int {
        return string.hashCode()
    }


}