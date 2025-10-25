package com.mafazaa.ainaa.ui.support

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mafazaa.ainaa.BuildConfig
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.ui.common.TwoColorText
import com.mafazaa.ainaa.ui.theme.red

@Composable
fun SupportScreen(
    modifier: Modifier = Modifier,
    onSupportClick: () -> Unit = {},
    onJoinClick: () -> Unit = {},
    onShareLogFile: () -> Unit = {},
    onStopBlocking: () -> Unit = {},
    onOpenScreenShotWindow: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = stringResource(R.string.support_us_label_text),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.donation_label_text),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.donation_label_message),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = onSupportClick,
                    colors = ButtonDefaults.buttonColors(containerColor = red)
                ) {
                    Text(
                        text= stringResource(R.string.support_us_text),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }


        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.join_us_text),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.join_us_message),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = onJoinClick,
                    colors = ButtonDefaults.buttonColors(containerColor = red)
                ) {
                    Text(
                        text= stringResource(R.string.join_text),
                        color = MaterialTheme.colorScheme.onBackground

                    )
                }
            }
        }
        TwoColorText(black = stringResource(R.string.share_register_folder_text), red = stringResource(R.string.click_here_text)) {
            onShareLogFile()
        }
        TwoColorText(black = stringResource(R.string.share_screen_not_blocked_text), red = stringResource(R.string.click_here_text)) {
            onOpenScreenShotWindow()

        }
        if (BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(16.dp))
            TwoColorText(black = stringResource(R.string.stop_blocktemp_text), red = stringResource(R.string.click_here_text)) {
                onStopBlocking()
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun SupportScreenPreview() {
    SupportScreen()
}
