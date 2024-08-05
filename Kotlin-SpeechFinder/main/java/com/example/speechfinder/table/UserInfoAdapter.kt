package com.example.speechfinder.table

// UserInfoAdapter.kt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.speachfinder.R
import com.example.speechfinder.NumberItem
import com.example.speechfinder.User

class UserInfoAdapter(private val userInfo: User, private val numberItemList: List<NumberItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_USER = 0
    private val VIEW_TYPE_ITEM = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_USER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_table, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_table2, parent, false)
            NumberItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == VIEW_TYPE_USER) {
            val userViewHolder = holder as UserViewHolder
            userViewHolder.emailTextView.text = userInfo.email
            userViewHolder.firstNameTextView.text = userInfo.first_name
            userViewHolder.lastNameTextView.text = userInfo.last_name
        } else {
            val numberItemViewHolder = holder as NumberItemViewHolder
            val item = numberItemList[position - 1]
            numberItemViewHolder.soundTextView.text = item.sound
            numberItemViewHolder.wordTextView.text = item.word
            numberItemViewHolder.numbersTextView.text = item.numbers.toString()
        }
    }

    override fun getItemCount(): Int {
        return numberItemList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_USER else VIEW_TYPE_ITEM
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        val firstNameTextView: TextView = itemView.findViewById(R.id.firstNameTextView)
        val lastNameTextView: TextView = itemView.findViewById(R.id.lastNameTextView)
    }

    class NumberItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val soundTextView: TextView = itemView.findViewById(R.id.soundTextView)
        val wordTextView: TextView = itemView.findViewById(R.id.wordTextView)
        val numbersTextView: TextView = itemView.findViewById(R.id.numbersTextView)
    }
}
