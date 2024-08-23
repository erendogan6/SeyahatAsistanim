package com.erendogan6.seyahatasistanim.presentation.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.erendogan6.seyahatasistanim.R

@Composable
fun lottieLoadingScreen(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )

    Log.d(stringResource(id = R.string.lottie_log_tag), stringResource(id = R.string.loading_animation_requested))

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (composition != null) {
            Log.d(stringResource(id = R.string.lottie_log_tag), stringResource(id = R.string.animation_loaded))
            LottieAnimation(
                composition = composition,
                progress = { progress },
            )
        } else {
            Log.e(stringResource(id = R.string.lottie_log_tag), stringResource(id = R.string.animation_load_failed))
        }
    }
}
