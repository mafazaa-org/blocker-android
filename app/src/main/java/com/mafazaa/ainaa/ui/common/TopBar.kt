package com.mafazaa.ainaa.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.navigation.Screen

@Composable
fun TopBar(
    supportUs: () -> Unit,
    openMenu: () -> Unit,
    currentScreen: Screen? = null,
    onBack: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()

    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .align(Alignment.TopStart)
            .padding(horizontal = 8.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { openMenu() },
                modifier = Modifier,
                content = {
                    Icon(
                        painter = painterResource(R.drawable.round_menu_24),
                        contentDescription = "Menu",
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
            Image(
                painter = painterResource(id = R.drawable.red), // Replace with actual drawable
                contentDescription = "Logo",
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxWidth(.24f)
            )
            Spacer(modifier = Modifier.weight(1f))
            CustomOutlinedButton(
                onCLick = supportUs,
                text = stringResource(R.string.block_apps_text)
            )
        }
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(1.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp)
                )
        )
    }
}

@Preview
@Composable
fun TopBarPreview() {
    TopBar(supportUs = {}, openMenu = {})
}

