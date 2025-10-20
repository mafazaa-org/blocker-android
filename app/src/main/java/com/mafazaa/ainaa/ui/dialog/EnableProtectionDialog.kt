package com.mafazaa.ainaa.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.ui.theme.AinaaTheme
import com.mafazaa.ainaa.ui.theme.red

/**
 * last dialog before enabling protection
 */
@Composable
fun EnableProtectionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)

            ) {
                var scrollState = rememberScrollState()
                var isEndReached by remember {
                    mutableStateOf(false)
                }
                isEndReached = scrollState.value == scrollState.maxValue || isEndReached

                val isScrollAtEnd by remember {
                    derivedStateOf {
                        scrollState.value == scrollState.maxValue
                    }
                }

                // Timer state for enabling the OK button after 5 seconds
                var timerSeconds by remember { mutableStateOf(5) }
                var timerActive by remember { mutableStateOf(true) }
                val isOkEnabled = isEndReached && timerSeconds == 0

                // Start countdown timer when dialog is shown
                if (timerActive && timerSeconds > 0) {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        while (timerSeconds > 0) {
                            kotlinx.coroutines.delay(1000)
                            timerSeconds--
                        }
                        timerActive = false
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "تنويه",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_close_24),
                            contentDescription = "close",
                        )
                    }
                }


                Text(
                    text = "بمجرد تفعيل الحماية، سيتم تطبيق الإجراءات التالية:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "خاصية عدم الحذف",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "عند تفعيل الحماية، يتم تفعيل خاصية تمنع إزالة (حذف) البرنامج من الهاتف دون المرور بإجراءات محددة وصارمة. هذه الخاصية مصممة لضمان استمرارية الحماية ومنع أي مستخدم من تعطيلها بسهولة.",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "سيكون من المستحيل إزالة التطبيق بالطرق التقليدية (السحب أو الحذف المباشر).",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "في حال رغبتك بإزالة التطبيق مستقبلاً، سيتعين عليك التواصل مع خدمة العملاء واتباع إجراءات التحقق والفك الخاصة بهم.",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "لذلك، فإن موافقتك على تفعيل الحماية تعتبر موافقة صريحة منك على تفعيل خاصية عدم الإزالة والالتزام بجميع قيود الحماية المترتبة عليها.",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "هل ترغب في المتابعة وتفعيل الحماية الآن؟",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = red,
                    modifier = Modifier.padding(bottom = 24.dp)
                )


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = red)
                ) {
                    Text("لاحقاً")
                }

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = red),

                    ) {
                    Text("فعل الحماية")
                }

            }}
        }
    }
}

@Preview(showBackground = false, showSystemUi = true)
@Composable
fun EnableProtectionDialogPreview() {
    AinaaTheme {
        EnableProtectionDialog(onDismiss = {}, onConfirm = {})
    }
}

