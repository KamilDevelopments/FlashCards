package com.kamildevelopments.flashcards.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kamildevelopments.flashcards.R
import com.kamildevelopments.flashcards.models.Flashcard

class CustomAdapter(private val flashcards: MutableList<Flashcard>
) :
    RecyclerView.Adapter<CustomAdapter.FlashcardViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    inner class FlashcardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionTextView: TextView = itemView.findViewById(R.id.questionTextView)
        val answerTextView: TextView = itemView.findViewById(R.id.answerTextView)
        val deleteQuestionButton: Button = itemView.findViewById(R.id.deleteQuestionButton)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.question_row_item, parent, false)

        return FlashcardViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: FlashcardViewHolder, position: Int) {

        val flashcard = flashcards[position]
        val questionText = "Q${position + 1}: ${flashcard.question}"
        val answerText = "A${position + 1}: ${flashcard.answer}"
        viewHolder.questionTextView.text = questionText
        viewHolder.answerTextView.text = answerText
        viewHolder.deleteQuestionButton.setOnClickListener {
            deleteFlashcard(position)
        }

    }

    override fun getItemCount() = flashcards.size
    fun addFlashcard(flashcard: Flashcard) {
        flashcards.add(flashcard)
        notifyItemInserted(flashcards.size - 1)
    }
    fun deleteFlashcard(position: Int) {
        flashcards.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, flashcards.size)
    }

}