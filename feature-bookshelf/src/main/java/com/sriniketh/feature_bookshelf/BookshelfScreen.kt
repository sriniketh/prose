package com.sriniketh.feature_bookshelf

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sriniketh.core_design.ui.components.gradientPlaceholder
import com.sriniketh.core_design.ui.theme.AppTheme
import com.sriniketh.core_platform.buildHttpsUri
import kotlinx.coroutines.launch

@Composable
fun BookshelfScreen(
    modifier: Modifier = Modifier,
    viewModel: BookshelfViewModel = viewModel(),
    goToSearch: () -> Unit,
    goToHighlight: (String) -> Unit
) {
    val uiState: BookshelfUIState by viewModel.bookshelfUIState.collectAsStateWithLifecycle()
    Bookshelf(
        uiState = uiState,
        modifier = modifier,
        goToSearch = { goToSearch() },
        goToHighlight = { goToHighlight(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Bookshelf(
    uiState: BookshelfUIState,
    modifier: Modifier = Modifier,
    goToSearch: () -> Unit,
    goToHighlight: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.bookshelf_pagetitle),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = goToSearch,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(id = R.string.search_fab_cont_desc)
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { contentPadding ->
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = modifier.fillMaxWidth())
        }

        if (uiState.books.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = modifier.fillMaxSize()
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(id = R.string.bookshelf_empty_text),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else if (uiState.books.isNotEmpty()) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                itemsIndexed(uiState.books) { index, bookUIState ->
                    Card(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .clickable { goToHighlight(bookUIState.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Row(
                            modifier = modifier
                                .padding(12.dp)
                                .align(Alignment.Start)
                        ) {
                            val uri = bookUIState.thumbnailLink?.buildHttpsUri()
                            AsyncImage(
                                modifier = modifier
                                    .padding(6.dp)
                                    .height(100.dp)
                                    .width(80.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                model = uri,
                                contentScale = ContentScale.Crop,
                                contentDescription = null,
                                placeholder = gradientPlaceholder(),
                                error = gradientPlaceholder()
                            )
                            Column(
                                modifier = modifier
                                    .padding(horizontal = 6.dp)
                                    .align(Alignment.Top)
                            ) {
                                Text(
                                    modifier = modifier.padding(6.dp),
                                    text = bookUIState.title,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    modifier = modifier.padding(6.dp),
                                    text = bookUIState.authors.joinToString(", "),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        uiState.snackBarText?.let { message ->
            val errorMessage = stringResource(id = message)
            LaunchedEffect(key1 = null) {
                launch {
                    snackbarHostState.showSnackbar(errorMessage)
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
internal fun BookshelfScreenSuccessPreview() {
    AppTheme {
        Bookshelf(
            uiState = BookshelfUIState(
                books = listOf(BookUIState(
                    id = "someId",
                    title = "Some title 1",
                    authors = listOf("Author 1", "Author 2", "Author 3"),
                    thumbnailLink = "https://picsum.photos/200/300",
                    viewBook = {}
                ), BookUIState(
                    id = "someId2",
                    title = "Some title 2",
                    authors = listOf("Author 1"),
                    thumbnailLink = "https://picsum.photos/200/300",
                    viewBook = {}
                ), BookUIState(
                    id = "someId3",
                    title = "Some title 3",
                    authors = listOf("Author 1", "Author 2"),
                    thumbnailLink = "https://picsum.photos/200/300",
                    viewBook = {}
                ), BookUIState(
                    id = "someId4",
                    title = "Some title 4",
                    authors = listOf("Author 1", "Author 2"),
                    thumbnailLink = "https://picsum.photos/200/300",
                    viewBook = {}
                ), BookUIState(
                    id = "someId5",
                    title = "Some title 5",
                    authors = listOf("Author 1", "Author 2"),
                    thumbnailLink = "https://picsum.photos/200/300",
                    viewBook = {}
                ), BookUIState(
                    id = "someId6",
                    title = "Some title 6",
                    authors = listOf("Author 1", "Author 2"),
                    thumbnailLink = "https://picsum.photos/200/300",
                    viewBook = {}
                ), BookUIState(
                    id = "someId7",
                    title = "Some title 7",
                    authors = listOf("Author 1", "Author 2"),
                    thumbnailLink = "https://picsum.photos/200/300",
                    viewBook = {}
                ))),
            goToSearch = {}, goToHighlight = {})
    }
}

@PreviewLightDark
@Composable
internal fun BookshelfScreenLoadingPreview() {
    AppTheme {
        Bookshelf(
            uiState = BookshelfUIState(isLoading = true),
            goToSearch = {}, goToHighlight = {})
    }
}

@PreviewLightDark
@Composable
internal fun BookshelfScreenSuccessNoBooksPreview() {
    AppTheme {
        Bookshelf(
            uiState = BookshelfUIState(books = emptyList()),
            goToSearch = {}, goToHighlight = {})
    }
}

@PreviewLightDark
@Composable
internal fun BookshelfScreenFailurePreview() {
    AppTheme {
        Bookshelf(
            uiState = BookshelfUIState(snackBarText = R.string.getallbooks_error_message),
            goToSearch = {}, goToHighlight = {})
    }
}
