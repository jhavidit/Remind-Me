package tech.jhavidit.remindme.view.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat


import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.background_color_item.view.*
import kotlinx.android.synthetic.main.bottom_sheet_add_color.view.*
import kotlinx.android.synthetic.main.fragment_create_notes.view.*
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.model.ColorModel
import tech.jhavidit.remindme.util.toast

class SelectBackgroundColorAdapter : RecyclerView.Adapter<SelectBackgroundColorAdapter.MyViewHolder>() {
    var  isSelected = -1
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

    }

    val color = ColorModel.getColorList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.background_color_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return color.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
       val currentColor = color[position]
        holder.itemView.color_card.setCardBackgroundColor(Color.parseColor(currentColor))
        holder.itemView.color_card.setOnClickListener {
            isSelected = position
            notifyDataSetChanged()
        }

        if(position==isSelected)
        {
            holder.itemView.color_card.strokeColor = ContextCompat.getColor(holder.itemView.context,R.color.purple_200)
            holder.itemView.color_selected.visibility = VISIBLE
        }
        else
        {
            holder.itemView.color_card.strokeColor = ContextCompat.getColor(holder.itemView.context,R.color.description_color)
            holder.itemView.color_selected.visibility = GONE
        }
    }
}