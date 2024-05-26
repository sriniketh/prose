package com.sriniketh.feature_addhighlight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sriniketh.core_design.ui.components.NavigationBack
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditAndSaveHighlightScreen(
    uiState: EditAndSaveHighlightUiState,
    updateHighlightText: (String) -> Unit,
    saveHighlight: (String) -> Unit,
    goBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.save_highlight_title_text),
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = { NavigationBack { goBack() } }
            )
        },
    ) { contentPadding ->
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = modifier.fillMaxWidth())
        }

        uiState.snackBarText?.let { resId ->
            val snackbarMessage = stringResource(id = resId)
            LaunchedEffect(key1 = null) {
                launch {
                    snackbarHostState.showSnackbar(snackbarMessage)
                }
            }
        }

        Column(
            modifier = modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                modifier = modifier
                    .weight(0.8f)
                    .fillMaxWidth()
                    .padding(12.dp),
                value = uiState.highlightText,
                onValueChange = { updateHighlightText(it) },
                enabled = !uiState.isLoading,
                textStyle = MaterialTheme.typography.bodyLarge
            )
            Row(
                modifier = modifier
                    .weight(0.2f)
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    modifier = modifier.padding(6.dp),
                    enabled = !uiState.isLoading,
                    onClick = { goBack() }
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel_button_text),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                ElevatedButton(
                    modifier = modifier.padding(6.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(),
                    onClick = { saveHighlight(uiState.highlightText) }) {
                    Text(
                        text = stringResource(id = R.string.save_button_text),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
