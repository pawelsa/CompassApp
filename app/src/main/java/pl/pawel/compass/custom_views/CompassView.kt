package pl.pawel.compass.custom_views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import pl.pawel.compass.R
import pl.pawel.compass.utils.toRadians
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CompassView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        setWillNotDraw(false)
    }

    private val compassPaint by lazy {
        Paint().apply {
            strokeWidth = 10f
            style = Paint.Style.STROKE
            color = ContextCompat.getColor(context, R.color.green_500)
        }
    }
    private val compassTextPaint by lazy {
        TextPaint().apply {
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            color = ContextCompat.getColor(context, R.color.green_500)
        }
    }
    private val destinationPaint by lazy {
        Paint().apply {
            strokeWidth = 10f
            style = Paint.Style.STROKE
            color = ContextCompat.getColor(context, android.R.color.holo_red_light)
        }
    }
    private val destinationTextPaint by lazy {
        TextPaint().apply {
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            color = ContextCompat.getColor(context, android.R.color.holo_red_light)
        }
    }
    private val center by lazy { PointF(width / 2f, height / 2f) }
    private val radius by lazy {
        val smallerDim = min(height, width) * 0.7f
        smallerDim / 2f
    }
    private val distanceForLetterFromCenter by lazy {
        val smallerDim = min(height, width) * 0.75f
        smallerDim / 2f
    }

    var northAngle: Float? = null
        set(value) {
            field = when {
                value != null -> value - 90f
                else -> null
            }
            invalidate()
        }
    var destinationAngle: Float? = null
        set(value) {
            field = when {
                value != null -> value - 90f
                else -> null
            }
            invalidate()
        }

    override fun onDraw(canvas: Canvas?) {
        canvas?.save()


        northAngle?.let { angle ->
            val end = pointAtAngleFromCenter(angle)

            canvas?.drawLineAtAngle(end, compassPaint)
            canvas?.drawTextAtAngle("N", angle, compassTextPaint)
        }

        destinationAngle?.let { angle ->

            val end = pointAtAngleFromCenter(angle)

            canvas?.drawLineAtAngle(end, destinationPaint)
            canvas?.drawTextAtAngle("D", angle, destinationTextPaint)
        }


        super.onDraw(canvas)
        canvas?.restore()
    }

    private fun pointAtAngleFromCenter(angle: Float) = PointF(
        center.x + radius * cos(angle.toRadians()),
        center.y + radius * sin(angle.toRadians())
    )

    private fun Canvas.drawLineAtAngle(end: PointF, paint: Paint) {
        drawLine(center.x, center.y, end.x, end.y, paint)
    }

    private fun Canvas.drawTextAtAngle(text: String, angle: Float, paint: Paint) {
        val alteredAngleForText = angle + 2
        val letterPlacing = PointF(
            center.x + distanceForLetterFromCenter * cos(alteredAngleForText.toRadians()),
            center.y + distanceForLetterFromCenter * sin(
                alteredAngleForText.toRadians()
            )
        )
        drawText(text, letterPlacing.x, letterPlacing.y, paint)
    }

}