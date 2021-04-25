package tech.jhavidit.remindme.room

import androidx.lifecycle.LiveData
import androidx.room.*
import tech.jhavidit.remindme.model.NotesModel

@Dao
interface NotesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNotes(user: NotesModel)

    @Update
    suspend fun updateNotes(user: NotesModel)

    @Delete
    suspend fun deleteNotes(user: NotesModel)

    @Query("DELETE FROM notes_table")
    suspend fun deleteAllNotes()

    @Query("SELECT * FROM notes_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<NotesModel>>

    @Query("SELECT COUNT(id) FROM notes_table")
    fun notesCount():LiveData<Int>



}