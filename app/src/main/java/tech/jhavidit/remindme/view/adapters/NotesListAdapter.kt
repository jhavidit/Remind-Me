package tech.jhavidit.remindme.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.notes_item.view.*
import kotlinx.android.synthetic.main.notes_item.view.description
import kotlinx.android.synthetic.main.notes_item.view.title
import kotlinx.android.synthetic.main.reminder_item.view.*
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.util.UPDATE
import tech.jhavidit.remindme.view.fragments.NotesFragmentDirections

class NotesListAdapter : RecyclerView.Adapter<NotesListAdapter.MyViewHolder>() {

    private var notes = emptyList<NotesModel>()

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.notes_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentNotes = notes[position]
        holder.itemView.title.text = currentNotes.title
        holder.itemView.description.text = currentNotes.description
        holder.itemView.notes.setCardBackgroundColor(Color.parseColor(currentNotes.backgroundColor))
        if(currentNotes.isPinned)
            holder.itemView.pinned_note.visibility = VISIBLE
        else
            holder.itemView.pinned_note.visibility = GONE

        val notes = NotesModel(
            id = currentNotes.id,
            title = currentNotes.title,
            description = currentNotes.description,
            locationReminder = currentNotes.locationReminder,
            timeReminder = currentNotes.timeReminder,
            reminderWaitTime = currentNotes.reminderWaitTime,
            reminderTime =currentNotes.reminderTime,
            reminderDate = currentNotes.reminderDate,
            latitude = currentNotes.latitude,
            isPinned = currentNotes.isPinned,
            locationName = currentNotes.locationName,
            longitude = currentNotes.longitude,
            radius = currentNotes.radius,
            repeatValue = currentNotes.repeatValue,
            backgroundColor = currentNotes.backgroundColor,
            image = currentNotes.image,
            lastUpdated = currentNotes.lastUpdated

        )
        holder.itemView.notes.setOnClickListener {
            holder.itemView.findNavController()
                .navigate(NotesFragmentDirections.updateNotes(UPDATE, notes))
        }
    }

    fun setNotes(notes: List<NotesModel>) {
        this.notes = notes
        notifyDataSetChanged()
    }

}