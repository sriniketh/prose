package com.sriniketh.core_design.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sriniketh.core_design.ui.theme.AppTheme

@Preview
@Composable
internal fun Typography() {
    AppTheme {
        Surface {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Display Large",
                    style = MaterialTheme.typography.displayLarge
                )

                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Display Medium",
                    style = MaterialTheme.typography.displayMedium
                )

                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Headline Large",
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Headline Medium",
                    style = MaterialTheme.typography.headlineMedium
                )


                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Title Large",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Title Medium",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Body Large",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Body Medium",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Label Large",
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Label Medium",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
