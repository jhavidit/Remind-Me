package tech.jhavidit.remindme.repository

import androidx.lifecycle.LiveData
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.room.NotesDao

class NotesRepository(private val notesDao: NotesDao) {

    val readAllData: LiveData<List<NotesModel>> = notesDao.readAllData()
    val notesCount: LiveData<Int> = notesDao.notesCount()
    val createdId: LiveData<Int> = notesDao.createdId()


    suspend fun addNotes(notes: NotesModel) {
        notesDao.addNotes(notes)
    }

    fun selectedNote(id: Int): Flow<List<NotesModel>> {
        return notesDao.selectedNote(id)
    }

    fun searchNote(searchQuery: String?): Flow<List<NotesModel>> {
        return notesDao.searchNotes(searchQuery)
    }

    suspend fun updateNotes(notes: NotesModel) {
        notesDao.updateNotes(notes)
    }

    suspend fun deleteNotes(notes: NotesModel) {
        notesDao.deleteNotes(notes)
    }

}