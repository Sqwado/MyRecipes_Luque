package com.example.myrecipes_luque.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.example.myrecipes_luque.R
import kotlin.math.max
import kotlin.math.min

/**
 * Indicateur nutritionnel (BN3) : barre 0 % (rouge) → 100 % (vert) avec curseur.
 */
class NutriScoreView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val knobFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val knobStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.brand_brown)
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density * 2f
    }

    private val barRect = RectF()
    private var gradient: LinearGradient? = null

    private var barHeightPx: Float =
        resources.getDimension(R.dimen.nutri_bar_height)
    private var knobRadiusPx: Float =
        resources.getDimension(R.dimen.nutri_knob_radius)

    var score: Int = 0
        set(value) {
            field = value.coerceIn(0, 100)
            contentDescription = context.getString(R.string.nutri_score_a11y, field)
            invalidate()
        }

    init {
        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.NutriScoreView) {
                score = getInt(R.styleable.NutriScoreView_nutriScore, 0)
                barHeightPx = getDimension(
                    R.styleable.NutriScoreView_nutriBarHeight,
                    barHeightPx,
                )
                knobRadiusPx = getDimension(
                    R.styleable.NutriScoreView_nutriKnobRadius,
                    knobRadiusPx,
                )
            }
        }
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (knobRadiusPx * 2f + paddingTop + paddingBottom).toInt()
            .coerceAtLeast((barHeightPx + paddingTop + paddingBottom).toInt())
        val width = resolveSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val left = paddingLeft + knobRadiusPx
        val right = w - paddingRight - knobRadiusPx
        val cy = h / 2f
        val halfBar = barHeightPx / 2f
        barRect.set(left, cy - halfBar, max(left, right), cy + halfBar)
        gradient = LinearGradient(
            barRect.left,
            0f,
            barRect.right,
            0f,
            intArrayOf(
                context.getColor(R.color.nutri_score_low),
                context.getColor(R.color.nutri_score_mid),
                context.getColor(R.color.nutri_score_high),
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP,
        )
        barPaint.shader = gradient
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = barHeightPx / 2f
        canvas.drawRoundRect(barRect, radius, radius, barPaint)

        val fraction = score / 100f
        val cx = barRect.left + barRect.width() * fraction
        val cy = height / 2f
        canvas.drawCircle(cx, cy, knobRadiusPx, knobFillPaint)
        canvas.drawCircle(cx, cy, knobRadiusPx, knobStrokePaint)
    }

    fun setScorePercent(percent: Int) {
        score = min(100, max(0, percent))
    }
}
