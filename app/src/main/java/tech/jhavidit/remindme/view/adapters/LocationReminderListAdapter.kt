package tech.jhavidit.remindme.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.reminder_item.view.*
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.model.NotesModel

class LocationReminderListAdapter : RecyclerView.Adapter<LocationReminderListAdapter.MyViewHolder>() {
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
        currentNotes.locationName?.let {
            holder.itemView.title.text = currentNotes.title
            holder.itemView.description.text = currentNotes.description
            holder.itemView.time.text = currentNotes.locationName
            holder.itemView.reminder_repeat.text = currentNotes.repeatAlarmIndex.toString()
        }
    }

    fun setNotes(notes: List<NotesModel>) {
        this.notes = notes
        notifyDataSetChanged()
    }
}