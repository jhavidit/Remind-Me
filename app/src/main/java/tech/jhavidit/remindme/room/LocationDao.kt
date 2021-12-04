package tech.jhavidit.remindme.room

import androidx.lifecycle.LiveData
import androidx.room.*
import tech.jhavidit.remindme.model.LocationModel
import tech.jhavidit.remindme.model.NotesModel

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addLocation(location : LocationModel)

    @Update
    suspend fun updateLocation(location: LocationModel)

    @Query("SELECT * FROM location_table ORDER BY id DESC LIMIT 6")
    fun readAllData(): LiveData<List<LocationModel>>
}