package dev.krishna.rickmorty.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingData
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import dev.krishna.rickmorty.R
import dev.krishna.rickmorty.data.api.model.Filters
import dev.krishna.rickmorty.data.api.model.RickMortyCharacter
import dev.krishna.rickmorty.databinding.ActivityMainBinding
import dev.krishna.rickmorty.ui.adapters.CharacterPagingAdapter
import dev.krishna.rickmorty.ui.adapters.GridSpacingItemDecoration
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
        setUpCharacterRecyclerView()
        setUpObservers()
        setupFilters()
        setupFab()
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
        val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        binding.rvCharacters.addItemDecoration(GridSpacingItemDecoration(2, spacing, true))
    }

    private fun openDetailsScreen(character: RickMortyCharacter, view: View) {
        val intent = Intent(this, CharacterDetailsActivity::class.java).apply {
            putExtra("CHARACTER_ID", character.id)
        }

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            Pair(view.findViewById<ImageView>(R.id.ivCharacter), getString(R.string.transition_character_image))
        )
        startActivity(intent, options.toBundle())
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

    private fun setupFilters() {
        listOf("Alive", "Dead", "unknown").forEach { status ->
            binding.filterChipGroup.addView(Chip(this).apply {
                text = status
                isCheckable = true
                isChecked = viewModel.filters.value?.status == status
                setOnCheckedChangeListener { _, checked ->
                    viewModel.applyFilters(
                        if (checked) Filters(status = status) else Filters()
                    )
                }
            })
        }
    }

    private fun setupFab() {
        binding.fabScrollTop.apply {
            hide()
            setOnClickListener {
                binding.rvCharacters.smoothScrollToPosition(0)
            }
        }

        binding.rvCharacters.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 20) {
                    binding.fabScrollTop.show()
                } else if (dy < -5) {
                    binding.fabScrollTop.hide()
                }
            }
        })
    }
}