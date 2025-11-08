package com.sriniketh.feature_searchbooks

import android.text.Html
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sriniketh.core_design.ui.components.NavigationBack
import com.sriniketh.core_design.ui.components.gradientPlaceholder
import com.sriniketh.core_design.ui.theme.AppTheme
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.BookInfo
import com.sriniketh.core_platform.buildHttpsUri
import kotlinx.coroutines.launch

@Composable
fun BookInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: BookInfoViewModel = hiltViewModel(),
    bookId: String,
    goBack: () -> Unit
) {
    LaunchedEffect(key1 = bookId) {
        viewModel.getBookDetail(bookId)
    }
    val uiState: BookInfoUiState by viewModel.uiState.collectAsStateWithLifecycle()
    BookInfo(
        modifier = modifier,
        uiState = uiState,
        goBack = { goBack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookInfo(
    uiState: BookInfoUiState,
    modifier: Modifier = Modifier,
    goBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { BookInfoScreenTitle(uiState) },
                navigationIcon = { NavigationBack(goBack) },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (uiState.canAddToShelf) {
                BookInfoScreenFloatingActionButton(uiState.addBookToShelf)
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { contentPadding ->
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = modifier
                    .fillMaxWidth()
                    .testTag("BookInfoLoadingIndicator")
            )
        }

        uiState.snackBarText?.let { resId ->
            val snackbarMessage = stringResource(id = resId)
            LaunchedEffect(key1 = null) {
                launch {
                    snackbarHostState.showSnackbar(snackbarMessage)
                }
            }
        }

        BookInfoLayout(uiState, modifier, contentPadding)
    }
}

@Composable
private fun BookInfoLayout(
    uiState: BookInfoUiState,
    modifier: Modifier,
    contentPadding: PaddingValues
) {
    uiState.book?.let { book ->
        val bookInfo = book.info
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            Row(
                modifier = modifier
                    .padding(12.dp)
                    .align(Alignment.Start)
            ) {
                val uri = bookInfo.thumbnailLink?.buildHttpsUri()
                AsyncImage(
                    modifier = modifier
                        .padding(6.dp)
                        .height(160.dp)
                        .width(120.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    model = uri,
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    placeholder = gradientPlaceholder(),
                    error = gradientPlaceholder()
                )
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            modifier = modifier.padding(6.dp),
                            text = bookInfo.authors.joinToString(", "),
                            style = MaterialTheme.typography.titleLarge
                        )
                        bookInfo.publisher?.let { publisher ->
                            Text(
                                modifier = modifier.padding(6.dp),
                                text = publisher,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            bookInfo.description?.let { description ->
                Card(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Text(
                        modifier = modifier.padding(12.dp),
                        text = Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY)
                            .toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = modifier.padding(12.dp)) {
                    val averageRating = bookInfo.averageRating
                    val ratingsCount = bookInfo.ratingsCount
                    if (averageRating != null && ratingsCount != null) {
                        Text(
                            modifier = modifier.padding(6.dp),
                            text = stringResource(
                                id = R.string.book_info_ratings_template,
                                averageRating,
                                ratingsCount
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    bookInfo.pageCount?.let {
                        Text(
                            modifier = modifier.padding(6.dp),
                            text = stringResource(
                                id = R.string.book_info_pagecount_template,
                                it
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    bookInfo.publisher?.let {
                        Text(
                            modifier = modifier.padding(6.dp),
                            text = stringResource(
                                id = R.string.book_info_publisher_template,
                                it
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    bookInfo.publishedDate?.let {
                        Text(
                            modifier = modifier.padding(6.dp),
                            text = stringResource(
                                id = R.string.book_info_publish_date_template,
                                it
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookInfoScreenFloatingActionButton(buttonOnClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    ExtendedFloatingActionButton(
        onClick = {
            buttonOnClick()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        icon = {
            Icon(
                painter = painterResource(com.sriniketh.core_design.R.drawable.ic_add),
                contentDescription = stringResource(id = R.string.add_to_shelf_button_text)
            )
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        text = {
            Text(
                text = stringResource(id = R.string.add_to_shelf_button_text),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    )
}

@Composable
private fun BookInfoScreenTitle(uiState: BookInfoUiState) {
    Text(
        text = if (uiState.book != null) {
            uiState.book.info.title
        } else {
            stringResource(id = R.string.nav_label_book_info)
        },
        style = MaterialTheme.typography.headlineMedium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@PreviewLightDark
@Composable
internal fun BookInfoScreenPreview() {
    AppTheme {
        BookInfo(
            uiState = BookInfoUiState(
                book = Book(
                    id = "someId",
                    info = BookInfo(
                        title = "some really really really long title",
                        subtitle = "some subtitle",
                        authors = listOf("author 1, author 2"),
                        thumbnailLink = "https://picsum.photos/200/300",
                        publisher = "some publisher",
                        publishedDate = "23rd January 2021",
                        description = "some description that's repeated again and again so that we have this long piece of text. some description that's repeated again and again so that we have this long piece of text.",
                        pageCount = 215,
                        averageRating = 4.3,
                        ratingsCount = 1227
                    )
                ),
                canAddToShelf = true,
                addBookToShelf = {}
            ),
            goBack = {}
        )
    }
}

@PreviewLightDark
@Composable
internal fun BookInfoScreenLoadingPreview() {
    AppTheme {
        BookInfo(
            uiState = BookInfoUiState(isLoading = true),
            goBack = {}
        )
    }
}

@PreviewLightDark
@Composable
internal fun BookInfoScreenSnackbarPreview() {
    AppTheme {
        BookInfo(
            uiState = BookInfoUiState(snackBarText = R.string.book_info_load_error_message),
            goBack = {}
        )
    }
}
