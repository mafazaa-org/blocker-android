package com.mafazaa.ainaa.ui.protection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.domain.models.UpdateState
import com.mafazaa.ainaa.ui.common.ReportLink
import com.mafazaa.ainaa.ui.common.TwoColorText
import com.mafazaa.ainaa.ui.theme.red

@Composable
fun ProtectionActivatedScreen(
    onSupportClick: () -> Unit,
    onBlockAppClick: () -> Unit,
    onManageKeywordsClick: () -> Unit,
    onReportClick: () -> Unit,
    onConfirmProtectionClick: () -> Unit,
    onUpdateClick: (updateState: UpdateState) -> Unit = { /* Default no-op */ },
    updateState: UpdateState = UpdateState.NoUpdate,

    ) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = stringResource(R.string.protection_activated_title),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

// Subtitle
        Text(
            text = stringResource(R.string.how_check_activation_work_text),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = red,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 24.dp)
                .clickable {
                    onConfirmProtectionClick()
                }
        )

        // Buttons row
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            OutlinedButton(
                onClick = onSupportClick,
                border = BorderStroke(1.dp, red),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = red),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(text = stringResource(R.string.support_us))
            }

            Button(
                onClick = onBlockAppClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = red,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(text = stringResource(R.string.block_app_text))
            }
        }
        
        // Keyword management button
        Button(
            onClick = onManageKeywordsClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = red,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .height(48.dp)
        ) {
            Text(text = stringResource(R.string.manage_keywords_text))
        }
        
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

