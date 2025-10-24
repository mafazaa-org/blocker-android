package com.mafazaa.ainaa.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mafazaa.ainaa.R

@Composable
fun ScreenshotOverlay(
    modifier: Modifier = Modifier, onScreenShot: (Long) -> Unit = {}, onClose: () -> Unit = {}
) {
    Row(
        modifier
            .padding(8.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .7f),
                shape = MaterialTheme.shapes.large
            )
    ) {

        var isHeld by remember { mutableStateOf(false) }
        Icon(
            modifier = Modifier
                .padding(4.dp),
            painter = painterResource(id = R.drawable.drag_pan),
            contentDescription = stringResource(R.string.close_text),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        Icon(
            modifier = Modifier
                .padding(4.dp)
                .background(
                    if (isHeld) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.large
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            val startTime = System.currentTimeMillis()
                            isHeld = true
                            tryAwaitRelease()
                            isHeld = false
                            onScreenShot((System.currentTimeMillis() - startTime))
                        })
                },
            painter = painterResource(id = R.drawable.screenshot),
            contentDescription = stringResource(R.string.screenshot_text),
            tint = if (isHeld) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer
        )


        Icon(
            modifier = Modifier
                .padding(4.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small
                )
                .clickable { 
                    onClose()
                },
            painter = painterResource(id = R.drawable.baseline_close_24),
            contentDescription = stringResource(R.string.close_text),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
    }
}
