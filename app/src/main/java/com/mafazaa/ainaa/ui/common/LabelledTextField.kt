package com.mafazaa.ainaa.ui.common

import androidx.annotation.IntegerRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import com.mafazaa.ainaa.ui.theme.lightGray


@Composable
fun LabelledTextField(
    @IntegerRes labelResId: Int,
    @IntegerRes placeholderResId: Int,
    valueState: MutableState<String>,
    errorState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
    maxLines: Int = 1,
    onValueChange: ((String) -> Unit)? = null
) {

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = valueState.value,
        onValueChange = {
            valueState.value = it
            errorState.value = false

            onValueChange?.invoke(it)
        },
        label = {
            Text(
                text = stringResource(labelResId),
                fontSize = 14.sp,
            )
        },
        placeholder = {
            Text(
                text = stringResource(placeholderResId),
                color = lightGray,
                fontSize = 12.sp,
            )
        },
        keyboardOptions = keyboardOptions.copy(keyboardType = keyboardType),
        maxLines = maxLines,
        singleLine = maxLines == 1,
        textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Content),
        isError = errorState.value,
    )

}
