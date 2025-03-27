package dev.krishna.rickmorty.ui.activity

import android.os.Bundle
import android.transition.ChangeBounds
import android.util.Log
import android.view.View
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import dagger.hilt.android.AndroidEntryPoint
import dev.krishna.rickmorty.R
import dev.krishna.rickmorty.data.api.model.Episode
import dev.krishna.rickmorty.data.api.model.RickMortyCharacter
import dev.krishna.rickmorty.databinding.ActivityCharacterDetailsBinding
import dev.krishna.rickmorty.ui.adapters.EpisodeAdapter
import dev.krishna.rickmorty.ui.state.UIState
import dev.krishna.rickmorty.ui.viewmodel.CharacterDetailsViewModel
import kotlin.properties.Delegates

@AndroidEntryPoint
class CharacterDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCharacterDetailsBinding
    private val viewModel: CharacterDetailsViewModel by viewModels()

    private var characterId by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCharacterDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        characterId = intent.getIntExtra("CHARACTER_ID", -1)
        Log.d("CharacterDetailsActivity", "onCreate: $characterId")

        setupToolbar()
        setupSharedElementTransition()

        if (characterId != -1) {
            viewModel.loadCharacterDetails(characterId)
        }

        observeViewModel()

    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupSharedElementTransition() {
        val characterImage = binding.ivCharacter
        ViewCompat.setTransitionName(characterImage, getString(R.string.transition_character_image))
        supportPostponeEnterTransition()
        characterImage.doOnPreDraw { supportStartPostponedEnterTransition() }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { character ->
            when(character){
                is UIState.Success -> {
                    binding.container.visibility = View.VISIBLE
                    binding.loadingState.root.visibility = View.GONE
                    binding.errorState.root.visibility = View.GONE
                    binding.tvEpisodeCount.text = getString(R.string.episodes_count, character.data.episode.size)
                }
                is UIState.Error -> {
                    binding.container.visibility = View.GONE
                    binding.loadingState.root.visibility = View.GONE
                    binding.errorState.root.visibility = View.VISIBLE
                    binding.errorState.tvError.text = character.message
                    binding.errorState.btnRetry.setOnClickListener {
                        viewModel.loadCharacterDetails(characterId)
                    }
                }
                is UIState.Loading -> {
                    binding.loadingState.root.visibility = View.VISIBLE
                    binding.container.visibility = View.GONE
                    binding.errorState.root.visibility = View.GONE
                }
            }
        }

        viewModel.episodes.observe(this) { episodes ->
            setupEpisodesRecyclerView()
        }
    }

    private fun setupEpisodesRecyclerView() {
        binding.recyclerViewEpisodes.apply {
            layoutManager = LinearLayoutManager(this@CharacterDetailsActivity)
            adapter = EpisodeAdapter(viewModel.episodes.value ?: emptyList())
            addItemDecoration(DividerItemDecoration(this@CharacterDetailsActivity, DividerItemDecoration.VERTICAL))

            postDelayed({ smoothScrollToPosition(0) }, 300)
        }
    }
}