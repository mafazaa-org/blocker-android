package com.mafazaa.ainaa.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mafazaa.ainaa.R

@Composable
fun ReportLink(modifier: Modifier = Modifier, onReportClick: () -> Unit) {
    TwoColorText(
        modifier,
        stringResource(R.string.bug_website_not_blocked_text),
        stringResource(R.string.tell_us_about_text),
        onReportClick
    )
}

