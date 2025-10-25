package com.mafazaa.ainaa.ui.dialog

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.rememberAsyncImagePainter
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.ui.theme.lightGray
import com.mafazaa.ainaa.ui.theme.red

@Composable
fun HowItWorksDialog(
    onContactClicked: () -> Unit,
    onSafeSearchClicked: () -> Unit,
    image: Uri,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        Scaffold(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(16.dp)),
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text(stringResource(R.string.okay_text))
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth(.9f)
                    .padding(innerPadding),
            ) {

                val title = stringResource(R.string.congrats_enable_protection_text)
                val introText = stringResource(R.string.enable_protection_service_message).trimIndent()
                val failureText = stringResource(R.string.enable_protection_failed_message).trimIndent()
                val outroText = stringResource(R.string.enable_protection_report_message).trimIndent()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    // Normal text
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth()
                    )
                    Text(
                        text = introText,

                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )

                    // First clickable link
                    Text(
                        text = stringResource(R.string.click_link_text),
                        color = red,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            // open browser
                            onSafeSearchClicked()
                        }
                    )
                    Image(
                        painter = rememberAsyncImagePainter(model = image),
                        contentDescription = stringResource(R.string.safe_search_image),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .border(1.dp, lightGray, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Text(
                        text = failureText,
                        textAlign = TextAlign.Start
                    )

                    // Second clickable link
                    Text(
                        text = stringResource(R.string.contact_customer_service_text),
                        color = red,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            onContactClicked()
                        }
                    )

                    Text(text = outroText)


                }
            }
        }
    }
}