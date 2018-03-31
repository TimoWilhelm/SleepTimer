package com.timowilhelm.sleeptimer

import android.content.Context
import android.media.AudioManager
import android.os.AsyncTask


abstract class LowerMediaVolumeTask(context: Context) : AsyncTask<Any, Unit, Any>() {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val originalVolumeIndex = getMediaVolume()

    private fun getMediaVolume(): Int {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    private fun setMediaVolume(volumeIndex: Int) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeIndex,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
    }

    abstract fun onFinished()
    override fun doInBackground(vararg params: Any?) {
        while (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)

            Thread.sleep(2000L)
        }
        onFinished()
        setMediaVolume(originalVolumeIndex)
    }

    override fun onCancelled() {
        setMediaVolume(originalVolumeIndex)
    }

}