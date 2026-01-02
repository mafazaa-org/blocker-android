package com.mafazaa.ainaa.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mafazaa.ainaa.R

@Composable
fun CustomOutlinedButton(
    modifier: Modifier = Modifier,
    onCLick: () -> Unit,
    text : String = stringResource(R.string.block_apps_text)
) {

    OutlinedButton(
        onClick = onCLick,
        modifier = modifier,
        shape = RoundedCornerShape(4),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun CustomFilledButton(
    isEnabled : Boolean,
    modifier: Modifier = Modifier,
    onCLick: () -> Unit,
    text : String,
    icon : Painter? = null,
    isInteractable : Boolean = true
){
    val primaryColor = MaterialTheme.colorScheme.primary
    val disabledColor = MaterialTheme.colorScheme.inverseSurface
    Button(
        enabled = isInteractable,
        onClick = onCLick,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(46.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) primaryColor else disabledColor,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimary

        )
        if (icon != null)
        Icon(
            modifier = Modifier.padding(start = 4.dp)
                .scale(scaleX = -1f, scaleY = 1f),
            painter =  icon,
            contentDescription = "Button Icon")

    }
}