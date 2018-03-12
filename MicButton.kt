package tech.crayfishentertainment.smartnotes

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.widget.ImageView
import java.util.*


/**
 * Created by domowy on 2018-03-11.
 */

class MicButton : android.support.v7.widget.AppCompatImageView {
    constructor(context: Context) : super(context)
    {init(context, null);}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    {init(context, attrs);}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    {init(context, attrs);}
    private val PRESSED_COLOR_LIGHTUP = 255 / 25
    private val PRESSED_RING_ALPHA = 75
    private val DEFAULT_PRESSED_RING_WIDTH_DIP = 4
    private val ANIMATION_TIME_ID = android.R.integer.config_shortAnimTime

    private var centerY= 0f
    private var centerX = 0f
    private var outerRadius = 0f
    private var pressedRingRadius = 0f

    private var circlePaint: Paint? = null
    private var focusPaint: Paint? = null

    private var animationProgress: Float = 0.toFloat()

    private var pressedRingWidth = 0f
    private var defaultColor = Color.TRANSPARENT
    private var pressedColor= Color.TRANSPARENT
    private var pressedAnimator: ObjectAnimator? = null


    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)

        if (circlePaint != null) {
            circlePaint!!.color = if (pressed) pressedColor else defaultColor
        }

        if (pressed) {
            showPressedRing()
        } else {
            hidePressedRing()
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, pressedRingRadius + animationProgress, focusPaint)
        canvas.drawCircle(centerX, centerY, outerRadius - pressedRingWidth, circlePaint)
        super.onDraw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        outerRadius = Math.min(w, h).toFloat() /2
        pressedRingRadius = outerRadius - pressedRingWidth - pressedRingWidth/2
    }

    fun getAnimationProgress(): Float {
        return animationProgress
    }

    fun setAnimationProgress(animationProgress: Float) {
        this.animationProgress = animationProgress
        this.invalidate()
    }

    fun setColor(color: Int) {
        this.defaultColor = color
        this.pressedColor = getHighlightColor(color, PRESSED_COLOR_LIGHTUP)

        circlePaint!!.color = defaultColor
        focusPaint!!.color = defaultColor

        this.invalidate()
    }

    private fun hidePressedRing() {
        pressedAnimator!!.setFloatValues(pressedRingWidth, 0f)
        pressedAnimator!!.start()
    }

    private fun showPressedRing() {
        pressedAnimator!!.setFloatValues(animationProgress, pressedRingWidth)
        pressedAnimator!!.start()
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        this.isFocusable = true
        this.scaleType = ImageView.ScaleType.CENTER_INSIDE
        isClickable = true

        circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint!!.style = Paint.Style.FILL

        focusPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        focusPaint!!.style = Paint.Style.STROKE

        pressedRingWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PRESSED_RING_WIDTH_DIP.toFloat(), resources
                .displayMetrics)

        var color = Color.TRANSPARENT
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.CircleButton)
            color = a.getColor(R.styleable.CircleButton_cb_color, color)
            pressedRingWidth = a.getDimension(R.styleable.CircleButton_cb_pressedRingWidth, pressedRingWidth)
            a.recycle()
        }

        setColor(color)

        focusPaint!!.strokeWidth = pressedRingWidth
        val pressedAnimationTime = resources.getInteger(ANIMATION_TIME_ID)
        pressedAnimator = ObjectAnimator.ofFloat(this, "animationProgress", 0f, 0f)
        pressedAnimator!!.duration = pressedAnimationTime.toLong()


        speechRecognizer.setRecognitionListener(object: RecognitionListener {
            override fun onRmsChanged(rmsdB: Float) {
                setAnimationProgress(rmsdB)
            }

            override fun onBufferReceived(buffer: ByteArray?) {
            }

            override fun onPartialResults(results: Bundle?) {
                results?.let {
                    micListener!!.onResultFound(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
            }

            override fun onError(error: Int) {
            }

            override fun onReadyForSpeech(params: Bundle?) {

            }

            override fun onBeginningOfSpeech() {

            }

            override fun onEndOfSpeech() {
            }

            override fun onResults(results: Bundle?) {

                results?.let {
                    micListener!!.onResultFound(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION))
                }

            }

        })
    }

    private fun getHighlightColor(color: Int, amount: Int): Int {
        return Color.argb(Math.min(255, Color.alpha(color)), Math.min(255, Color.red(color) + amount),
                Math.min(255, Color.green(color) + amount), Math.min(255, Color.blue(color) + amount))
    }
    val speechRecognizer by lazy {SpeechRecognizer.createSpeechRecognizer(context)}
    var myLanguage = "en-US"
    interface MicListener{
        fun onResultFound(result:ArrayList<String>)
    }
    var micListener:MicListener? = null


    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when(event?.action)
        {
            ACTION_DOWN ->
            {
                setColor(Color.RED)
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, myLanguage)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, myLanguage)
                intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, myLanguage)
                intent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", Array<String>(1,{myLanguage}))
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        "Say something")
                speechRecognizer.startListening(intent)
            }
            ACTION_UP -> {
                setColor(Color.TRANSPARENT)
                speechRecognizer.stopListening()
            }
        }
       return  true
    }

}
