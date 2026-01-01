package com.mafazaa.ainaa.ui.protection

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.domain.models.UpdateState
import com.mafazaa.ainaa.ui.common.ReportLink
import com.mafazaa.ainaa.ui.common.TwoColorText

@Composable
fun ProtectionActivatedScreen(
    onSupportClick: () -> Unit = {},
    onBlockAppClick: () -> Unit = {},
    onReportClick: () -> Unit = {},
    onBlockWordClicked: () -> Unit = {},
    onConfirmProtectionClick: () -> Unit = {},
    onUpdateClick: (updateState: UpdateState) -> Unit = { /* Default no-op */ },
    updateState: UpdateState = UpdateState.NoUpdate,
    ) {
    val image = R.drawable.browser_blocked_page
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        // Heading
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(painter = painterResource(R.drawable.ic_auto_protect), contentDescription = "")
            Text(
                text = stringResource(R.string.protection_activated_title),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }

        // Subtitle
        Text(
            text = stringResource(R.string.test_protection_text),
            style = MaterialTheme.typography.bodyMedium,
        )

        // Buttons row
        Image(
            painter = rememberAsyncImagePainter(model = image),
            contentDescription = stringResource(R.string.safe_search_image),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .padding(vertical = 8.dp)
                .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
        )
        ReportLink(onReportClick = onReportClick)
        Spacer(modifier = Modifier.height(16.dp))
        val (black, red) = when (updateState) {
            UpdateState.NoUpdate -> Pair(
                stringResource(R.string.no_update_found_text),
                stringResource(R.string.click_to_check_text)
            )

            UpdateState.Checking -> Pair(
                stringResource(R.string.check_update_text),
                stringResource(R.string.empty_string)
            )

            is UpdateState.Downloading -> Pair(
                stringResource(R.string.downloading_update_text),
                stringResource(R.string.empty_string)
            )

            is UpdateState.Failed -> Pair(
                stringResource(R.string.update_failed_text),
                stringResource(R.string.try_again)
            )

            UpdateState.Downloaded -> Pair(
                stringResource(R.string.update_done),
                stringResource(R.string.confirmation_text)
            )
        }

        TwoColorText(black = black, red = red, onClick = { onUpdateClick(updateState) })

    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun ProtectionActivatedScreenPreview() {
    ProtectionActivatedScreen()
}
