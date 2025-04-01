package dev.krishna.rickmorty.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.krishna.rickmorty.R
import dev.krishna.rickmorty.databinding.ActivityCharacterDetailsBinding
import dev.krishna.rickmorty.ui.adapters.EpisodeAdapter
import dev.krishna.rickmorty.ui.state.UIState
import dev.krishna.rickmorty.ui.viewmodel.CharacterDetailsViewModel
import kotlin.properties.Delegates

@AndroidEntryPoint
class CharacterDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCharacterDetailsBinding
    private val characterDetailsViewModel: CharacterDetailsViewModel by viewModels()

    private lateinit var episodeAdapter: EpisodeAdapter

    private var characterId by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCharacterDetailsBinding.inflate(layoutInflater).apply {
            setContentView(root)
            lifecycleOwner = this@CharacterDetailsActivity
            viewModel = characterDetailsViewModel
        }

        characterId = intent.getIntExtra("CHARACTER_ID", -1)
        Log.d("CharacterDetailsActivity", "onCreate: $characterId")

        setupSharedElementTransition()

        if (characterId != -1) {
            characterDetailsViewModel.loadCharacterDetails(characterId)
        }

        setupEpisodesRecyclerView()
        observeViewModel()

    }

    private fun setupSharedElementTransition() {
        val characterImage = binding.ivCharacter
        ViewCompat.setTransitionName(characterImage, getString(R.string.transition_character_image))
        supportPostponeEnterTransition()
        characterImage.doOnPreDraw { supportStartPostponedEnterTransition() }
    }

    private fun observeViewModel() {
        characterDetailsViewModel.uiState.observe(this) { character ->
            when(character){
                is UIState.Success -> {
                    binding.apply {
                        container.visibility = View.VISIBLE
                        loadingState.root.visibility = View.GONE
                        errorState.root.visibility = View.GONE
                        tvEpisodeCount.text = getString(R.string.episodes_count, character.data.episode.size)
                    }
                }
                is UIState.Error -> {
                     binding.apply{
                        container.visibility = View.GONE
                        loadingState.root.visibility = View.GONE
                        errorState.root.visibility = View.VISIBLE
                        errorState.tvError.text = character.message
                        errorState.btnRetry.setOnClickListener {
                            characterDetailsViewModel.loadCharacterDetails(characterId)
                        }
                    }
                }
                is UIState.Loading -> {
                    binding.apply {
                        container.visibility = View.GONE
                        loadingState.root.visibility = View.VISIBLE
                        errorState.root.visibility = View.GONE
                    }
                }
            }
        }

        characterDetailsViewModel.episodes.observe(this) { episodes ->
            episodeAdapter.submitList(episodes)
            binding.recyclerViewEpisodes.postDelayed({ binding.recyclerViewEpisodes.smoothScrollToPosition(0) }, 300)
        }
    }

    private fun setupEpisodesRecyclerView() {
        episodeAdapter = EpisodeAdapter()
        binding.recyclerViewEpisodes.apply {
            layoutManager = LinearLayoutManager(this@CharacterDetailsActivity)
            adapter = episodeAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }
}