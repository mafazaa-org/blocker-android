package com.mafazaa.ainaa.ui.dialog


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.domain.models.PermissionState

@Composable
fun PermissionDialog(
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    permissionState: PermissionState,
) {
    val (permissionTitle, permissionDescription) = when (permissionState) {
        PermissionState.Notification ->
            stringResource(R.string.notification_permission_text) to stringResource(
            R.string.notification_permission_message
        )
        PermissionState.UsageStats ->
            stringResource(R.string.usage_stats_permission_text) to stringResource(
            R.string.usage_stats_permission_message
        )
        PermissionState.Overlay -> stringResource(R.string.overlay_permission_text) to stringResource(
            R.string.overlay_permission_message
        )
        PermissionState.Vpn -> stringResource(R.string.vpn_permission_text) to stringResource(R.string.vpn_permission_message)
        PermissionState.Accessibility -> stringResource(R.string.accessibility_permission_text) to stringResource(
            R.string.accessibility_permission_message
        )
        PermissionState.Granted -> "" to ""
    }

    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(.9f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = permissionTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = permissionDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel_text))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onClick) {
                        Text(stringResource(R.string.ok_text))
                    }
                }
            }
        }
    }
}

