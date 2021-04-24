package tech.jhavidit.remindme.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.notes_item.view.*
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.view.fragments.NotesFragmentDirections

class NotesListAdapter : RecyclerView.Adapter<NotesListAdapter.MyViewHolder>() {

    private var notes = emptyList<NotesModel>()

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesListAdapter.MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.notes_item, parent, false))

    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun onBindViewHolder(holder: NotesListAdapter.MyViewHolder, position: Int) {
        val currentNotes = notes[position]
        holder.itemView.title.text = currentNotes.title
        holder.itemView.description.text = currentNotes.description
        val notes = NotesModel(currentNotes.id,currentNotes.title,currentNotes.description)
        holder.itemView.notes.setOnClickListener {
            holder.itemView.findNavController().navigate(NotesFragmentDirections.updateNotes("update",notes))
        }

    }

    fun setNotes(notes: List<NotesModel>){
        this.notes = notes
        notifyDataSetChanged()
    }

}