package tech.jhavidit.remindme.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.reminder_item.view.*
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.util.TIME
import tech.jhavidit.remindme.util.UPDATE
import tech.jhavidit.remindme.view.fragments.NotesFragment
import tech.jhavidit.remindme.view.fragments.ReminderFragmentDirections

class TimeReminderListAdapter : RecyclerView.Adapter<TimeReminderListAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private var notes = emptyList<NotesModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.reminder_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentNotes = notes[position]
        currentNotes.reminderTime?.let {
            holder.itemView.title.text = currentNotes.title
            holder.itemView.description.text = currentNotes.description
            holder.itemView.time.text =
                currentNotes.reminderTime.toString()
            if (currentNotes.repeatValue == -1L)
                holder.itemView.reminder_repeat.text = "Not Repeating"
            else
                holder.itemView.reminder_repeat.text = "Repeating"
            holder.itemView.reminder.setCardBackgroundColor(Color.parseColor(currentNotes.backgroundColor))
            holder.itemView.reminder.setOnClickListener {
                holder.itemView.findNavController().navigate(
                    ReminderFragmentDirections.editReminder(
                        UPDATE, currentNotes
                    )
                )
            }
        }
    }

    fun setNotes(notes: List<NotesModel>) {
        this.notes = notes
        notifyDataSetChanged()
    }
}