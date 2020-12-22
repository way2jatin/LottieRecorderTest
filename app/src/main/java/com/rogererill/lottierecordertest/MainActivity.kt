package com.rogererill.lottierecordertest

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider.getUriForFile
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

  private val job = Job()
  private val scopeIO = CoroutineScope(job + Dispatchers.IO)
  private val scopeMainThread = CoroutineScope(job + Dispatchers.Main)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val textView: TextView = findViewById(R.id.tv_info)
    val startButton: Button = findViewById(R.id.button_start)
    val progressView:ProgressBar = findViewById(R.id.progress_view)

    startButton.setOnClickListener {
      textView.text = getString(R.string.recording)
      progressView.visibility = View.VISIBLE
      scopeIO.launch {
        val lottieComposition = LottieCompositionFactory.fromRawResSync(it.context, R.raw.android_wave) // Make sure to call this on a background thread!
        val lottieDrawable = LottieDrawable()
        lottieDrawable.composition = lottieComposition.value

        val path = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: File(cacheDir, Environment.DIRECTORY_PICTURES).apply { mkdirs() }
        val videoFile = File(path, "lottie_in_video.mp4")
        val recordingOperation = RecordingOperation(Recorder(videoOutput = videoFile), FrameCreator(lottieDrawable))
        {
          scopeMainThread.launch {
            progressView.visibility = View.GONE
            textView.text = getString(R.string.recording_finished)
            openCreatedVideo(videoFile)
          }
        }

        recordingOperation.start()  // Make sure to call this on a background thread!
      }
    }
  }

  private fun openCreatedVideo(videoFile: File) {
    val intent = Intent()
    intent.action = Intent.ACTION_VIEW
    val uriForFile = getUriForFile(this, "com.rogererill.provider", videoFile)
    intent.setDataAndType(uriForFile, "video/*")
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(intent)
  }
}
