package dev.krishna.rickmorty.ui.adapters

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

    private var bookmarks: List<Bookmark> = emptyList()

    inner class CharacterViewHolder(
        private val binding: ItemCharacterBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(character: RickMortyCharacter, isBookmarked: Boolean) {
            binding.apply {
                this.character = character
                this.isBookmarked = isBookmarked

                root.setOnClickListener {
                    onItemClick(character, it)
                }

                ibBookmark.setOnClickListener {
                    onBookMarkClick(character)
                }

                executePendingBindings()
            }
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

        val CHARACTER_COMPARATOR = object : DiffUtil.ItemCallback<RecyclerItem>() {
            override fun areItemsTheSame(
                oldItem: RecyclerItem,
                newItem: RecyclerItem
            ): Boolean {
                return when {
                    oldItem is RecyclerItem.HeaderItem && newItem is RecyclerItem.HeaderItem -> {
                        oldItem.name == newItem.name
                    }

                    oldItem is RecyclerItem.CharacterItem && newItem is RecyclerItem.CharacterItem -> {
                        oldItem.character.id == newItem.character.id
                    }

                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: RecyclerItem,
                newItem: RecyclerItem
            ): Boolean {
                return when {
                    oldItem is RecyclerItem.HeaderItem && newItem is RecyclerItem.HeaderItem -> {
                        oldItem == newItem
                    }

                    oldItem is RecyclerItem.CharacterItem && newItem is RecyclerItem.CharacterItem -> {
                        oldItem.character == newItem.character
                    }

                    else -> false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder(
                ItemHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            ITEM_VIEW_TYPE_CHARACTER -> CharacterViewHolder(
                ItemCharacterBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
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
            is HeaderViewHolder -> bindHeaderViewHolder(holder, position)
            is CharacterViewHolder -> bindCharacterViewHolder(holder, position)
        }
    }

    private fun bindHeaderViewHolder(holder: HeaderViewHolder, position: Int) {
        (getItem(position) as RecyclerItem.HeaderItem).let { header->
            holder.bind(header.name)
        }
    }

    private fun bindCharacterViewHolder(holder: CharacterViewHolder, position: Int) {
        (getItem(position) as RecyclerItem.CharacterItem).let { character ->
            val isBookmarked = bookmarks.any { it.characterId == character.character.id }
            holder.bind(character.character, isBookmarked)
        }
    }

        fun updateBookmarks(newBookmarks: List<Bookmark>) {
            val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = bookmarks.size

                override fun getNewListSize(): Int = newBookmarks.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                    bookmarks[oldItemPosition].characterId == newBookmarks[newItemPosition].characterId


                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean =
                    bookmarks[oldItemPosition] == newBookmarks[newItemPosition]
            })
            bookmarks = newBookmarks
            diff.dispatchUpdatesTo(this)
        }
    }
