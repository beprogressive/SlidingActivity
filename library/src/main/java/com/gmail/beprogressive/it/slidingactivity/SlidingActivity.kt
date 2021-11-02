package com.gmail.beprogressive.it.slidingactivity

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.window.layout.WindowMetricsCalculator
import kotlin.math.abs

/**
 * Created by Lyudmila Kostik on 31.05.2017
 */
interface SlidingInstance {
    fun canSlideRight(): Boolean
    fun onInstanceFinish()
    fun getSlidingContainer(): View?
}

abstract class SlidingActivity : AppCompatActivity(), SlidingInstance {

    companion object {
        private const val GESTURE_THRESHOLD = 10
    }

    var root: View? = null

    private var startX = 0f
    private var startY = 0f
    private var isSliding = false

    private lateinit var currentBounds: Rect

    private lateinit var windowScrim: ColorDrawable

    private var alpha = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        windowScrim = ColorDrawable(Color.argb(120, 0, 0, 0))
        windowScrim.alpha = 0
        window.setBackgroundDrawable(windowScrim)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
        currentBounds = windowMetrics.bounds
    }

    lateinit var slidingInstance: SlidingInstance
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        var handled = false

        if (ev.action == MotionEvent.ACTION_DOWN) {
            val fragment =
                (supportFragmentManager.fragments.findLast { fgm -> fgm.isVisible } as? SlidingFragment)

            if (fragment != null) {
                fragment.view?.background = windowScrim
                slidingInstance = fragment
            } else {
                slidingInstance = this
            }

            root = slidingInstance.getSlidingContainer()
        }

        if (root == null) {
            Log.w("SlidingActivity", "getSlidingContainer() must not be null!")
            return super.dispatchTouchEvent(ev)
        }

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                // запоминаем точку старта
                startX = ev.x
                startY = ev.y
            }

            MotionEvent.ACTION_MOVE -> {
                // нужно определить, является ли текущий жест "смахиванием вниз"
                if ((isSlidingRight(
                        startX,
                        startY,
                        ev
                    ) && slidingInstance.canSlideRight()) || isSliding
                ) {
                    if (!isSliding) {
                        // момент, когда мы определили, что польователь "смахивает" экран
                        // начиная с этого жеста все последующие ACTION_MOVE мы будем
                        // воспринимать как "смахивание"
                        isSliding = true
                        onSlidingStarted()

                        // сообщим всем остальным обработчикам, что жест закончился
                        // и им не нужно больше ничего обрабатывать
                        ev.action = MotionEvent.ACTION_CANCEL
                        super.dispatchTouchEvent(ev)
                    }
                    // переместим контейнер на соответсвующую Y координату
                    // но не выше, чем точка старта
                    root?.x = (ev.x - startX).coerceAtLeast(0f)

                    updateScrim()

                    handled = true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                if (isSliding) {
                    // если пользователь пытался "смахнуть" экран...
                    isSliding = false
                    onSlidingFinished()
                    handled = true
                    if (shouldClose(ev.x - startX)) {
                        // закрыть экран
//                        finish()
                        closeDownAndDismiss()
                    } else {
                        // вернуть все как было
                        root?.x = 0f
                    }
                }
                startX = 0f
                startY = 0f

            }
        }

        return if (handled) true else super.dispatchTouchEvent(ev)
    }

    private fun isSlidingRight(startX: Float, startY: Float, ev: MotionEvent): Boolean {
//        val deltaX = abs(startX - ev.x)
//        if (deltaX > GESTURE_THRESHOLD) return false
//        val deltaY = ev.y - startY
//        return deltaY > GESTURE_THRESHOLD

        val deltaY = abs(startY - ev.y)
        if (deltaY > GESTURE_THRESHOLD) return false
        val deltaX = ev.x - startX
        return deltaX > GESTURE_THRESHOLD
    }

    abstract override fun getSlidingContainer(): View

//    abstract fun onFragmentFinish()

    abstract fun onSlidingFinished()

    abstract fun onSlidingStarted()

    abstract override fun canSlideRight(): Boolean

    private fun shouldClose(delta: Float): Boolean {
        return delta > currentBounds.width() / 4
    }

    private fun closeDownAndDismiss() {

//        val moveX = ObjectAnimator.ofFloat(root, "x", root.x, screenSize.x.toFloat())
        val moveY =
            ObjectAnimator.ofFloat(root!!, "y", root!!.y, currentBounds.height().toFloat())
        moveY.duration = 400
//        val `as` = AnimatorSet()
//        `as`.playTogether(moveX, moveY)
        moveY.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                root!!.x = currentBounds.width().toFloat()
                root!!.y = currentBounds.height().toFloat()
                windowScrim.alpha = 0
                finishScrim()
                slidingInstance.onInstanceFinish()
            }

            override fun onAnimationCancel(animation: Animator) {
                updateScrim()
            }

            override fun onAnimationStart(animation: Animator) {}

        })
        moveY.start()
    }

    private fun updateScrim() {
        val progress = root!!.x / currentBounds.width()
        alpha = (progress * 255f).toInt()
        windowScrim.alpha = 255 - alpha
    }

    private fun finishScrim() {
//        val progress = root!!.x / currentBounds.width()
//        val alphaA = alpha - (progress * 255f).toInt()
//        windowScrim.alpha = 255 - alphaA
    }

    fun hideStatusBar() {
        root.let {
            if (it == null) return

            WindowCompat.setDecorFitsSystemWindows(window, false)

            WindowInsetsControllerCompat(window, it).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    fun showStatusBar() {
        root.let {
            if (it == null) return

            WindowCompat.setDecorFitsSystemWindows(window, true)

            WindowInsetsControllerCompat(window, it).show(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onInstanceFinish() {
        finish()
    }
}

abstract class SlidingFragment : Fragment(), SlidingInstance, LifecycleObserver {

    abstract override fun getSlidingContainer(): View

    override fun onInstanceFinish() {
        activity?.supportFragmentManager?.popBackStack()
    }

    abstract override fun canSlideRight(): Boolean
}