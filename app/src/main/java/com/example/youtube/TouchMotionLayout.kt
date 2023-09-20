package com.example.youtube

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationSet
import android.widget.ImageView
import android.widget.SeekBar
import androidx.constraintlayout.motion.widget.MotionLayout
import kotlinx.android.synthetic.main.video_detail.view.*
import kotlin.math.abs

class TouchMotionLayout(context: Context, attributeSet: AttributeSet) :
    MotionLayout(context, attributeSet) {

    private val iconArrowDow: ImageView by lazy {
        findViewById(R.id.hide_player)
    }

    private val imgBase: ImageView by lazy {
        findViewById(R.id.video_player)
    }

    private val playButton: ImageView by lazy {
        findViewById(R.id.play_button)
    }

    private val seekBar: SeekBar by lazy {
        findViewById(R.id.seek_bar)
    }


    private var startX: Float? = null
    private var startY: Float? = null
    private var isPaused = false

    private lateinit var animFadeIn: AnimatorSet
    private lateinit var animFadeOut: AnimatorSet

    ///Eventos de touch
    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        val isInTarget: Boolean = touchEventInsideTargetView(imgBase, event!!)
        val isInProgress: Boolean = (progress > 0.0f && progress < 1.0f)

        return if (isInProgress || isInTarget) {
            return super.onInterceptTouchEvent(event)
        } else {
            return false
        }
    }

    private fun touchEventInsideTargetView(v: View, ev: MotionEvent): Boolean {
        if (ev.x > v.left && ev.x < v.right) {
            if (ev.y > v.top && ev.y < v.bottom) {
                return true
            }
        }
        return false
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x
                startY = ev.y
            }
            MotionEvent.ACTION_UP -> {
                val endX = ev.x
                val endY = ev.y

                if (isAClick(startX!!, endX, startY!!, endY)) {
                    if (touchEventInsideTargetView(imgBase, ev)) {
                        if (doClick(imgBase)) {
                            return true
                        }
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isAClick(starX: Float, endX: Float, starY: Float, endY: Float): Boolean {
        val differenceX = abs(starX - endX)
        val differenceY = abs(starY - endY)

        return !(differenceX > 200 || differenceY > 200)
    }

    private fun doClick(view: View): Boolean {
        var isClickHandle = false

        if (progress < 0.5f) {
            isClickHandle = true

            when (view) {
                imgBase -> {
                    if (isPaused) {

                    } else {
                        animateFade {
                            animFadeOut.startDelay = 1000
                            animFadeOut.start()
                        }
                    }
                }
            }
        }
        return isClickHandle
    }

    private fun animateFade(onAnimationEndOn: () -> Unit) {
        animFadeOut = AnimatorSet()
        animFadeIn = AnimatorSet()

        fade(animFadeIn, arrayOf(
            play_button,
            hide_player,
            next_button,
            preview_button,
            playlist_player,
            full_player,
            share_player,
            more_player,
            current_time,
            duration_time
        ),true)

        animFadeIn.play(
            ObjectAnimator.ofFloat(view_frame, View.ALPHA, 0f, .5f)
        )

        val valueFadeIn = ValueAnimator.ofInt(0,255)
            .apply {
                addUpdateListener {
                    seek_bar.thumb.mutate().alpha = it.animatedValue as Int
                }
                duration = 200
            }

        animFadeIn.play(valueFadeIn)

        fade(animFadeOut, arrayOf(
            play_button,
            hide_player,
            next_button,
            preview_button,
            playlist_player,
            full_player,
            share_player,
            more_player,
            current_time,
            duration_time
        ), false)

        val valueFadeOut = ValueAnimator.ofInt(255,0)
            .apply {
                addUpdateListener {
                    seek_bar.thumb.mutate().alpha = it.animatedValue as Int
                }
                duration = 200
            }

        animFadeOut.play(valueFadeOut)

        animFadeIn.addListener(object : Animator.AnimatorListener{
            override fun onAnimationStart(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                onAnimationEndOn.invoke()
            }

            override fun onAnimationCancel(p0: Animator) {

            }

            override fun onAnimationRepeat(p0: Animator) {

            }
        })
        animFadeIn.start()

    }

    private fun fade(animatorSet: AnimatorSet, view: Array<View>, toZero: Boolean) {
        view.forEach {
            animatorSet.play(
                ObjectAnimator.ofFloat(
                    it, View.ALPHA,
                    if (toZero) 0f else 1f,
                    if (toZero) 1f else 0f

                )
            )
        }
    }

}