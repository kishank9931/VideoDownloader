package com.videodownloader.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.videodownloader.app.ui.nav.AppNavGraph
import com.videodownloader.app.ui.theme.VideoDownloaderTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If the user shared a link into the app (e.g. from a browser's
        // "Share" menu), pre-fill and kick off resolution automatically.
        if (intent?.action == android.content.Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(android.content.Intent.EXTRA_TEXT)?.let { sharedUrl ->
                viewModel.onUrlSubmitted(sharedUrl)
            }
        }

        setContent {
            VideoDownloaderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(viewModel)
                }
            }
        }
    }
}
