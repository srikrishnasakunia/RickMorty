package dev.krishna.rickmorty.ui.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.krishna.rickmorty.data.api.model.RickMortyCharacter
import dev.krishna.rickmorty.data.database.Bookmark
import dev.krishna.rickmorty.databinding.ItemCharacterBinding
import dev.krishna.rickmorty.databinding.ItemHeaderBinding

class CharacterPagingAdapter(
    private val onItemClick: (RickMortyCharacter, View) -> Unit,
    private val onBookMarkClick: (RickMortyCharacter) -> Unit
) : PagingDataAdapter<RecyclerItem, RecyclerView.ViewHolder>(CHARACTER_COMPARATOR) {

    private var bookmarks: Set<Bookmark> = emptySet()

    inner class CharacterViewHolder(
        private val binding: ItemCharacterBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(character: RickMortyCharacter, isBookmarked: Boolean) {
            binding.apply {
                this.character = character
                this.isBookmarked = isBookmarked

                root.setOnClickListener { onItemClick(character, it) }
                ibBookmark.setOnClickListener { onBookMarkClick(character) }

                executePendingBindings()
            }
        }

        fun updateBookmarkState(isBookmarked: Boolean) {
            binding.isBookmarked = isBookmarked
            binding.executePendingBindings()
        }
    }

    inner class HeaderViewHolder(
        private val binding: ItemHeaderBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.tvHeader.text = title
        }
    }

    companion object {
        const val ITEM_VIEW_TYPE_HEADER = 0
        const val ITEM_VIEW_TYPE_CHARACTER = 1
        const val IS_BOOKMARKED = "isBookmarked"

        val CHARACTER_COMPARATOR = object : DiffUtil.ItemCallback<RecyclerItem>() {
            override fun areItemsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean {
                return when {
                    oldItem is RecyclerItem.HeaderItem && newItem is RecyclerItem.HeaderItem -> oldItem.name == newItem.name
                    oldItem is RecyclerItem.CharacterItem && newItem is RecyclerItem.CharacterItem -> oldItem.character.id == newItem.character.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean {
                return when {
                    oldItem is RecyclerItem.HeaderItem && newItem is RecyclerItem.HeaderItem -> oldItem == newItem
                    oldItem is RecyclerItem.CharacterItem && newItem is RecyclerItem.CharacterItem -> oldItem.character == newItem.character
                    else -> false
                }
            }

            override fun getChangePayload(oldItem: RecyclerItem, newItem: RecyclerItem): Any? {
                if (oldItem is RecyclerItem.CharacterItem && newItem is RecyclerItem.CharacterItem) {
                    val diffBundle = Bundle()
                    if (oldItem.character.isBookmarked != newItem.character.isBookmarked) {
                        diffBundle.putBoolean(IS_BOOKMARKED, newItem.character.isBookmarked)
                    }
                    return if (diffBundle.size() > 0) diffBundle else null
                }
                return null
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder(
                ItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            ITEM_VIEW_TYPE_CHARACTER -> CharacterViewHolder(
                ItemCharacterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is RecyclerItem.HeaderItem -> ITEM_VIEW_TYPE_HEADER
            is RecyclerItem.CharacterItem -> ITEM_VIEW_TYPE_CHARACTER
            null -> throw IllegalArgumentException("Null Item at position $position")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                (getItem(position) as RecyclerItem.HeaderItem).let { holder.bind(it.name) }
            }
            is CharacterViewHolder -> {
                (getItem(position) as RecyclerItem.CharacterItem).let { character ->
                    val isBookmarked = bookmarks.any { it.characterId == character.character.id }
                    holder.bind(character.character, isBookmarked)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty() && payloads[0] is Bundle && holder is CharacterViewHolder) {
            val bundle = payloads[0] as Bundle
            if (bundle.containsKey(IS_BOOKMARKED)) {
                holder.updateBookmarkState(bundle.getBoolean(IS_BOOKMARKED))
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    fun updateBookmarks(newBookmarks: List<Bookmark>) {
        val newBookmarkSet = newBookmarks.map { it }.toSet()
        val changedPositions = mutableListOf<Int>()

        snapshot().items.forEachIndexed { index, item ->
            if (item is RecyclerItem.CharacterItem) {
                val wasBookmarked = bookmarks.any { it.characterId == item.character.id }
                val isBookmarked = newBookmarkSet.any { it.characterId == item.character.id }


                if (wasBookmarked != isBookmarked) {
                    changedPositions.add(index)
                }
            }
        }

        bookmarks = newBookmarkSet

        changedPositions.forEach { position ->
            notifyItemChanged(position, "BOOKMARK_CHANGED")
        }
    }
}

