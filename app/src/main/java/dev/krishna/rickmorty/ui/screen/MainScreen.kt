package dev.krishna.rickmorty.ui.screen

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import dev.krishna.rickmorty.R
import dev.krishna.rickmorty.data.api.model.Filters
import dev.krishna.rickmorty.data.api.model.RickMortyCharacter
import dev.krishna.rickmorty.ui.viewmodel.CharacterViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CharacterViewModel,
    onCharacterClicked: (Int) -> Unit
) {
    val characters = viewModel.characters.collectAsLazyPagingItems()
    val lazyGridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            HomeTopAppBar(
                viewModel = viewModel,
                onSearchQueryChanged = { query -> viewModel.setSearchQuery(query) },
                onFilterChanged = { filters -> viewModel.applyFilters(filters) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                scope.launch { lazyGridState.animateScrollToItem(0) }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_up),
                    contentDescription = stringResource(id = R.string.scroll_up)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (characters.loadState.refresh) {
                is LoadState.Loading -> {
                    LoadingState(modifier = Modifier.align(Alignment.Center))
                }
                is LoadState.Error -> {
                    val error = characters.loadState.refresh as LoadState.Error
                    ErrorState(
                        message = error.error.localizedMessage ?: "An error occurred",
                        onRetry = { characters.retry() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    if (characters.itemCount == 0) {
                        EmptyState(modifier = Modifier.align(Alignment.Center))
                    } else {
                        CharacterGrid(
                            characters = characters,
                            state = lazyGridState,
                            onCharacterClicked = { characterId ->
                                if (isOnline) {
                                    onCharacterClicked(characterId)
                                } else {
                                    Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onBookmarkClicked = { viewModel.toggleBookmark(it) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    viewModel: CharacterViewModel,
    onSearchQueryChanged: (String) -> Unit,
    onFilterChanged: (Filters) -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()

    TopAppBar(
        title = { 
            if (!isSearchActive) {
                Text(text = stringResource(id = R.string.app_name)) 
            }
        },
        actions = {
            if (isSearchActive) {
                TextField(
                    value = searchQuery,
                    onValueChange = { onSearchQueryChanged(it) },
                    label = { Text(stringResource(id = R.string.search_hint)) },
                    trailingIcon = {
                        IconButton(onClick = { 
                            onSearchQueryChanged("")
                            isSearchActive = false 
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(id = R.string.clear_search))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChips(selectedFilter = filters, onFilterChanged = onFilterChanged)
                    IconButton(onClick = { isSearchActive = true }) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(id = R.string.search))
                    }
                }
            }
        }
    )
}

@Composable
fun FilterChips(selectedFilter: Filters, onFilterChanged: (Filters) -> Unit) {
    val statuses = listOf("Alive", "Dead", "unknown")
    Row {
        statuses.forEach { status ->
            FilterChip(
                selected = selectedFilter.status == status,
                onClick = { 
                    val newFilter = if (selectedFilter.status == status) Filters() else Filters(status = status)
                    onFilterChanged(newFilter)
                },
                label = { Text(text = status) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun CharacterGrid(
    characters: LazyPagingItems<RickMortyCharacter>,
    state: LazyGridState,
    onCharacterClicked: (Int) -> Unit,
    onBookmarkClicked: (RickMortyCharacter) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = state,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(characters.itemCount) { index ->
            characters[index]?.let {
                CharacterCard(
                    character = it,
                    onCharacterClicked = { onCharacterClicked(it.id) },
                    onBookmarkClicked = onBookmarkClicked
                )
            }
        }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier = modifier)
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 8.dp)) {
            Text(text = stringResource(id = R.string.retry))
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = stringResource(id = R.string.no_offline_data))
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CharacterCard(
    character: RickMortyCharacter,
    onCharacterClicked: () -> Unit,
    onBookmarkClicked: (RickMortyCharacter) -> Unit
) {
    val cardBorderColor = when (character.status) {
        "Alive" -> Color.Green
        "Dead" -> Color.Red
        else -> Color.Gray
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = cardBorderColor,
                shape = MaterialTheme.shapes.medium
            )
            .clickable { onCharacterClicked() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.height(180.dp)) {
                GlideImage(
                    model = character.image,
                    contentDescription = character.name,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { onBookmarkClicked(character) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        painter = painterResource(id = if (character.isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline),
                        contentDescription = stringResource(id = R.string.bookmark),
                        tint = Color.White
                    )
                }
            }
            Text(
                text = character.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
