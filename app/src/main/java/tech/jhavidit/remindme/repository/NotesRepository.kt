package tech.jhavidit.remindme.repository

import androidx.lifecycle.LiveData
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.room.NotesDao

class NotesRepository(private val notesDao: NotesDao) {

    val readAllData: LiveData<List<NotesModel>> = notesDao.readAllData()
    val notesCount : LiveData<Int> = notesDao.notesCount()
    val createdId : LiveData<Int> = notesDao.createdId()

    suspend fun addNotes(notes: NotesModel) {
        notesDao.addNotes(notes)
    }

    suspend fun updateNotes(notes: NotesModel) {
        notesDao.updateNotes(notes)
    }

    suspend fun deleteNotes(notes: NotesModel) {
        notesDao.deleteNotes(notes)
    }

    suspend fun deleteAllNotes() {
        notesDao.deleteAllNotes()
    }

}