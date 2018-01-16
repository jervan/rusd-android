package edu.uwp.appfactory.rusd.data.model

import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmObject

/**
 * Created by dakota on 7/4/17.
 * Edited by jeremiah on 7/21/17.
 *
 * Tag for sorting publications objects
 */
open class Tag() : RealmObject(), Parcelable {

    var name: String = ""

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flag: Int) {
        if (isValid) {
            parcel.writeString(name)
        }
    }

    override fun toString(): String {
        return "Tag(name='$name')"
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Tag> {
        override fun createFromParcel(parcel: Parcel): Tag {
            return Tag(parcel)
        }

        override fun newArray(size: Int): Array<Tag?> {
            return arrayOfNulls(size)
        }
    }


}