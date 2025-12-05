package com.mafazaa.ainaa.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mafazaa.ainaa.R


val navigationItem = mapOf(
    R.string.support_us_text to R.drawable.support_icon,
    R.string.customer_service_text to R.drawable.customer_supp_icon,
    R.string.join_us_label to R.drawable.join_icon,
)

val socialMedia = mapOf(
    R.string.facebook_text to R.drawable.facebook_icon,
    R.string.whatsapp_text to R.drawable.whatsapp_icon,
    R.string.youtube_text to R.drawable.youtube_icon
)
@Composable
fun MainDrawer(
    modifier: Modifier = Modifier,
    drawerState : DrawerState,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerContent = {DrawerContent()},
        drawerState = drawerState,
        gesturesEnabled = true,
        modifier = modifier,
        content = {
            content()
        }
    )
}


@Composable
fun DrawerContent(modifier: Modifier = Modifier) {
    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.8f),
        drawerContainerColor = MaterialTheme.colorScheme.background,
        drawerContentColor = MaterialTheme.colorScheme.onBackground,
        drawerShape = MaterialTheme.shapes.large,
        content = {
            DrawerHeader(modifier = Modifier.padding(horizontal = 8.dp))
            for (item in navigationItem) {
                DrawerItem(
                    label = item.key,
                    icon = item.value,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            for (item in socialMedia) {
                SocialMediaItem(
                    label = item.key,
                    icon = item.value,
                )
            }
        }
    )


}

@Composable
fun DrawerHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        AsyncImage(
            model = R.drawable.logo_red,
            contentDescription = "Logo",
            modifier = modifier.size(81.dp)

        )
        Spacer(modifier = Modifier.weight(1f))
        CustomOutlinedButton(
            onCLick = {},
            text = stringResource(R.string.who_are_we_text)
        )

    }
}

@Composable
fun DrawerItem(
    label: Int,
    icon: Int,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    NavigationDrawerItem(
        icon = {Icon(
            painter = painterResource( icon),
            contentDescription = "item icon"
        )},
        label = { Text(text = stringResource(label)) },
        selected = selected,
        onClick = onClick
    )
}

@Composable
fun SocialMediaItem(
    badge: Int = R.drawable.external_link_icon,
    label: Int = R.string.facebook_text,
    icon: Int = R.drawable.facebook_icon,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavigationDrawerItem(
        badge = { Icon(
            painter = painterResource( badge),
            contentDescription = "item icon",
            modifier = Modifier.size(16.dp)
        )},
        icon = {Icon(
            painter = painterResource( icon),
            contentDescription = "item icon"
        )},
        label = { Text(text = stringResource(label)) },
        selected = selected,
        onClick = onClick
    )

}

