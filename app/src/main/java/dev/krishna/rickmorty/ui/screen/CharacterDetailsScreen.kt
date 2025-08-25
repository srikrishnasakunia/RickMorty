package dev.krishna.rickmorty.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import dev.krishna.rickmorty.R
import dev.krishna.rickmorty.data.api.model.Episode
import dev.krishna.rickmorty.data.api.model.RickMortyCharacter
import dev.krishna.rickmorty.ui.viewmodel.CharacterDetailsViewModel

@Composable
fun CharacterDetailsScreen(viewModel: CharacterDetailsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }
            uiState.error != null -> {
                Text(text = uiState.error ?: "An unknown error occurred.")
            }
            uiState.character != null -> {
                uiState.character?.let { character ->
                    val isExpanded by viewModel.isExpanded.collectAsStateWithLifecycle()
                    val episodes = uiState.episodes
                    val statusColor = when (character.status) {
                        "Alive" -> Color.Green
                        "Dead" -> Color.Red
                        else -> Color.Gray
                    }

                    if (isExpanded) {
                        ExpandedCharacterView(
                            character = character,
                            episodes = episodes,
                            statusColor = statusColor,
                            onClose = { viewModel.toggleExpand() }
                        )
                    } else {
                        CollapsedCharacterView(
                            character = character,
                            statusColor = statusColor,
                            onExpand = { viewModel.toggleExpand() }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CollapsedCharacterView(
    character: RickMortyCharacter,
    statusColor: Color,
    onExpand: () -> Unit
) {
    Card(
        modifier = Modifier.clickable { onExpand() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GlideImage(
                model = character.image,
                contentDescription = character.name,
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
            )
            Text(
                text = character.name,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = stringResource(id = R.string.episodes_count, character.episode.size),
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ExpandedCharacterView(
    character: RickMortyCharacter,
    episodes: List<Episode>,
    statusColor: Color,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(statusColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
            IconButton(onClick = onClose, modifier = Modifier.padding(16.dp)) {
                Icon(painter = painterResource(id = R.drawable.ic_close_white), contentDescription = "Close")
            }
            GlideImage(
                model = character.image,
                contentDescription = character.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
            Text(
                text = character.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White)) {
                items(episodes) { episode ->
                    EpisodeListItem(episode = episode)
                }
            }
        }
    }
}

@Composable
fun EpisodeListItem(episode: Episode) {
    Text(
        text = episode.name,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
}
