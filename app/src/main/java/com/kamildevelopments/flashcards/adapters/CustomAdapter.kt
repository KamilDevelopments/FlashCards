package com.kamildevelopments.flashcards.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kamildevelopments.flashcards.R
import com.kamildevelopments.flashcards.models.Flashcard

class CustomAdapter(private val flashcards: MutableList<Flashcard>) :
    RecyclerView.Adapter<CustomAdapter.FlashcardViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    inner class FlashcardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionTextView: TextView = itemView.findViewById(R.id.questionTextView)
        val answerTextView: TextView = itemView.findViewById(R.id.answerTextView)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.question_row_item, parent, false)

        return FlashcardViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: FlashcardViewHolder, position: Int) {

        val flashcard = flashcards[position]
        viewHolder.questionTextView.text = flashcard.question
        viewHolder.answerTextView.text = flashcard.answer
    }

    override fun getItemCount() = flashcards.size
    fun addFlashcard(flashcard: Flashcard) {
        flashcards.add(flashcard)
        notifyItemInserted(flashcards.size - 1)
    }
}