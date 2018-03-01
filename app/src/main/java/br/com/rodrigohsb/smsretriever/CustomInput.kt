package br.com.rodrigohsb.smsretriever

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager

/**
 * Created by lclavelares on 30/08/2017.
 */
class CustomInput : AppCompatEditText {

    private val XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"

    private var mSpace = 16f //24 dp by default, space between the lines
    private var mCharSize: Float = 0F
    private var mNumChars = 4f
    private var mLineSpacing = 8f //8dp by default, height of the text from our lines
    private var mMaxLength = 4
    private var mErrorSpacing = 12f
    private var mErrorSize = 12f
    private var mErrorText = "Código inválido ou expirado"
    private var mErrorPaint: Paint? = null
    private var mShowError: Boolean = true

    private var mClickListener: OnClickListener? = null

    private var mLineStroke = 1f //1dp by default
    private var mLineStrokeSelected = 2f //2dp by default
    private var mLinesPaint: Paint? = null
    private var mStates = arrayOf(intArrayOf(android.R.attr.state_selected), // selected
            intArrayOf(android.R.attr.state_focused), // focused
            intArrayOf(-android.R.attr.state_focused))// unfocused

    private var mColors = intArrayOf(
            ContextCompat.getColor(context, R.color.colorAccent),
            ContextCompat.getColor(context, R.color.colorPrimary),
            ContextCompat.getColor(context, R.color.colorPrimaryDark))

    private var mColorStates = ColorStateList(mStates, mColors)

    constructor(context: Context?) : super(context) {
        init(context!!, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context!!, attrs!!)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context!!, attrs!!)
    }

    fun showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        this.requestFocus()
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
    private fun init(context: Context, attrs: AttributeSet?) {
        val multi = context.resources.displayMetrics.density  //get display density
        mLineStroke *= multi //passing line stroke value to dp
        mLineStrokeSelected *= multi //passing selected line stroke value to dp
        mLinesPaint = Paint(paint)
        mLinesPaint!!.strokeWidth = mLineStroke //set the line stroke value
        setBackgroundResource(0)
        mSpace *= multi //convert to pixels for our density
        mLineSpacing *= multi //convert to pixels for our density
        mMaxLength = attrs!!.getAttributeIntValue(XML_NAMESPACE_ANDROID, "maxLength", 4)
        mNumChars = mMaxLength.toFloat()
        mErrorText = ""
        mErrorPaint = Paint(paint)
        mErrorPaint?.color = attrs.getAttributeResourceValue(XML_NAMESPACE_ANDROID, "errorColor", R.color.abc_tint_default)
        mErrorSize *= multi
        mErrorPaint?.textSize = mErrorSize
        mErrorSpacing *= multi
    }

    override fun setOnClickListener(l: OnClickListener) {
        mClickListener = l
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
        }
        return result
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        setMeasuredDimension(measureDimension(desiredWidth, widthMeasureSpec),
                measureDimension(desiredHeight, heightMeasureSpec) + (mErrorSize + textSize + mErrorSpacing).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        //super.onDraw(canvas);

        val availableWidth = width - paddingRight - paddingLeft
        if (mSpace < 0) {
            mCharSize = availableWidth / (mNumChars * 2 - 1)
        } else {
            mCharSize = (availableWidth - mSpace * (mNumChars - 1)) / mNumChars
        }

        var startX = paddingLeft
        val bottom = height - paddingBottom - mErrorSize - paddingTop

        //Text Width
        val text = text
        val textLength = text.length
        val textWidths = FloatArray(textLength)
        paint.getTextWidths(getText(), 0, textLength, textWidths)

        var i = 0
        while (i < mNumChars) {
            updateColorForLines(i == textLength)

            if (mShowError)
                mLinesPaint?.color = ContextCompat.getColor(context, R.color.abc_background_cache_hint_selector_material_light)

            canvas.drawLine(startX.toFloat(), bottom, startX + mCharSize, bottom, mLinesPaint)

            if (getText().length > i) {
                val middle = startX + mCharSize / 2
                canvas.drawText(text, i, i + 1, middle - textWidths[0] / 2, bottom - mLineSpacing, paint)
            }

            if (mSpace < 0) {
                startX += (mCharSize * 2).toInt()
            } else {
                startX += (mCharSize + mSpace).toInt()
            }
            i++
        }

        if (mShowError) {
            canvas.drawText(mErrorText, (width - ((mErrorSize * (mErrorText.length - 1)) / 2)) / 2, bottom + mErrorSpacing + mErrorSize, mErrorPaint)

        }
    }

    fun showError() {
        mShowError = true
        invalidate()
    }

    fun hideError() {
        mShowError = false
        invalidate()
    }

    private fun getColorForState(vararg states: Int): Int {
        return mColorStates.getColorForState(states, Color.GRAY)
    }

    /**
     * @param next Is the current char the next character to be input?
     */
    private fun updateColorForLines(next: Boolean) {
        if (mShowError)
            return

        if (isFocused) {
            mLinesPaint!!.strokeWidth = mLineStrokeSelected
            mLinesPaint!!.color = getColorForState(android.R.attr.state_focused)
            if (next) {
                mLinesPaint!!.color = getColorForState(android.R.attr.state_selected)
            }
        } else {
            mLinesPaint!!.strokeWidth = mLineStroke
            mLinesPaint!!.color = getColorForState(-android.R.attr.state_focused)
        }
    }
}