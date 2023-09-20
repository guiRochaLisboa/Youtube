package com.example.youtube

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.SeekBar
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.video_detail.*
import kotlinx.android.synthetic.main.video_detail.view.*
import kotlinx.android.synthetic.main.video_detail_content.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var videoAdapter : VideoAdapter
    private lateinit var youtubePlayer : YoutubePlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        val videos = mutableListOf<Video>()
        videoAdapter = VideoAdapter(videos){video ->
           showOverlayView(video)
        }
        view_layer.alpha = 0f

        rv_main.layoutManager = LinearLayoutManager(this)
        rv_main.adapter = videoAdapter


        CoroutineScope(Dispatchers.IO).launch {
            val res = async { getVideo() }
            val listVideo = res.await()
            withContext(Dispatchers.Main){
                listVideo?.let {
                    videos.clear()
                    videos.addAll(listVideo.data)
                    videoAdapter.notifyDataSetChanged()
                    motion_container.removeView(progress_recycler)
                    //progress_recycler.visibility = View.GONE
                }
            }
        }

        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    youtubePlayer.seek(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })
        preparePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        youtubePlayer.release()
    }

    override fun onPause() {
        super.onPause()
        youtubePlayer.pause()
    }

     private fun preparePlayer(){
         youtubePlayer = YoutubePlayer(this)
         youtubePlayer
         youtubePlayer.youtubePlayerListener = object : YoutubePlayer.YoutubePlayerListener{
             override fun onPrepared(duration: Int) {

             }

             override fun onTrackTime(currentePosition: Long,percent: Long) {
                 motion_container.seek_bar.progress = percent.toInt()
                 motion_container.current_time.text = currentePosition.formatTime()
                 println(currentePosition.formatTime())
             }

         }
        surface_player.holder.addCallback(youtubePlayer)
    }

    private fun showOverlayView(video: Video) {
        view_layer.animate().apply {
            duration = 400
            alpha(0.5f)
        }

        motion_container.setTransitionListener(object : MotionLayout.TransitionListener{
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {

            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
                if(progress > 0.5f)
                view_layer.alpha = 1.0f - progress
                else
                    view_layer.alpha = 0.5f
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {

            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {

            }

        })

        video_player.visibility = View.GONE
        youtubePlayer.setUrl(video.videoUrl)

        val detailAdapter = VideoDetailAdapter(videos())
        rv_similar.layoutManager = LinearLayoutManager(this)
        rv_similar.adapter = detailAdapter

        content_channel.text = video.publisher.name
        content_title.text = video.title
        Picasso.get().load(video.publisher.pictureProfileUrl).into(img_channel)

        detailAdapter.notifyDataSetChanged()
    }

    //Função chamada para criação do nosso menu na toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }


    //Na chamada na nossa api
    private fun getVideo() : ListVideo? {
        val client = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .get()
            .url("https://tiagoaguiar.co/api/youtube-videos")
            .build()

        return try {
        val response = client.newCall(request).execute()

            if(response.isSuccessful){
                GsonBuilder().create()
                    .fromJson(response.body()?.string(), ListVideo::class.java)
            }else{
                null
            }
        }catch (e: Exception){
            null
        }
    }
}