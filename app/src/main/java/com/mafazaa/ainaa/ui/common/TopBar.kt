package com.mafazaa.ainaa.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.navigation.Screen
import com.mafazaa.ainaa.ui.theme.red

@Composable
fun TopBar(
    supportUs: () -> Unit,
    home: () -> Unit,
    currentScreen: Screen? = null,
    onBack: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 24.dp, end = 4.dp),
    ) {

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(y = (-2).dp)
        ) {}


        Image(
            painter = painterResource(id = R.drawable.red), // Replace with actual drawable
            contentDescription = "Logo",
            modifier = Modifier
                .fillMaxWidth(.24f)
                .align(Alignment.Center)
                .clickable { home() }
        )
        Text(
            text = if (currentScreen == Screen.Support)
                stringResource(R.string.back_text)
            else stringResource(R.string.support_us),
            color = red,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable {
                    if (currentScreen == Screen.Support)
                        onBack?.invoke()
                    else supportUs()
                },
            style = LocalTextStyle.current.copy(textDecoration = TextDecoration.Underline),
            lineHeight = 20.sp
        )

    }
}

@Preview
@Composable
fun TopBarPreview() {
    TopBar(supportUs = {}, home = {})
}

