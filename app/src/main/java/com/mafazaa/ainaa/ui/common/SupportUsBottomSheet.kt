package com.mafazaa.ainaa.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mafazaa.ainaa.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportUsBottomSheet(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.support_us_text),
    description: String = stringResource(R.string.support_us_message),
    onDismiss: () -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiary
            )
            Spacer(Modifier.height(16.dp))
            PaymentForm()
            Spacer(Modifier.height(16.dp))
            CustomFilledButton(
                isEnabled = false,
                onCLick = onDismiss,
                text = stringResource(R.string.support_devs),
                icon = painterResource(R.drawable.external_link_icon)
            )
            Spacer(Modifier.height(16.dp))
            AnimatedExpendableBox()
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SupportUsBottomSheetPreview() {
    SupportUsBottomSheet()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentForm(
    modifier: Modifier = Modifier,
    amount: String = "",
    onAmountChange: (String) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedIndex = remember { mutableStateOf<Int?>(0) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            modifier = modifier.fillMaxWidth(0.5f).padding(end = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(stringResource(R.string.donation_amount_placeholder))},
            label = { Text(stringResource(R.string.donation_amount_label)) },
            maxLines = 1,
            singleLine = true,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(8.dp)
        ) {
            TextButton (
                onClick = { expanded = !expanded },
                shape = RectangleShape
            ) {
                Text(text = stringResource(getSelectedPaymentMethod(selectedIndex.value ?: 0)))
                Spacer(Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    modifier = Modifier.scale(scaleX = -1f, scaleY = 1f)
                , contentDescription = "More options")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                paymentMethods.forEach { paymentMethod ->
                    if (paymentMethod == R.string.select_payment_method_text) return@forEach
                    DropdownMenuItem(
                        text = { Text(stringResource(paymentMethod)) },
                        onClick = {
                            selectedIndex.value = paymentMethods.indexOf(paymentMethod)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun  getSelectedPaymentMethod(selectedIndex: Int): Int {
    return when (selectedIndex) {
        1 -> R.string.vodafon_text
        2 -> R.string.visa_text
        else -> R.string.select_payment_method_text
    }
}


val paymentMethods = listOf(
    R.string.select_payment_method_text,
    R.string.vodafon_text,
    R.string.visa_text

)