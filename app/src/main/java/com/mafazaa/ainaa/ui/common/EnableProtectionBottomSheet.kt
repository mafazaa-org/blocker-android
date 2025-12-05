package com.mafazaa.ainaa.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.mafazaa.ainaa.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnableProtectionBottomSheet(
    modifier: Modifier = Modifier,
    title: String = "",
    onDismiss: () -> Unit = {},

    onConfirm: () -> Unit = {},
    sheetState: SheetState
) {
    var sheetContentState : SheetContentState by remember {
        mutableStateOf(SheetContentState.EnableProtection)
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = modifier.fillMaxWidth()
                .padding(16.dp)
        ) {
            BottomSheetHeader()
            Spacer(modifier = modifier.height(8.dp))
            when (sheetContentState) {
                SheetContentState.EnableProtection -> EnableUninstallSheet()
                SheetContentState.ConfirmProtection -> ConfirmProtectionSheet()
            }
            Spacer(modifier = modifier.height(16.dp))
            SheetNavButtons(
                onContinue = if (sheetContentState == SheetContentState.EnableProtection) {
                    { sheetContentState = SheetContentState.ConfirmProtection }
                } else onConfirm,
                onBack = if (sheetContentState == SheetContentState.ConfirmProtection) {
                    { sheetContentState = SheetContentState.EnableProtection }
                } else onDismiss,
                primaryText = if(sheetContentState == SheetContentState.ConfirmProtection) "start"
                else "next",
                tertiaryText =  if(sheetContentState == SheetContentState.ConfirmProtection) "back"
                else "dismiss"

            )
        }
    }
}

@Composable
private fun BottomSheetHeader(modifier: Modifier = Modifier) {
    Column {
        Row() {
            Text("تفعيل الحماية العالية",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))

        }
        Spacer(modifier = modifier.height(16.dp))
        Text("يمكنك ايضا:",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}



@Composable
private fun EnableUninstallSheet(modifier: Modifier = Modifier) {
    Column {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                enabled = true,
                checked = false,
                onCheckedChange = {},
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.primary
                )
            )
            Text("عدم حذف التطبيق",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    Text("هذه الخاصية  مصممة لضمان استمراريه الحماية و منع اي مستخدم من تعطيلها بسهولة.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun ConfirmProtectionSheet(){
    Text("Confirtrm protection")

}

@Composable
private fun SheetNavButtons(
    modifier: Modifier = Modifier,
    onContinue: () -> Unit = {},
    onBack: () -> Unit = {},
    primaryText: String = "التالي",
    tertiaryText: String = "تراجع"
) {
    Row(modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
            ),
            shape = RoundedCornerShape(4.dp),
            onClick =  onBack
        ) {
            Text(tertiaryText)
        }
        Spacer(modifier = modifier.width(16.dp))
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            shape = RoundedCornerShape(4.dp),
            onClick =  onContinue
        ) {
            Text(primaryText)
        }
    }

}

private sealed class SheetContentState {
    object EnableProtection : SheetContentState()
    object ConfirmProtection : SheetContentState()
}

