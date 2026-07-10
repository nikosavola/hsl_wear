package com.hsl.wear.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.material3.*
import com.hsl.wear.R

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier
) {
    val listState = rememberTransformingLazyColumnState()
    val transformSpec = rememberTransformationSpec()

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(
            modifier = modifier.fillMaxSize(),
            state = listState,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this@item, transformSpec),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.information),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                InfoCard(
                    title = stringResource(R.string.disclaimer),
                    body = stringResource(R.string.disclaimer_text),
                    modifier = Modifier.transformedHeight(this@item, transformSpec)
                )
            }

            item {
                InfoCard(
                    title = stringResource(R.string.about),
                    body = stringResource(R.string.about_text),
                    modifier = Modifier.transformedHeight(this@item, transformSpec)
                )
            }

            item {
                InfoCard(
                    title = stringResource(R.string.no_warranty),
                    body = stringResource(R.string.no_warranty_text),
                    modifier = Modifier.transformedHeight(this@item, transformSpec)
                )
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String, modifier: Modifier = Modifier) {
    Card(
        onClick = { /* Non-interactive card */ },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start
            )
        }
    }
}
