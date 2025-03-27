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

@AndroidEntryPoint
class CharacterDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCharacterDetailsBinding
    private val viewModel: CharacterDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCharacterDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val characterId = intent.getIntExtra("CHARACTER_ID", -1)
        Log.d("CharacterDetailsActivity", "onCreate: $characterId")

        setupToolbar()
        setupSharedElementTransition()

        if (characterId != -1) {
            viewModel.loadCharacterDetails(characterId)
        }

        observeViewModel()
        //setupEpisodesCard()

    }

    private fun setupToolbar() {
        //setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupSharedElementTransition() {
        val characterImage = binding.ivCharacter
        ViewCompat.setTransitionName(characterImage, getString(R.string.transition_character_image))
        supportPostponeEnterTransition()
        characterImage.doOnPreDraw { supportStartPostponedEnterTransition() }
    }

//    private fun setupViews(character: RickMortyCharacter) {
//        //binding.collapsingToolbar.title = character.name
//        Glide.with(this)
//            .load(character.image)
//            .transition(DrawableTransitionOptions.withCrossFade())
//            .into(binding.ivCharacter)
//
//        binding.tvName.text = character.name
//        binding.tvStatus.text = "Status: ${character.status}"
//    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { character ->
            when(character){
                is UIState.Success -> {
                    //binding.loadingState.root.visibility = View.GONE
                    binding.tvEpisodeCount.text = getString(R.string.episodes_count, character.data.episode.size)
                }
                is UIState.Error -> {
//                    binding.loadingState.root.visibility = View.GONE
//                    binding.errorState.tvError.text = character.message
                }
                is UIState.Loading -> {
//                    binding.loadingState.root.visibility = View.VISIBLE
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

            // Smooth scroll to top when expanded
            postDelayed({ smoothScrollToPosition(0) }, 300)
        }
    }

//    private fun setupEpisodesCard() {
//        binding.cardEpisodes.setOnClickListener {
//            // Toggle card expansion
//            val layoutParams = binding.rvEpisodes.layoutParams
//            layoutParams.height = if (binding.rvEpisodes.visibility == View.VISIBLE) {
//                binding.rvEpisodes.visibility = View.GONE
//                0
//            } else {
//                binding.rvEpisodes.visibility = View.VISIBLE
//                ViewGroup.LayoutParams.WRAP_CONTENT
//            }
//            binding.rvEpisodes.layoutParams = layoutParams
//        }
//    }
}