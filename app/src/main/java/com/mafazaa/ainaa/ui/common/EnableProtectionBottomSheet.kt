package com.mafazaa.ainaa.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.viewmodels.AppViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnableProtectionBottomSheet(
    modifier: Modifier = Modifier,
    title: String = "",
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
    sheetState: SheetState,
    viewModel: AppViewModel
) {
    var sheetContentState : SheetContentState by remember {
        mutableStateOf(SheetContentState.EnableProtection)
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            BottomSheetHeader(
                modifier = modifier,
                title = title,
                sheetContentState = sheetContentState
            )
            Spacer(modifier = modifier.height(8.dp))
            when (sheetContentState) {
                SheetContentState.EnableProtection -> EnableUninstallSheet()
                SheetContentState.ConfirmProtection -> ConfirmProtectionSheet(
                    uninstallCheck = viewModel.uninstallAppCheck
                )
            }
            Spacer(modifier = modifier.height(64.dp))
            SheetNavButtons(
                sheetContentState = sheetContentState,
                onContinue = if (sheetContentState == SheetContentState.EnableProtection) {
                    { sheetContentState = SheetContentState.ConfirmProtection }
                } else onConfirm,
                onBack = if (sheetContentState == SheetContentState.ConfirmProtection) {
                    { sheetContentState = SheetContentState.EnableProtection }
                } else onDismiss,
                primaryText = if(sheetContentState == SheetContentState.ConfirmProtection) "بدأ الحماية"
                else "التالي",
                tertiaryText =  if(sheetContentState == SheetContentState.ConfirmProtection) "السابق"
                else "تراجع"

            )
        }
    }
}

@Composable
private fun BottomSheetHeader(
    modifier: Modifier = Modifier,
    title: String = "",
    sheetContentState: SheetContentState
)
{
    Column {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            SheetOrder(sheetContentState = sheetContentState)

        }

        if (sheetContentState == SheetContentState.EnableProtection) {
            Spacer(modifier = modifier.height(16.dp))
            Text(
                "يمكنك ايضا:",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}



@Composable
private fun EnableUninstallSheet(modifier: Modifier = Modifier) {
    val viewModel : AppViewModel = koinViewModel()
    Column {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                enabled = true,
                checked = viewModel.uninstallAppCheck,
                onCheckedChange = {
                    viewModel.uninstallAppCheck = it
                },
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
private fun ConfirmProtectionSheet(
    modifier: Modifier = Modifier,
    uninstallCheck: Boolean
){
    Column {
        Text(
            if (uninstallCheck) stringResource(id = R.string.uninstall_protection_text)
            else stringResource(id = R.string.confirm_protection_text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = modifier.height(8.dp))
        Text(
            if (uninstallCheck) stringResource(id = R.string.uninstall_protection_description)
            else stringResource(id = R.string.confirm_protection_description),
            style = MaterialTheme.typography.bodySmall
            ,
            color = MaterialTheme.colorScheme.onSurface
        )
    }




}

@Composable
private fun SheetNavButtons(
    modifier: Modifier = Modifier,
    onContinue: () -> Unit = {},
    onBack: () -> Unit = {},
    sheetContentState: SheetContentState = SheetContentState.EnableProtection,
    primaryText: String = "التالي",
    tertiaryText: String = "تراجع"
) {
    var cooldown by remember(sheetContentState) {
        mutableIntStateOf(
            if (sheetContentState == SheetContentState.ConfirmProtection) 5 else 0
        )
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center
    ) {
        val validCounter = sheetContentState == SheetContentState.ConfirmProtection && cooldown > 0

        if (validCounter)
            Text("$cooldown",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.outlineVariant
            )

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                ),
                shape = RoundedCornerShape(4.dp),
                onClick = onBack
            ) {
                Text(tertiaryText)
            }
            Spacer(modifier = modifier.width(16.dp))
            Button(
                enabled = cooldown == 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                shape = RoundedCornerShape(4.dp),
                onClick = onContinue
            ) {
                Text(primaryText)
            }
        }
    }
    LaunchedEffect(sheetContentState) {
        if (sheetContentState == SheetContentState.ConfirmProtection) {
            while (cooldown > 0) {
                delay(1000)
                cooldown--
            }
        }
    }
}

@Composable
fun SheetOrder(
    modifier: Modifier = Modifier,
    sheetContentState: SheetContentState,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val color = MaterialTheme.colorScheme.outlineVariant
        val firstModifier = if (sheetContentState == SheetContentState.EnableProtection) {
            Modifier
                .size(12.dp)
                .background(
                    color, // First step is always active or completed in this context
                    CircleShape
                )
        } else {
            Modifier
                .size(width = 24.dp, height = 12.dp)
                .background(
                    color,
                    CircleShape // Using CircleShape on a non-square Box creates an oval
                )
        }
        val secondModifier = if (sheetContentState == SheetContentState.ConfirmProtection) {
            Modifier
                .size(12.dp)
                .background(
                    color, // First step is always active or completed in this context
                    CircleShape
                )
        } else {
            Modifier
                .size(width = 24.dp, height = 12.dp)
                .background(
                    color,
                    CircleShape // Using CircleShape on a non-square Box creates an oval
                )
        }
        Box(
            modifier = secondModifier
        )
        Box(
            modifier = firstModifier
        )
    }

}

sealed class SheetContentState {
    object EnableProtection : SheetContentState()
    object ConfirmProtection : SheetContentState()
}
