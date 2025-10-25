package com.mafazaa.ainaa.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.ui.theme.red


@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    androidVersion: String,
    appVersion: String,
    home: () -> Unit,
) {
    Box(
        modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.android, androidVersion),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp),
            color = red.copy(alpha = .7f),
            style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
            fontSize = 10.sp
        )
        Image(
            painter = painterResource(id = R.drawable.logo_red),
            contentDescription = "logo",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(8.dp)
                .clickable { home() }
                .fillMaxWidth(.29f),//todo
        )
        Text(
            text = appVersion,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp),
            color = red.copy(alpha = .7f),
            style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
            fontSize = 10.sp
        )
    }


}

@Preview(showSystemUi = true)
@Composable
fun BottomBarPreview(modifier: Modifier = Modifier) {
    Box(Modifier.fillMaxSize()) {
        BottomBar(
            modifier.align(Alignment.BottomCenter),
            stringResource(R.string.android_version),
            stringResource(R.string.app_version_number)
        ) {}
    }
}