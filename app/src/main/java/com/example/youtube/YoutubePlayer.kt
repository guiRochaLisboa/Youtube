package com.example.youtube

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.SurfaceHolder
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource


//Classe referente ao nosso Callback com nosso VideoPlayer

class YoutubePlayer(private val context: Context) : SurfaceHolder.Callback{

    private var mediaPlayer: ExoPlayer? = null


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

    @SuppressLint("UnsafeOptInUsageError")
    fun setUrl(url: String){
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
            play()
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

}