package tech.jhavidit.remindme.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.reminder_item.view.*
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.util.LOCATION
import tech.jhavidit.remindme.util.UPDATE
import tech.jhavidit.remindme.view.fragments.ReminderFragmentDirections

class LocationReminderListAdapter :
    RecyclerView.Adapter<LocationReminderListAdapter.MyViewHolder>() {
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
            holder.itemView.reminder_repeat.text = "Radius - ${currentNotes.radius}m"
            holder.itemView.reminder.setCardBackgroundColor(Color.parseColor(currentNotes.backgroundColor))
            holder.itemView.reminder.setOnClickListener {
                holder.itemView.findNavController().navigate(
                    ReminderFragmentDirections.editReminder(
                        UPDATE, currentNotes
                    )
                )
            }
            currentNotes.image?.let { } ?: run {
                holder.itemView.reminder_image.visibility = GONE

            }
        }
    }

    fun setNotes(notes: List<NotesModel>) {
        this.notes = notes
        notifyDataSetChanged()
    }
}