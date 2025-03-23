package dev.krishna.rickmorty.ui.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.paging.PagingData
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import dagger.hilt.android.AndroidEntryPoint
import dev.krishna.rickmorty.data.api.model.Character
import dev.krishna.rickmorty.data.api.model.RickMortyCharacter
import dev.krishna.rickmorty.databinding.ActivityMainBinding
import dev.krishna.rickmorty.ui.adapters.CharacterPagingAdapter
import dev.krishna.rickmorty.ui.adapters.RecyclerItem
import dev.krishna.rickmorty.ui.adapters.StickyHeaderItemDecoration
import dev.krishna.rickmorty.ui.state.UIState
import dev.krishna.rickmorty.ui.viewmodel.CharacterViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CharacterViewModel by viewModels()
    private lateinit var adapter: CharacterPagingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setUpCharacterRecyclerView() {
        adapter = CharacterPagingAdapter(
            onItemClick = { character, view -> openDetailsScreen(character, view) },
            onBookMarkClick = { character -> viewModel.toggleBookmark(character) }
        )
        binding.rvCharacters.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2).apply {
                spanSizeLookup = object : SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (
                            adapter?.getItemViewType(position) == CharacterPagingAdapter.ITEM_VIEW_TYPE_HEADER
                        )
                            2
                        else
                            1
                    }
                }
            }
            adapter = this@MainActivity.adapter
            addItemDecoration(StickyHeaderItemDecoration())
            itemAnimator = DefaultItemAnimator().apply {
                addDuration = 200
                changeDuration = 200
                moveDuration = 200
            }
        }
    }

    private fun openDetailsScreen(character: RickMortyCharacter, view: View) {

    }

    private fun setUpObservers() {
        viewModel.uiState.observe(this) { state->
            when(state) {
                is UIState.Loading -> showLoading()
                is UIState.Success -> showCharacters(state.data)
                is UIState.Error -> showError(state.message)
            }
        }

        viewModel.bookmarks.observe(this) {
            adapter.updateBookmarks(it)
        }
    }

    private fun showLoading() {
        binding.loadingState.root.visibility = View.VISIBLE
        binding.rvCharacters.visibility = View.GONE
        binding.emptyState.root.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.loadingState.root.visibility = View.GONE
    }

    private fun showCharacters(characters: PagingData<RecyclerItem>) {
        binding.emptyState.root.visibility = View.GONE
        binding.rvCharacters.visibility = View.VISIBLE
        adapter.submitData(lifecycle, characters)
        hideLoading()
    }

    private fun showError(message: String) {
        binding.emptyState.root.visibility = View.VISIBLE
        binding.emptyState.tvEmpty.text = message ?: getString(R.string.unknown_error)
        binding.rvCharacters.visibility = View.GONE
        hideLoading()
    }

    private fun setupSearch() {
        binding.searchView.editText.doAfterTextChanged { text ->
            if (text?.length ?: 0 >= 3) {
                viewModel.searchCharacters(text.toString())
                binding.rvCharacters.smoothScrollToPosition(0)
            } else if (text.isNullOrEmpty()) {
                viewModel.clearSearch()
            }
        }
    }

}