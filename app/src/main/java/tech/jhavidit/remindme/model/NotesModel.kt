
package tech.jhavidit.remindme.model
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "notes_table")
@Parcelize
data class NotesModel(
        @PrimaryKey(autoGenerate = true)
        val id: Int = 0,
        var title : String  = "",
        val description : String = ""
):Parcelable
