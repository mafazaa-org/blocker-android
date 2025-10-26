package com.mafazaa.ainaa.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.ui.theme.red

@Composable
fun ManageKeywordsDialog(
    keywords: Set<String>,
    onDismiss: () -> Unit,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit
) {
    var newKeyword by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(.9f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close_text)
                        )
                    }
                    Text(
                        text = stringResource(R.string.manage_keywords_text),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Input field for adding new keyword
                OutlinedTextField(
                    value = newKeyword,
                    onValueChange = { newKeyword = it },
                    label = { Text(stringResource(R.string.add_keyword_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        Button(
                            onClick = {
                                if (newKeyword.trim().isNotEmpty()) {
                                    onAddKeyword(newKeyword.trim())
                                    newKeyword = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = red),
                            enabled = newKeyword.trim().isNotEmpty()
                        ) {
                            Text(stringResource(R.string.add_keyword_text))
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // List of keywords
                if (keywords.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_keywords_text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 300.dp)
                    ) {
                        items(keywords.toList()) { keyword ->
                            KeywordItem(
                                keyword = keyword,
                                onRemove = { showDeleteConfirmation = keyword }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteConfirmation?.let { keyword ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = { Text(stringResource(R.string.confirm_delete_keyword)) },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveKeyword(keyword)
                        showDeleteConfirmation = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = red)
                ) {
                    Text(stringResource(R.string.delete_keyword_text))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = null }) {
                    Text(stringResource(R.string.cancel_text))
                }
            }
        )
    }
}

@Composable
fun KeywordItem(
    keyword: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = keyword,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_keyword_text),
                    tint = red
                )
            }
        }
    }
}
