package com.mafazaa.ainaa.ui.common


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mafazaa.ainaa.R

@Composable
fun AnimatedExpendableBox(modifier: Modifier = Modifier) {
    val expend = remember { mutableStateOf(false) }
    val expendIcon = if (!expend.value) Icons.AutoMirrored.Filled.KeyboardArrowRight
        else Icons.Filled.KeyboardArrowDown

    Column(modifier = modifier.clickable(
        onClick = { expend.value = !expend.value }
    )) {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineContent = { Text(stringResource(R.string.support_us_text)) },
            trailingContent = {Icon(imageVector = expendIcon, contentDescription = "expend Icon")}
        )
        AnimatedVisibility(
            visible = expend.value,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(stringResource(R.string.support_us_message))
        }
    }

}
