package fr.adixon.adiposrfiddemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.adixon.adiposrfid.RFIDTag

class RFIDTagAdapter(val tags: List<RFIDTag>)
    : RecyclerView.Adapter<RFIDTagAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tagCode: TextView = itemView.findViewById(R.id.tag_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.activity_tag_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = tags[position]
        holder.itemView.tag = position
        holder.tagCode.text = tag.code
    }

    override fun getItemCount(): Int {
        return tags.size
    }
}