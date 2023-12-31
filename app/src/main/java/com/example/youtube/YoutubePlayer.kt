package com.example.youtube

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.view.SurfaceHolder
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.mediacodec.MediaCodecInfo.TAG
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.google.android.material.color.utilities.MaterialDynamicColors


//Classe referente ao nosso Callback com nosso VideoPlayer

class YoutubePlayer(private val context: Context) : SurfaceHolder.Callback{

    private var mediaPlayer: ExoPlayer? = null
    var youtubePlayerListener: YoutubePlayerListener? = null
    private lateinit var runnable: Runnable
    private val handler= Handler()



    override fun surfaceCreated(holder: SurfaceHolder) {
        if(mediaPlayer == null){
            mediaPlayer = ExoPlayer.Builder(context).build()
            mediaPlayer?.setVideoSurfaceHolder(holder)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        mediaPlayer?.release()
    }

    //////  @SuppressLint("UnsafeOptInUsageError") foi necessário para alguns métodos depreciados do ExoPlayer \\\\\\\\\\\\
    fun setUrl(url: String) {

        mediaPlayer?.let {
            val dataSourceFactory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context,"utube")
            )

            val mediaItem = MediaItem.fromUri(Uri.parse(url))

            val videoSource : MediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)

            it.prepare(videoSource)
            it.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    if(isPlaying)
                        trackTime()
                }
            })
            play()
        }
    }



    /////Método chamado quando nosso vídeo está sendo apresentado, atualizando nosso contador e SeekBar
    private fun trackTime(){
        mediaPlayer?.let {
            youtubePlayerListener?.onTrackTime(it.currentPosition,it.currentPosition * 100 / it.duration)
            if (it.isPlaying){
                runnable = Runnable {
                    trackTime()
                }
                handler.postDelayed(runnable,1000)
            }
        }
    }

    fun play(){
        mediaPlayer?.playWhenReady = true
    }

    fun pause(){
        mediaPlayer?.playWhenReady = false
    }

    fun release(){
        mediaPlayer?.release()
    }

    fun seek(progress: Long){
        if(progress > 0 ){
            mediaPlayer?.let {
                val seek = progress * it.duration / 100
                it.seekTo(seek)
            }
        }
    }

    //Interface para atualizarmos nossa MainActivity.
    interface YoutubePlayerListener {
        fun onPrepared(duration: Int)
        fun onTrackTime(currentePosition: Long,percent: Long)
    }
}