package org.sagebionetworks.research.mobiletoolbox.app.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sagebionetworks.assessmentmodel.presentation.compose.DefaultButtonColors
import org.sagebionetworks.assessmentmodel.presentation.compose.SageButton
import org.sagebionetworks.assessmentmodel.presentation.ui.theme.BackgroundGray
import org.sagebionetworks.assessmentmodel.presentation.ui.theme.SageBlack
import org.sagebionetworks.assessmentmodel.presentation.ui.theme.SageSurveyTheme
import org.sagebionetworks.assessmentmodel.presentation.ui.theme.sageH1
import org.sagebionetworks.assessmentmodel.presentation.ui.theme.sageP2
import org.sagebionetworks.research.mobiletoolbox.app.R

@Composable
internal fun UpdateNowUi(
    modifier: Modifier = Modifier,
    onButtonClick:()->Unit,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(BackgroundGray),
    ) {
        Column(
            modifier = modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(45.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.update_now),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.height(100.dp))
            Text(
                text = stringResource(R.string.we_are_getting_better),
                style = sageH1,
                modifier = Modifier
                    .align(CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.update_the_app),
                style = sageP2,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(CenterHorizontally)
            )
            Spacer(modifier = Modifier.weight(1f))
            SageButton(
                onClick = onButtonClick,
                text = stringResource(R.string.update_now),
                drawBorder = false,
                buttonColors = DefaultButtonColors(
                    backgroundColor = colorResource(R.color.colorPrimary),
                    contentColor = SageBlack,
                    disabledBackgroundColor = colorResource(R.color.colorPrimary).copy(alpha = ContentAlpha.disabled),
                    disabledContentColor = SageBlack.copy(alpha = ContentAlpha.disabled)
                ),
                modifier = modifier.fillMaxWidth()
            )
        }
    }
}


@Preview
@Composable
private fun UpdateNowPreview() {
    SageSurveyTheme {
        UpdateNowUi(onButtonClick = {})
    }
}
