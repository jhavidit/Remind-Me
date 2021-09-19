package tech.jhavidit.remindme.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "location_table")
@Parcelize
data class LocationModel(
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0,
    var latitude : Double = 0.0,
    var longitude : Double = 0.0,
    var name : String = "",
    var placeId : String = ""

):Parcelable