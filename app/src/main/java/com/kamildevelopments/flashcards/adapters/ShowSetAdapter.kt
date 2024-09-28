package com.kamildevelopments.flashcards.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kamildevelopments.flashcards.R
import com.kamildevelopments.flashcards.models.Flashcard

class ShowSetAdapter(private val sets: List<Flashcard>): RecyclerView.Adapter<ShowSetAdapter.ShowSetViewHolder>() {

inner class ShowSetViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val descTextView: TextView = itemView.findViewById(R.id.descTextView)
    val ansTextView: TextView = itemView.findViewById(R.id.ansTextView)
}

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowSetViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_show_set, parent, false)
            return ShowSetViewHolder(itemView)
        }

    override fun onBindViewHolder(holder: ShowSetViewHolder, position: Int) {
        val question = sets[position].question
        val answer = sets[position].answer
        holder.descTextView.text = "question"
        holder.ansTextView.text = question
        holder.itemView.setOnClickListener {
        if(holder.descTextView.text=="question"){
            holder.descTextView.text = "answer"
            holder.ansTextView.text = answer
        } else {
            holder.descTextView.text = "question"
            holder.ansTextView.text = question
        }
        }
    }
    override fun getItemCount(): Int = sets.size
}