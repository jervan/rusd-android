package edu.uwp.appfactory.rusd.data.model

import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*


/**
 * Created by dakota on 6/30/17.
 * Edited by jeremiah on 7/21/17.
 *
 * A Model Class for Publication Objects
 */
open class Publication() : RealmObject(), Parcelable {

    @PrimaryKey var name: String = ""
    var image: String = "rusd_logo.png"
    var URL: String = ""
    var tags: RealmList<Tag> = RealmList(Tag())

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()
        image = parcel.readString()
        URL = parcel.readString()
    }

    override fun toString(): String {
        return "Publication(name='$name', image='$image', URL='$URL', tags=" + Arrays.toString(tags.toArray()) + ")"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        if (isValid) {
            parcel.writeString(name)
            parcel.writeString(image)
            parcel.writeString(URL)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Publication> {
        override fun createFromParcel(parcel: Parcel): Publication {
            return Publication(parcel)
        }

        override fun newArray(size: Int): Array<Publication?> {
            return arrayOfNulls(size)
        }
    }
}
