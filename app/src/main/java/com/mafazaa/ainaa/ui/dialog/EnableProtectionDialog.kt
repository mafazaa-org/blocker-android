package com.mafazaa.ainaa.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.ui.theme.AinaaTheme
import com.mafazaa.ainaa.ui.theme.red
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalConfiguration

/**
 * last dialog before enabling protection
 */
@Composable
fun EnableProtectionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)

            ) {
                // Make the textual content scrollable and pin the action row to the bottom
                val scrollState = rememberScrollState()

                // Timer state for enabling the OK button after 5 seconds
                // Restore 3-second delay before accept as requested
                var timerSeconds by remember { mutableStateOf(3) }
                var timerActive by remember { mutableStateOf(true) }
                val isOkEnabled = (scrollState.value == scrollState.maxValue) && timerSeconds == 0

                // Start countdown timer when dialog is shown
                if (timerActive && timerSeconds > 0) {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        while (timerSeconds > 0) {
                            kotlinx.coroutines.delay(1000)
                            timerSeconds--
                        }
                        timerActive = false
                    }
                }

                // compute a sensible max height based on screen height so scrolling can kick in on small screens
                val configuration = LocalConfiguration.current
                val maxDialogHeight = (configuration.screenHeightDp.dp * 0.8f).coerceAtMost(560.dp)

                Column(
                    modifier = Modifier
                        .heightIn(max = maxDialogHeight)
                        .verticalScroll(scrollState)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = stringResource(R.string.disclaimer_text),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_close_24),
                                contentDescription = "close",
                            )
                        }
                    }


                    Text(
                        text = stringResource(R.string.disclaimer_message),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.uninstall_feature_text),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.unistall_feature_message),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.classic_unistall_text),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.classic_unistall_message),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = stringResource(R.string.classic_unistall_message2),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.enable_protection_confirm_text),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = red,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                // Action row pinned below the scrollable content so it's always visible
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = red)
                    ) {
                        Text(stringResource(R.string.later_text))
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = isOkEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = red),

                        ) {
                        // Show countdown on the Confirm button while timer is active
                        val confirmLabel = if (timerSeconds > 0) {
                            "${stringResource(R.string.enable_protection_text1)} (${timerSeconds})"
                        } else {
                            stringResource(R.string.enable_protection_text1)
                        }
                        Text(confirmLabel, color = Color.White)
                    }
                }

            }
        }
    }
}


@Preview(showBackground = false, showSystemUi = true)
@Composable
fun EnableProtectionDialogPreview() {
    AinaaTheme {
        EnableProtectionDialog(onDismiss = {}, onConfirm = {})
    }
}