package com.mafazaa.ainaa.ui.dialog

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.data.models.ReportModel
import com.mafazaa.ainaa.ui.common.LabelledTextField
import com.mafazaa.ainaa.ui.theme.lightGray


val nameValidRegex = Regex("[\\w\\s]{3,}")
val phoneValidRegex = Regex("\\d{10,14}")
val emailValidRegex = Regex(Patterns.EMAIL_ADDRESS.pattern())


@Composable
fun ReportProblemDialog(
    onClose: () -> Unit,
    onSubmit: (ReportModel) -> Unit,
) {

    val nameState = remember { mutableStateOf("") }
    val phoneState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val problemState = remember { mutableStateOf("") }

    val nameErrorState = remember { mutableStateOf(false) }
    val phoneErrorState = remember { mutableStateOf(false) }
    val emailErrorState = remember { mutableStateOf(false) }
    val problemErrorState = remember { mutableStateOf(false) }


    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with close button and title
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close_text)
                        )
                    }
                    Text(
                        text = stringResource(R.string.report_problem_text),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,

                        modifier = Modifier.align(Alignment.Center)
                    )
                }


                // Name Field
                LabelledTextField(
                    labelResId = R.string.name_field_label,
                    placeholderResId = R.string.name_field_placeholder,
                    valueState = nameState,
                    errorState = nameErrorState,
                )

                // Phone Field
                LabelledTextField(
                    labelResId = R.string.phone_number_field_label,
                    placeholderResId = R.string.phone_number_field_placeholder,
                    valueState = phoneState,
                    errorState = phoneErrorState,
                    keyboardType = KeyboardType.Phone,
                )

                // Email Field
                LabelledTextField(
                    labelResId = R.string.email_field_label,
                    placeholderResId = R.string.email_field_placeholder,
                    valueState = emailState,
                    errorState = emailErrorState,
                    keyboardType = KeyboardType.Email,
                )

                // Problem Field
                LabelledTextField(
                    labelResId = R.string.issue_field_label,
                    placeholderResId = R.string.issue_field_placeholder,
                    valueState = problemState,
                    errorState = problemErrorState,
                    modifier = Modifier.height(100.dp),
                    keyboardOptions = KeyboardOptions.Default,
                    maxLines = 5,
                )


                // Local functions to validate fields and update error states
                val validate = { valueState: MutableState<String>,
                                 regex: Regex,
                                 errorState: MutableState<Boolean> ->
                    errorState.value = valueState.value.trim().matches(regex).not()
                }

                // Function to check for validation errors in user input
                val isValidationError: () -> Boolean = {
                    validate(nameState, nameValidRegex, nameErrorState)
                    validate(phoneState, phoneValidRegex, phoneErrorState)
                    validate(emailState, emailValidRegex, emailErrorState)
                    problemErrorState.value = problemState.value.trim().length < 10

                    booleanArrayOf(
                        nameErrorState.value,
                        phoneErrorState.value,
                        emailErrorState.value,
                        problemErrorState.value
                    )
                        .any { it }
                }


                // Submit Button
                Button(
                    onClick = {
                        if (isValidationError().not())
                            onSubmit(
                                ReportModel(
                                    name = nameState.value,
                                    phone = phoneState.value,
                                    email = emailState.value,
                                    problem = problemState.value,
                                )
                            )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)

                ) {
                    Text(stringResource(R.string.send_text), color = Color.White)
                }

                // Footer Message
                Text(
                    text = stringResource(R.string.email_phone_contact_message),
                    fontSize = 12.sp,
                    color = lightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
        }
    }
}

@Preview(locale = "ar")
@Composable
fun ReportProblemDialogPreview() {
    ReportProblemDialog(
        onClose = {},
        onSubmit = { _ -> }
    )
}
