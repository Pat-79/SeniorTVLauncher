package nl.awayfromhome.seniortvlauncher.ui.launcher

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import nl.awayfromhome.seniortvlauncher.R
import nl.awayfromhome.seniortvlauncher.data.AppInfo
import nl.awayfromhome.seniortvlauncher.data.ButtonShape
import nl.awayfromhome.seniortvlauncher.utils.ColorUtils
import kotlin.math.sqrt

class AppButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val iconContainer: ConstraintLayout
    private val iconView: ImageView
    private val appNameView: TextView
    private val addIconView: ImageView

    private var dominantColor: Int = Color.WHITE
    private var isFocused: Boolean = false
    private var isEmptySlot: Boolean = true
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * Animation progress: 0 = fully unfocused, 1 = fully focused.
     * Drives both the icon-container scale and the glow size/alpha together so
     * both grow proportionally on every frame.
     */
    private var glowProgress: Float = 0f
    private var currentAnimator: ValueAnimator? = null

    /** How far (px) the glow bleeds outside the tile's own bounds when fully focused. */
    private var glowOverflowPx: Float = 0f

    init {
        LayoutInflater.from(context).inflate(R.layout.item_app_button, this, true)
        iconContainer = findViewById(R.id.icon_container)
        iconView = findViewById(R.id.app_icon)
        appNameView = findViewById(R.id.app_name)
        addIconView = findViewById(R.id.add_icon)

        isFocusable = true
        isClickable = true
        descendantFocusability = FOCUS_BLOCK_DESCENDANTS
        setWillNotDraw(false)
    }

    /** Cached half-width/-height, kept in sync via [onSizeChanged] to avoid recalculation in [onDraw]. */
    private var tileCx: Float = 0f
    private var tileCy: Float = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        tileCx = w / 2f
        tileCy = h / 2f
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        glowOverflowPx = resources.getDimension(R.dimen.grid_glow_overflow)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        currentAnimator?.cancel()
        currentAnimator = null
    }

    fun bindApp(appInfo: AppInfo, showName: Boolean, shape: ButtonShape) {
        dominantColor = appInfo.dominantColor
        isEmptySlot = false
        iconView.setImageDrawable(appInfo.icon)
        iconView.visibility = VISIBLE
        addIconView.visibility = GONE
        appNameView.text = appInfo.label
        appNameView.visibility = if (showName) VISIBLE else GONE
        isFocusable = true
        isClickable = true
        isEnabled = true
        alpha = 1f
        applyShape(shape, false)
    }

    fun bindEmpty(shape: ButtonShape) {
        dominantColor = Color.WHITE
        isEmptySlot = true
        iconView.setImageDrawable(null)
        iconView.visibility = GONE
        addIconView.visibility = GONE
        appNameView.visibility = GONE
        isFocusable = false
        isClickable = false
        isEnabled = false
        alpha = 0f
        applyShape(shape, true)
    }

    companion object {
        /** Thin, subtle stroke shown on unfocused tiles – not bright. */
        private const val STROKE_WIDTH_UNFOCUSED = 2
        /** Maximum icon-container scale when the tile is fully focused. */
        private const val MAX_ICON_SCALE = 1.05f
        private const val ANIM_DURATION_MS = 150L
    }

    override fun onDraw(canvas: Canvas) {
        if (!isEmptySlot && glowProgress > 0f && width > 0 && height > 0) {
            val overflow = glowOverflowPx * glowProgress
            // Distance from the gradient center to the corner of the expanded glow rect.
            val expandedHalfW = tileCx + overflow
            val expandedHalfH = tileCy + overflow
            val radius = sqrt(expandedHalfW * expandedHalfW + expandedHalfH * expandedHalfH)
            val innerGlowAlpha = (160 * glowProgress).toInt()
            val outerGlowAlpha = (70 * glowProgress).toInt()
            val shader = RadialGradient(
                tileCx, tileCy, radius,
                intArrayOf(
                    ColorUtils.withAlpha(dominantColor, innerGlowAlpha),
                    ColorUtils.withAlpha(dominantColor, outerGlowAlpha),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0.15f, 0.55f, 1.0f),
                Shader.TileMode.CLAMP
            )
            glowPaint.shader = shader
            // Draw the glow rect, extending beyond the tile's own bounds by `overflow`.
            // Visible only because the parent RecyclerView has clipChildren = false.
            canvas.drawRect(-overflow, -overflow, width + overflow, height + overflow, glowPaint)
        }
        super.onDraw(canvas)
    }

    private fun applyShape(shape: ButtonShape, isEmpty: Boolean) {
        val background = GradientDrawable()
        val fillColor = when {
            isEmpty -> Color.argb(40, 255, 255, 255)
            isFocused -> ColorUtils.withAlpha(lightenColor(dominantColor, 30), 110)
            // Increased alpha (70 vs 40) so the app's colour fills more of the tile area.
            else -> ColorUtils.withAlpha(dominantColor, 70)
        }
        // When focused: no stroke – the radial glow provides the visual boundary.
        // When unfocused: very subtle stroke (muted, low-alpha) rather than the full accent.
        val strokeColor = when {
            isEmpty -> Color.argb(80, 255, 255, 255)
            isFocused -> Color.TRANSPARENT
            else -> ColorUtils.withAlpha(dominantColor, 55)
        }
        val strokeWidth = if (isFocused || isEmpty) 0 else STROKE_WIDTH_UNFOCUSED

        background.setColor(fillColor)
        background.setStroke(strokeWidth, strokeColor)

        when (shape) {
            ButtonShape.CIRCLE -> background.shape = GradientDrawable.OVAL
            ButtonShape.ROUNDED_SQUARE -> {
                background.shape = GradientDrawable.RECTANGLE
                background.cornerRadius = 24f * resources.displayMetrics.density
            }
            ButtonShape.SQUARE -> {
                background.shape = GradientDrawable.RECTANGLE
                background.cornerRadius = 0f
            }
        }

        iconContainer.background = background
        iconContainer.elevation = (if (isFocused) 24f else 4f) * resources.displayMetrics.density

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val shadowColor = if (isFocused && !isEmpty) dominantColor else Color.BLACK
            iconContainer.outlineAmbientShadowColor = shadowColor
            iconContainer.outlineSpotShadowColor = shadowColor
        }
    }

    private fun lightenColor(color: Int, amount: Int): Int {
        val r = (Color.red(color) + amount).coerceAtMost(255)
        val g = (Color.green(color) + amount).coerceAtMost(255)
        val b = (Color.blue(color) + amount).coerceAtMost(255)
        return Color.argb(255, r, g, b)
    }

    fun setFocusedState(focused: Boolean, shape: ButtonShape) {
        isFocused = focused
        val targetProgress = if (focused) 1f else 0f

        currentAnimator?.cancel()
        currentAnimator = ValueAnimator.ofFloat(glowProgress, targetProgress).apply {
            duration = ANIM_DURATION_MS
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                glowProgress = anim.animatedValue as Float
                // Scale icon container proportionally together with the glow.
                val scale = 1f + glowProgress * (MAX_ICON_SCALE - 1f)
                iconContainer.scaleX = scale
                iconContainer.scaleY = scale
                invalidate()
            }
            start()
        }

        applyShape(shape, isEmptySlot)
    }
}

