package com.mafazaa.ainaa.ui.protection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.domain.models.DnsProtectionLevel
import com.mafazaa.ainaa.ui.common.ReportLink
import com.mafazaa.ainaa.ui.theme.red

@Composable
fun EnableProtectionScreen(
    modifier: Modifier = Modifier,
    report: () -> Unit,
    enableProtection: (DnsProtectionLevel) -> Unit,
    selectedLevel: DnsProtectionLevel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp) // optional spacing between the two
    ) {
        var selectedLevel by remember { mutableStateOf(selectedLevel) }
        ProtectionLevelSelector(selectedLevel, {
            selectedLevel = it
        })
        ProtectYourDevice({ enableProtection(selectedLevel) }, report)
    }

}

@Composable
fun ProtectYourDevice(enableProtection: () -> Unit, report: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Button(
            onClick = { enableProtection() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = Color.LightGray,
                containerColor = red
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(id = R.drawable.ic_white), // Replace with your lock icon
                    contentDescription = "Lock Icon",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.enable_protection_label_text),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )

            }

        }

        // Link at the Bottom
        ReportLink(
            onReportClick = report,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}


// Preview for Design Time
@Preview(showBackground = true, locale = "ar")
@Composable
fun PreviewEnableProtectionScreen() {
    EnableProtectionScreen(Modifier, {}, { _-> }, DnsProtectionLevel.HIGH)
}