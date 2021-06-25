package tech.jhavidit.remindme.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.notes_item.view.*
import kotlinx.android.synthetic.main.search_note_item.view.*
import kotlinx.android.synthetic.main.search_note_item.view.description
import kotlinx.android.synthetic.main.search_note_item.view.title
import org.w3c.dom.Text
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.util.UPDATE
import tech.jhavidit.remindme.view.fragments.SearchNotesFragmentDirections

class SearchNoteAdapter : RecyclerView.Adapter<SearchNoteAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.title
        val description: TextView = itemView.description
        val noteCard: MaterialCardView = itemView.note
    }

    private var notes = emptyList<NotesModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.search_note_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentNotes = notes[position]
        holder.title.text = currentNotes.title
        holder.description.text = currentNotes.description
        holder.noteCard.setCardBackgroundColor(Color.parseColor(currentNotes.backgroundColor))
        holder.noteCard.setOnClickListener {
            holder.itemView.findNavController()
                .navigate(SearchNotesFragmentDirections.updateNotes(UPDATE, currentNotes))
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    fun setNotes(notes: List<NotesModel>) {
        this.notes = notes
        notifyDataSetChanged()
    }
}