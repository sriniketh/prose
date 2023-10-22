package com.sriniketh.feature_viewhighlights.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sriniketh.feature_viewhighlights.R
import com.sriniketh.feature_viewhighlights.ViewHighlightsUIState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ViewHighlightsScreen(
    uiState: ViewHighlightsUIState,
    goBack: () -> Unit,
    addHighlight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(modifier = modifier.fillMaxSize(), topBar = {
        HighlightsAppBar(goBack)
    }, floatingActionButton = {
        AddHighlightFloatingActionButton(addHighlight)
    }, snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)
    }) { innerPadding ->
        when (uiState) {
            is ViewHighlightsUIState.Failure -> {
                val errorMessage = stringResource(id = uiState.errorMessage)
                LaunchedEffect(key1 = null) {
                    launch {
                        snackbarHostState.showSnackbar(errorMessage)
                    }
                }
            }

            is ViewHighlightsUIState.Loading -> {
                LoadingHighlights(modifier, innerPadding)
            }

            is ViewHighlightsUIState.Success -> {
                HighlightsList(modifier, innerPadding, uiState)
            }

            is ViewHighlightsUIState.SuccessNoHighlights -> {
                NoHighlights(modifier, innerPadding)
            }

            else -> {}
        }
    }
}

@Composable
private fun AddHighlightFloatingActionButton(addHighlight: () -> Unit) {
    FloatingActionButton(onClick = addHighlight) {
        Icon(
            Icons.Default.Add, contentDescription = stringResource(id = R.string.add_fab_cont_desc)
        )
    }
}
