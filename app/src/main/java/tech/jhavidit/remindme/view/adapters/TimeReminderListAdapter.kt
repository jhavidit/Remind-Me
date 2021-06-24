package tech.jhavidit.remindme.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.reminder_item.view.*
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.room.stringToBitmap
import tech.jhavidit.remindme.util.*
import tech.jhavidit.remindme.view.fragments.NotesFragment
import tech.jhavidit.remindme.view.fragments.ReminderFragmentDirections

class TimeReminderListAdapter(private val clickListen: TimeReminderAdapterInterface) :
    RecyclerView.Adapter<TimeReminderListAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface TimeReminderAdapterInterface {
        fun disableEnableTimeReminder(checked: Boolean, notesModel: NotesModel)
    }

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
                holder.itemView.reminder_repeat.text = String.format("%s", "Repeating")
            holder.itemView.reminder.setCardBackgroundColor(Color.parseColor(currentNotes.backgroundColor))
            currentNotes.image?.let {
                if (checkStoragePermission(holder.itemView.context)) {
                    Glide.with(holder.itemView.context)
                        .load(stringToUri(currentNotes.image))
                        .into(holder.itemView.reminder_image)
                } else {
                    holder.itemView.image_card.visibility = GONE
                    toast(
                        holder.itemView.context,
                        "You need to enable storage permission to view image"
                    )
                }
            } ?: run {
                holder.itemView.image_card.visibility = GONE
            }
            holder.itemView.reminder.setOnClickListener {
                holder.itemView.findNavController().navigate(
                    ReminderFragmentDirections.editReminder(
                        UPDATE, currentNotes
                    )
                )
            }
            holder.itemView.reminder_switch.isChecked = currentNotes.timeReminder == true
            holder.itemView.reminder_switch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    clickListen.disableEnableTimeReminder(true, currentNotes)
                } else
                    clickListen.disableEnableTimeReminder(false, currentNotes)
            }
        }
    }

    fun setNotes(notes: List<NotesModel>) {
        this.notes = notes
        notifyDataSetChanged()
    }
}