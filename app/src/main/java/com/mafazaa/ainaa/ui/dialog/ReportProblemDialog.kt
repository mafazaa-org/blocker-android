package com.mafazaa.ainaa.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.data.models.ReportModel
import com.mafazaa.ainaa.ui.theme.lightGray

@Composable
fun ReportProblemDialog(
    onClose: () -> Unit,
    onSubmit: (ReportModel) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf("") }

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
                    .padding(20.dp)
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
                            contentDescription = "Close"
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
                Text(
                    text=stringResource(R.string.name_field_title),
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { it: String -> name = it },
                    placeholder = {
                        Text(
                            stringResource(R.string.enter_name_placeholder),
                            color = lightGray,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                )

                // Phone Field
                Spacer(Modifier.height(12.dp))
                Text("رقم الهاتف", fontSize = 14.sp)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { it: String -> phone = it },
                    placeholder = { Text(
                        text=stringResource(R.string.phone_number_text),
                        color = lightGray,
                        fontSize = 12.sp
                    ) },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                )

                // Email Field
                Spacer(Modifier.height(12.dp))
                Text(
                    text=stringResource(id= R.string.email_title),
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { it: String -> email = it },
                    placeholder = {
                        Text(
                            stringResource(R.string.enter_email_text),
                            color = lightGray,
                            fontSize = 12.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                )

                // Problem Field
                Spacer(Modifier.height(12.dp))
                Text("المشكلة", fontSize = 14.sp)
                OutlinedTextField(
                    value = problem,
                    onValueChange = { it: String -> problem = it },
                    placeholder = {
                        Text(
                            stringResource(R.string.write_here_message),
                            color = lightGray,
                            fontSize = 12.sp
                        ) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    textStyle = LocalTextStyle.current.copy(
                        textDirection = TextDirection.Rtl,
                        textAlign = TextAlign.Right
                    ),
                    maxLines = 5
                )

                // Submit Button
                Button(
                    onClick = { onSubmit(ReportModel(name, phone, email, problem)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)

                ) {
                    Text("إرسال", color = Color.White)
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
