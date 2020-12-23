package com.udacity.custom_button

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.postDelayed
import com.udacity.R
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.properties.Delegates


class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress: Float = 0f
    private var lastProgress: Float = 0f
    private val textRect = Rect()
    private val circleRect = RectF()
    private val cornerPath = Path()
    private val cornerRadius = 25f
    private val loadingRect = Rect()

    private val buttonText: String
        get() = context.getString(this.buttonState.textID)
    private var backColor = context.getColor(R.color.colorPrimary)
    private var backInProgressColor = context.getColor(R.color.colorPrimaryDark)
    private var circColor = context.getColor(R.color.colorAccent)

    private var valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { prop, oldVal, newVal ->

        when(newVal){
            is ButtonState.Loading -> {
                if (oldVal != ButtonState.Loading) {
                    setAnim()
                    setButtonBgColor(context.getColor(R.color.button_loading_color))
                }
            }

            is ButtonState.Completed -> {
                setButtonBgColor(context.getColor(R.color.button_completed_color))
                setButtonProgress(1f)
            }

            is ButtonState.Failed -> {
                setButtonBgColor(context.getColor(R.color.button_failed_color))
                resetProgress()
            }

            else -> {} //empty
        }

        invalidate()
        requestLayout()
    }

    private fun setAnim() {
        valueAnimator.cancel()
        valueAnimator = ValueAnimator.ofFloat(progress, lastProgress).apply {
            interpolator = AccelerateDecelerateInterpolator()
            // 1.5 second for 100% progress, 750ms for 50% progress, etc.
            val animDuration = abs(1500 * ((lastProgress - progress) / 100)).toLong()
            duration = if (animDuration >= 400){
                animDuration
            }else{
                400
            }
            addUpdateListener { animation ->
                progress = animation.animatedValue as Float
                postInvalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    if (progress == 1f){
                        resetProgress()
                    }else{
                        valueAnimator.cancel()
                    }
                }
            })
            start()
        }
    }

    // set the color of our button's background
    private fun setButtonBgColor(backgroundColor: Int) {
        backColor = backgroundColor
    }

    // Reset our button progress
    private fun resetProgress() {
        lastProgress = 0f
        setAnim()
    }

    fun setButtonProgress(progressF: Float) {
        if (progress < progressF){
            lastProgress = progressF
            setAnim()
        }
    }

    // Very regularly, it seems DownloadManager.COLUMN_TOTAL_SIZE_BYTES doesn't like working, so
    // we'll fake some progress if we've actually downloaded a few bytes.
    fun addButtonProgress(progressF: Float) {
        if (progress < 0.65f){
            setButtonProgress(progress + progressF)
        }
    }


    init {
        setLoadingButtonState(ButtonState.Init)
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadingButton,
            defStyleAttr,
            0
        ).apply {
            try {
                backColor = getColor(R.styleable.LoadingButton_backgroundColor, backColor)
                backInProgressColor = getColor(
                    R.styleable.LoadingButton_inProgressBackgroundColor,
                    backInProgressColor
                )
                circColor = getColor(R.styleable.LoadingButton_circleProgressColor, circColor)
            } finally {
                recycle()
            }
        }
    }

    // styling the default background
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backColor
    }

    // styling the in-progress background
    private val inProgressBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backInProgressColor
    }

    private val inProgressArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = circColor
        style = Paint.Style.FILL
    }

    // Text styling
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlignment = TEXT_ALIGNMENT_CENTER
        textSize = 60f
        color = Color.WHITE
        typeface = Typeface.DEFAULT_BOLD
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            save()
            clipPath(cornerPath)
            drawColor(backgroundPaint.color)
            textPaint.getTextBounds(buttonText, 0, buttonText.length, textRect)
            val textX = width / 2f - textRect.width() / 2f
            val textY = height / 2f + textRect.height() / 2f - textRect.bottom
            var textOffset = 0

            loadingRect.set(0, 0, (width * progress).roundToInt(), height)
            drawRect(loadingRect, inProgressBackgroundPaint)


            if (buttonState == ButtonState.Loading) {
                val circleStartX = width / 2f + textRect.width() / 2f
                val circleStartY = height / 2f - 20
                circleRect.set(circleStartX, circleStartY, circleStartX + 40, circleStartY + 40)
                drawArc(
                    circleRect, progress, progress * 360f, true, inProgressArcPaint
                )
                textOffset = 35
            }


            drawText(buttonText, textX - textOffset, textY, textPaint)
            restore()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cornerPath.reset()
        cornerPath.addRoundRect(
            0f,
            0f,
            w.toFloat(),
            h.toFloat(),
            cornerRadius,
            cornerRadius,
            Path.Direction.CW
        )
        cornerPath.close()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minW = paddingLeft + paddingRight + suggestedMinimumWidth
        val minH = paddingTop + paddingBottom + suggestedMinimumHeight
        val w = resolveSizeAndState(minW, widthMeasureSpec, 1)
        val h = resolveSizeAndState(minH, heightMeasureSpec, 1)
        setMeasuredDimension(w, h)
    }

    // Set state of button from Activity or otherwise
    fun setLoadingButtonState(state: ButtonState) {
        buttonState = state
    }

}