package com.mikeni.tictactoe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mikeni.tictactoe.model.Match
import com.mikeni.tictactoe.view.RoomFragmentDirections
import kotlinx.android.synthetic.main.item_match.view.*

class MatchesAdapter : ListAdapter<Match, MatchesAdapter.ViewHolder>(RoomDiffCallback) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Match) {
            itemView.matchId.text = item.host.nickname
            itemView.setOnClickListener {
                it.findNavController()
                    .navigate(RoomFragmentDirections.actionToGameFragment(item.id, false))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_match, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object RoomDiffCallback : DiffUtil.ItemCallback<Match>() {
        override fun areItemsTheSame(oldItem: Match, newItem: Match): Boolean =
            oldItem.host == newItem.host

        override fun areContentsTheSame(oldItem: Match, newItem: Match): Boolean =
            oldItem == newItem
    }
}