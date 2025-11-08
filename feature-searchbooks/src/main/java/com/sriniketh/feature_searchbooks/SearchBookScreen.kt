package com.sriniketh.feature_searchbooks

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sriniketh.core_design.ui.components.gradientPlaceholder
import com.sriniketh.core_design.ui.theme.AppTheme
import com.sriniketh.core_platform.buildHttpsUri

@Composable
fun SearchBookScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchBookViewModel = hiltViewModel(),
    goToBookInfo: (String) -> Unit
) {
    val uiState: BookSearchUiState by viewModel.searchUiState.collectAsStateWithLifecycle()
    SearchBook(
        modifier = modifier,
        uiState = uiState,
        searchForBooks = { query ->
            viewModel.searchForBook(query)
        },
        navigateToBookInfo = { volumeId ->
            goToBookInfo(volumeId)
        },
        resetSearch = {
            viewModel.resetSearch()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchBook(
    uiState: BookSearchUiState,
    modifier: Modifier = Modifier,
    searchForBooks: (String) -> Unit,
    navigateToBookInfo: (String) -> Unit,
    resetSearch: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var text by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { contentPadding ->
        SearchBar(
            modifier = modifier
                .fillMaxWidth()
                .padding(contentPadding)
                .focusRequester(focusRequester),
            inputField = {
                SearchBarDefaults.InputField(
                    query = text,
                    onQueryChange = {
                        text = it
                        if (text.length > 3) {
                            searchForBooks(text)
                        }
                    },
                    onSearch = {
                        expanded = false
                        if (text.length > 3) {
                            searchForBooks(text)
                        }
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = {
                        Text(text = stringResource(R.string.search_for_a_book))
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(com.sriniketh.core_design.R.drawable.ic_search),
                            contentDescription = stringResource(id = R.string.search_icon_cont_desc)
                        )
                    },
                    trailingIcon = {
                        if (expanded) {
                            Icon(
                                modifier = modifier.clickable {
                                    if (text.isNotEmpty()) {
                                        text = ""
                                        resetSearch()
                                    } else {
                                        expanded = false
                                    }
                                },
                                painter = painterResource(com.sriniketh.core_design.R.drawable.ic_close),
                                contentDescription = stringResource(id = R.string.close_icon_cont_desc)
                            )
                        }
                    }
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
            colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = modifier
                        .fillMaxWidth()
                        .testTag("SearchBookLoadingIndicator")
                )
            }
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                itemsIndexed(uiState.bookUiStates) { _, item ->
                    Row(
                        modifier = modifier
                            .padding(12.dp)
                            .align(Alignment.Start)
                            .fillMaxWidth()
                            .clickable { navigateToBookInfo(item.id) }
                    ) {
                        val uri = item.thumbnailLink?.buildHttpsUri()
                        AsyncImage(
                            modifier = modifier
                                .padding(6.dp)
                                .height(80.dp)
                                .width(60.dp)
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
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                modifier = modifier.padding(6.dp),
                                text = item.authors.joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                modifier = modifier.padding(6.dp),
                                text = item.subtitle.orEmpty(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    HorizontalDivider(modifier = modifier.fillMaxWidth())
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@PreviewLightDark
@Composable
internal fun SearchBookScreenPreview() {
    AppTheme {
        Surface {
            SearchBook(
                uiState = BookSearchUiState(
                    bookUiStates = listOf(
                        BookUiState(
                            id = "some book id",
                            title = "some book",
                            subtitle = "some subtitle",
                            authors = listOf(""),
                            thumbnailLink = null
                        )
                    )
                ),
                searchForBooks = {},
                navigateToBookInfo = {},
                resetSearch = {}
            )
        }
    }
}
