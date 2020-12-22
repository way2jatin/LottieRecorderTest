package com.rogererill.lottierecordertest

class RecordingOperation(
    private val recorder: Recorder,
    private val frameCreator: FrameCreator,
    private val listener: () -> Unit
) {

  suspend fun start() {
    while (isRecording()) {
      recorder.nextFrame(frameCreator.generateFrame())
    }
    recorder.end()
    listener()
  }

  private fun isRecording() = !frameCreator.hasEnded()
}