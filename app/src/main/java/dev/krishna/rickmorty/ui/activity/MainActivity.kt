package dev.krishna.rickmorty.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.widget.addTextChangedListener
import androidx.paging.LoadState
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
        setupSearch()
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
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator().apply {
                addDuration = 200
                changeDuration = 200
                moveDuration = 200
            }
        }
        val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        binding.rvCharacters.addItemDecoration(GridSpacingItemDecoration(2, spacing, true))

        adapter.addLoadStateListener { loadState ->
            when (val state = loadState.refresh) {
                is LoadState.Error -> showError(state.error.message.toString())
                is LoadState.Loading -> if (adapter.snapshot().isEmpty()) showLoading()
                else -> hideLoading()
            }
        }
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
            Log.d("MainActivity", "State changed to $state")
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

    private fun updateUIState(
        showLoading: Boolean = false,
        showError: Boolean = false,
        errorMessage: String? = null
    ) {
        binding.apply {
            loadingState.root.visibility = if (showLoading) View.VISIBLE else View.GONE
            errorState.root.visibility = if (showError) View.VISIBLE else View.GONE
            rvCharacters.visibility = if (!showLoading && !showError) View.VISIBLE else View.GONE
            emptyState.root.visibility = View.GONE

            errorMessage?.let {
                errorState.tvError.text = it
            }
        }
    }

    private fun showLoading() {
        updateUIState(showLoading = true)
    }

    private fun hideLoading() {
        updateUIState()
    }

    private fun showCharacters(characters: PagingData<RecyclerItem>) {
        binding.apply {
            container.visibility = View.VISIBLE
            emptyState.root.visibility = View.GONE
            errorState.root.visibility = View.GONE
            rvCharacters.visibility = View.VISIBLE
        }
        adapter.submitData(lifecycle, characters)
        hideLoading()
    }

    private fun showError(message: String) {
        updateUIState(showError = true, errorMessage = message)
        binding.apply {
            errorState.btnRetry.setOnClickListener {
                etSearch.text.clear()
                filterChipGroup.visibility = View.VISIBLE
                ivSearch.visibility = View.VISIBLE
                etSearch.visibility = View.GONE
                viewModel.clearFilters()
            }
        }
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
                    scrollToTop()
                }
            })
        }
    }

    private fun setupSearch() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        binding.ivSearch.setOnClickListener {
            Log.d("MainActivity", "setupSearch: Clicked")
            binding.apply {
                filterChipGroup.visibility = View.GONE
                ivSearch.visibility = View.GONE
                etSearch.visibility = View.VISIBLE
                etSearch.requestFocus()
            }
            imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.etSearch.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP){
                val drawableEnd = binding.etSearch.compoundDrawables[2]
                if (drawableEnd != null &&
                    event.rawX >= binding.etSearch.right - binding.etSearch.paddingEnd -
                    drawableEnd.bounds.width()) {
                    binding.apply {
                        etSearch.text.clear()
                        filterChipGroup.visibility = View.VISIBLE
                        ivSearch.visibility = View.VISIBLE
                        etSearch.visibility = View.GONE
                    }
                    imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
                    return@setOnTouchListener true
                }
            }
            false
        }

        binding.etSearch.addTextChangedListener { text ->
            viewModel.setSearchQuery(text.toString())
        }
    }

    private fun setupFab() {
        binding.fabScrollTop.apply {
            hide()
            setOnClickListener {
                scrollToTop()
                binding.fabScrollTop.visibility = View.GONE
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

    private fun scrollToTop() {
        binding.rvCharacters.smoothScrollToPosition(0)
    }
}