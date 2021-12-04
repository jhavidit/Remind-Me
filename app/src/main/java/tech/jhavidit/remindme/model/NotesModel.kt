package tech.jhavidit.remindme.model

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(tableName = "notes_table")
@Parcelize
data class NotesModel(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var title: String = "",
    var description: String = "",
    var locationReminder: Boolean? = null,
    var timeReminder: Boolean? = null,
    var reminderWaitTime: Long? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var locationName: String? = null,
    var radius: Double? = null,
    var reminderTime: String? = null,
    var reminderDate: String? = null,
    var repeatValue: Long? = null,
    var isPinned: Boolean = false,
    var image: String? = null,
    var lastUpdated: String? = null,
    var backgroundColor: String = "#FFFFFF"

) : Parcelable
