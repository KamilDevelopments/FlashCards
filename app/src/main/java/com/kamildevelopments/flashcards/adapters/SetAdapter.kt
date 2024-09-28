package com.kamildevelopments.flashcards.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kamildevelopments.flashcards.R

class SetAdapter(private val sets: List<String>,
                 private val onSetClick: (String) -> Unit,
                 private val onSetDelete: (String) -> Unit,
                 private val onSetExport: (String) -> Unit) :
    RecyclerView.Adapter<SetAdapter.SetViewHolder>() {

    inner class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val setNameTextView: TextView = itemView.findViewById(R.id.setNameTextView)
        val deleteSetButton: ImageButton   = itemView.findViewById(R.id.deleteSetButton)
        val exportSetButton: ImageButton = itemView.findViewById(R.id.exportSetButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_set, parent, false)
        return SetViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        val setName = sets[position]
        holder.setNameTextView.text = setName
        holder.deleteSetButton.setOnClickListener{
            onSetDelete(setName)
        }
        holder.itemView.setOnClickListener {
            onSetClick(setName)
        }
        holder.exportSetButton.setOnClickListener{
            onSetExport(setName)
        }
    }

    override fun getItemCount(): Int = sets.size
}