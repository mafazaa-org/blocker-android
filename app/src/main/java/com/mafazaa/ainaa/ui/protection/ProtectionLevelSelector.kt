package com.mafazaa.ainaa.ui.protection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.domain.models.DnsProtectionLevel
import com.mafazaa.ainaa.ui.theme.red


@Composable
fun ProtectionLevelSelector(
    selectedLevel: DnsProtectionLevel,
    onLevelSelected: (DnsProtectionLevel) -> Unit,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {

        Text(
            text = stringResource(R.string.pick_protection_lvl_text),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Right
        )

        // High Protection Card
        ProtectionCard(
            title = stringResource(R.string.protection_high_lvl_text),
            description = stringResource(R.string.protection_high_lvl_message),
            examples = listOf(
                stringResource(R.string.all_in_low_lvl_label_text),
                stringResource(R.string.music_label_text),
                stringResource(R.string.movies_label_text),
                stringResource(R.string.tiktok_label_text)
            ),
            selected = selectedLevel == DnsProtectionLevel.HIGH,
            onClick = { onLevelSelected(DnsProtectionLevel.HIGH) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Low Protection Card
        ProtectionCard(
            title = stringResource(R.string.protection_low_lvl_text),
            description = stringResource(R.string.protection_low_lvl_message),
            examples = listOf(
                stringResource(R.string.p_rn_label_text),
                stringResource(R.string.gamble_label_text),
                stringResource(R.string.ads_label_text),
                stringResource(R.string.joke_label_text),
                stringResource(R.string.inappropriate_behavior_label_text),
                stringResource(R.string.arabic_nfsw_label_text)
            ),
            selected = selectedLevel == DnsProtectionLevel.LOW,
            onClick = { onLevelSelected(DnsProtectionLevel.LOW) }
        )
    }
}

@Composable
fun ProtectionCard(
    title: String,
    description: String,
    examples: List<String>,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) red else Color.LightGray

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                RadioButton(
                    selected = selected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = red
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }

            Text(
                text = description,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                textAlign = TextAlign.Right
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                examples.forEach { label ->
                    Text(
                        text = label,
                        color = red,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Preview(locale = "ar")
@Composable
fun ProtectionLevelSelectorPreview() {
    var selectedLevel by remember { mutableStateOf(DnsProtectionLevel.HIGH) }
    ProtectionLevelSelector(
        selectedLevel = selectedLevel,
        onLevelSelected = { selectedLevel = it }
    )
}
