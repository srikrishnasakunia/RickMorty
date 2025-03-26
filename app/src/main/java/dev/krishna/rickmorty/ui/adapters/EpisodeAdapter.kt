package dev.krishna.rickmorty.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.krishna.rickmorty.data.api.model.Episode
import dev.krishna.rickmorty.databinding.ItemEpisodeBinding

class EpisodeAdapter(private val episodes: List<Episode>) :
    RecyclerView.Adapter<EpisodeAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEpisodeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val episode = episodes[position]
        holder.binding.apply {
            tvEpisodeName.text =  "${episode.episodeCode} - ${episode.name}"
        }
    }

    override fun getItemCount() = episodes.size
}