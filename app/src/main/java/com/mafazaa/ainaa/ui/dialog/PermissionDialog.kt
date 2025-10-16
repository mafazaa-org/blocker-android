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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mafazaa.ainaa.domain.models.PermissionState
import com.mafazaa.ainaa.ui.theme.red

@Composable
fun PermissionDialog(
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    permissionState: PermissionState,
) {
    val (permissionTitle, permissionDescription) = when (permissionState) {
        PermissionState.Notification -> "إذن الإشعارات" to "يحتاج هذا التطبيق إلى إذن الإشعارات."
        PermissionState.UsageStats -> "إذن إحصائيات الاستخدام" to "يحتاج هذا التطبيق إلى إذن إحصائيات الاستخدام لمراقبة التطبيقات التي تعمل."
        PermissionState.Overlay -> "إذن العرض فوق التطبيقات" to "يحتاج هذا التطبيق إلى إذن العرض فوق التطبيقات للظهور فوق التطبيقات الأخرى."
        PermissionState.Vpn -> "إذن VPN" to "يحتاج هذا التطبيق إلى إذن VPN لإنشاء اتصال آمن."
        PermissionState.Accessibility -> "إذن إمكانية الوصول" to "يحتاج هذا التطبيق إلى إذن إمكانية الوصول لتوفير ميزات إضافية."
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
                        Text(
                            text = "إلغاء",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = red
                        )
                    ) {
                        Text("موافق",
                            color = Color.White)
                    }
                }
            }
        }
    }
}

