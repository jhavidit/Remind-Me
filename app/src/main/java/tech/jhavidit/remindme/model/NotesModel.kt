package tech.jhavidit.remindme.model

import android.graphics.Color
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "notes_table")
@Parcelize
data class NotesModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var title: String = "",
    val description: String = "",
    val locationReminder: Boolean? = null,
    val timeReminder: Boolean? = null,
    val reminderTime: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val radius: Double? = null,
    val repeatAlarmIndex: Int = -1,
    val isPinned: Boolean = false,
    val image: String? = null,
    val lastUpdated: String? = null,
    val backgroundColor: String = "#FFFFFF"

) : Parcelable
