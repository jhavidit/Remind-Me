package tech.jhavidit.remindme.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "location_table")
@Parcelize
data class LocationModel(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val latitude : Double = 0.0,
    val longitude : Double = 0.0,
    val name : String = "",
    val placeId : String = ""

):Parcelable