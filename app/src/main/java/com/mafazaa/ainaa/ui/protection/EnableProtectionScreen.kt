package com.mafazaa.ainaa.ui.protection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.domain.models.DnsProtectionLevel
import com.mafazaa.ainaa.ui.common.CustomFilledButton
import com.mafazaa.ainaa.ui.common.SupportUsButton

@Composable
fun EnableProtectionScreen(
    modifier: Modifier = Modifier,
    report: () -> Unit,
    enableProtection: (DnsProtectionLevel) -> Unit,
    selectedLevel: DnsProtectionLevel,
    onSelectedLevelChange: (DnsProtectionLevel) -> Unit = {},
    supportUs: () -> Unit = {},
    onEnableProtection: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp) // optional spacing between the two
    ) {

        ProtectionLevelSelector(
            selectedLevel = selectedLevel,
            onLevelSelected = onSelectedLevelChange

        )
        ProtectYourDevice(
            enableProtection = { enableProtection(selectedLevel) },
            report = onEnableProtection,
            isLevelSelected = selectedLevel != DnsProtectionLevel.NONE
        )
        Spacer(modifier = Modifier.weight(1f))
        SupportUsButton(
            supportUs = supportUs
        )
    }

}
@Composable
fun ProtectYourDevice(
    enableProtection:  () -> Unit,
    report: () -> Unit = {},
    isLevelSelected: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CustomFilledButton(
            isEnabled = isLevelSelected,
            onCLick = enableProtection,
            text = stringResource(R.string.enable_protection_label_text)
        )
    }
}


// Preview for Design Time
@Preview(showBackground = true, locale = "ar")
@Composable
fun PreviewEnableProtectionScreen() {
    EnableProtectionScreen(Modifier, {}, { _-> }, DnsProtectionLevel.HIGH)
}
