package tech.jhavidit.remindme.repository

import androidx.lifecycle.LiveData
import tech.jhavidit.remindme.model.LocationModel
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.room.LocationDao
import tech.jhavidit.remindme.room.NotesDao

class  LocationRepository(private val locationDao: LocationDao)
{
    val readAllData : LiveData<List<LocationModel>> = locationDao.readAllData()

    suspend fun addLocation(location : LocationModel) {
        locationDao.addLocation(location)
    }

    suspend fun updateLocation(location: LocationModel) {
        locationDao.updateLocation(location)
    }

    suspend fun deleteLocation(location: LocationModel) {
        locationDao.deleteLocation(location)
    }

    suspend fun deleteAllLocation() {
        locationDao.deleteAllLocation()
    }
}