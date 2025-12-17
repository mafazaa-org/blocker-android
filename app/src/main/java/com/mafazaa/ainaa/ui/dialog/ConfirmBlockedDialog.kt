package com.mafazaa.ainaa.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.domain.models.AppInfo
import com.mafazaa.ainaa.ui.theme.red

/**
 * A dialog to confirm blocking an app.
 */
@Composable
fun ConfirmBlockedDialog(
    app: AppInfo,
    onDismiss: () -> Unit,
    onConfirm: (AppInfo) -> Unit,
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
                modifier = Modifier.padding(20.dp)
            ) {
                // Close Icon
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_close_24),
                        contentDescription = stringResource(R.string.close_text)
                        )
                }

                // Title
                Text(
                    text = stringResource(R.string.block_alert_message, app.name),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 16.dp)
                )

                // Description
                Text(
                    text = "",//todo
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 24.dp)
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = red)
                    ) {
                        Text(stringResource(R.string.cancel_text))
                    }

                    Button(
                        onClick = { onConfirm(app) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = red,
                            contentColor = Color.White
                        )
                    ) {
                        Text(stringResource(R.string.confirm_text))
                    }
                }
            }
        }
    }
}
