package com.mafazaa.ainaa.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.domain.models.AppInfo
import com.mafazaa.ainaa.helpers.toPainter
import com.mafazaa.ainaa.ui.theme.red


@Composable
fun BlockAppDialog(
    onDismiss: () -> Unit,
    appStates: List<AppInfo>,
    onBlockClick: (AppInfo) -> Unit,
) {

    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(.9f)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Close icon and title row
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
                        text = stringResource(R.string.block_app_text),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                )

                Text(
                    text = stringResource(R.string.block_app_message),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    LazyColumn {
                        items(appStates) { app ->
                            AppBlockItem(app) { onBlockClick(app) }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = DividerDefaults.Thickness,
                                color = DividerDefaults.color
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                }
            }
        }
    }

}

@Composable
fun AppBlockItem(
    app: AppInfo,
    onBlockClick: (AppInfo) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onBlockClick(app) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (app.isSelected) Color.Gray else red,
                contentColor = MaterialTheme.colorScheme.surface
            ),
            enabled = !app.isSelected,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .height(40.dp)
        ) {
            Text(
                text = if (app.isSelected)
                    stringResource(R.string.blocked_text)
                else
                    stringResource(R.string.block_text)
                ,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = app.icon?.toPainter() ?: painterResource(id = R.drawable.android),
                contentDescription = app.name,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = app.name,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
fun PreviewBlockAppDialog() {
    val sampleApps = listOf(
        AppInfo(
            stringResource(
                R.string.whatsapp_text
            ),
            null,
            stringResource(R.string.whatsapp_package_text),
            false
        ),
        AppInfo(
            stringResource(R.string.facebook_text),
            null,
            stringResource(R.string.facebook_package_text),
            true
        ),
        AppInfo(
            stringResource(R.string.instagram_text),
            null,
            stringResource(R.string.instagram_package_text),
            false
        )
    )
    BlockAppDialog(
        onDismiss = {},
        appStates = sampleApps,
        onBlockClick = {}
    )
}