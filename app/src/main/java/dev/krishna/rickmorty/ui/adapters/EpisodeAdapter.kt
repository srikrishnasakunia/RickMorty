package dev.krishna.rickmorty.ui.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.krishna.rickmorty.data.api.model.Episode
import dev.krishna.rickmorty.databinding.EmptyStateBinding
import dev.krishna.rickmorty.databinding.ItemEpisodeBinding

class EpisodeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val EMPTY_PLACEHOLDER = 0
        private const val EPISODE_ITEM = 1
    }

    private var items: List<EpisodeRecyclerItem> = emptyList()

    fun submitList(episodes: List<Episode>) {
        items = if (episodes.isEmpty()) {
            listOf(EpisodeRecyclerItem.EmptyPlaceholder)
        } else {
            episodes.map { EpisodeRecyclerItem.EpisodeItem(it) }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is EpisodeRecyclerItem.EmptyPlaceholder -> EMPTY_PLACEHOLDER
            is EpisodeRecyclerItem.EpisodeItem -> EPISODE_ITEM
        }
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            EMPTY_PLACEHOLDER -> EmptyViewHolder(
                EmptyStateBinding.inflate(inflater, parent, false)
            )
            EPISODE_ITEM -> EpisodeViewHolder(
                ItemEpisodeBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EpisodeViewHolder -> {
                val item = items[position] as EpisodeRecyclerItem.EpisodeItem
                holder.bind(item.episode)
            }
            is EmptyViewHolder -> {}
        }
    }

    inner class EpisodeViewHolder(private val binding: ItemEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(episode: Episode) {
            binding.tvEpisodeName.text = "${episode.episodeCode} - ${episode.name}"
        }
    }

    inner class EmptyViewHolder(binding: EmptyStateBinding) :
        RecyclerView.ViewHolder(binding.root)
}