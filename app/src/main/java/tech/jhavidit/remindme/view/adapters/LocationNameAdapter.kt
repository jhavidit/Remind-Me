package tech.jhavidit.remindme.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.saved_location_item.view.*
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.model.LocationModel

class LocationNameAdapter(private val clickListen : LocationNameInterface) :
    RecyclerView.Adapter<LocationNameAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface LocationNameInterface {
        fun clickListener(location: LocationModel)
    }

    private var location = emptyList<LocationModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.saved_location_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return location.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val locationName = location[position]
        holder.itemView.location_name.text = locationName.name
        holder.itemView.location_name.setOnClickListener {
           clickListen.clickListener(locationName)
        }
    }

    fun setLocation(location: List<LocationModel>) {
        this.location = location
        notifyDataSetChanged()
    }

}