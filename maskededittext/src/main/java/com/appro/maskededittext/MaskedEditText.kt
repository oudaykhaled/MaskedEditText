package com.appro.maskededittext

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes

import java.util.ArrayList

import android.text.InputType.TYPE_CLASS_NUMBER

class MaskedEditText : FrameLayout, TextWatcher {

    @LayoutRes
    private var charTextView: Int = 0
    private var pattern: String? = ""
    private val lstEditTexts = ArrayList<TextView>()
    private var onFieldsChangedListener: OnFieldsChangedListener? = null
    private var container: LinearLayout? = null
    private var etHidden: EditText? = null
    private var oldInput = ""


    private val value: String
        @Synchronized get() {
            var value = pattern!! + ""
            value = value.replace('^', '#')
            for (i in lstEditTexts.indices) {
                value = value.replaceFirst("#".toRegex(), lstEditTexts[i].text.toString())
            }
            return value
        }


    constructor(context: Context) : super(context) {
        init(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr, 0)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val view = View.inflate(getContext(), R.layout.masked_edit_text, this)
        this.container = view.findViewById(R.id.container)
        this.etHidden = view.findViewById(R.id.etHidden)
        val a = context.obtainStyledAttributes(attrs, R.styleable.MaskedEditText, defStyleAttr, 0)
        this.charTextView =
            a.getResourceId(R.styleable.MaskedEditText_CharTextView, R.layout.masked_char_text_view)
        this.pattern = a.getString(R.styleable.MaskedEditText_pattern)
        buildViews()
        etHidden!!.addTextChangedListener(this)
        etHidden!!.requestFocus()
        etHidden!!.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(0))
    }

    fun setPattern(pattern: String) {
        etHidden!!.removeTextChangedListener(this)
        etHidden!!.setText("")
        oldInput = ""
        this.pattern = pattern
        lstEditTexts.clear()
        buildViews()
        etHidden!!.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(lstEditTexts.size))
        etHidden!!.addTextChangedListener(this)
    }

    private fun buildViews() {
        container!!.removeAllViews()
        if (pattern == null || pattern!!.isEmpty()) return
        val chars = pattern!!.toCharArray()
        var buffer = StringBuilder()
        var lastChar: Char? = null
        for (i in chars.indices) {
            val c = chars[i]
            if (i == chars.size - 1) { // Last Chars
                buffer = buildViewsForLastChar(buffer, c)
            } else if (lastChar == null) { // first char | Note that case where pattern contains only char, this will be covered in the first condition
                buffer.append(c)
            } else {
                buffer = buildViewsForNonLastChar(buffer, lastChar, c)
            }
            lastChar = c
        }
    }

    private fun buildViewsForNonLastChar(
        buffer: StringBuilder,
        lastChar: Char?,
        c: Char
    ): StringBuilder {
        var buffer = buffer
        if (areTheSameCategory(
                c,
                lastChar
            )
        ) { // either both last char and c are special char or non-special char
            buffer.append(c)
        } else {
            if (isSpecialChar(c)) {
                dropTextView(buffer.toString())
                dropEditText(c + "")
            } else {
                dropEditText(buffer.toString())
                dropTextView(c + "")
            }
            buffer = StringBuilder()
        }
        return buffer
    }

    private fun buildViewsForLastChar(buffer: StringBuilder, c: Char): StringBuilder {
        var buffer = buffer
        if (isSpecialChar(c) && (buffer.toString().isEmpty() || isSpecialChar(buffer.toString()[buffer.length - 1]))) {
            // both last char and 'c' are special characters
            buffer.append(c)
            dropEditText(buffer.toString())
            buffer = StringBuilder()
        } else if (!isSpecialChar(c) && (buffer.toString().isEmpty() || !isSpecialChar(buffer.toString()[buffer.length - 1]))) {
            //both last char and 'c' are non-special characters
            buffer.append(c)
            dropTextView(buffer.toString())
            buffer = StringBuilder()
        } else {
            //last char and c are different | We are sure buffer is not empty
            if (isSpecialChar(c)) {
                dropTextView(buffer.toString())
                dropEditText(c + "")
            } else {
                dropEditText(buffer.toString())
                dropTextView(c + "")
            }
            buffer = StringBuilder()
        }
        return buffer
    }

    private fun areTheSameCategory(c: Char, lastChar: Char?): Boolean {
        return isSpecialChar(c) == isSpecialChar(lastChar!!)
    }

    private fun dropTextView(str: String?) {
        if (str == null || str.isEmpty()) return
        val editText = View.inflate(context, charTextView, null) as EditText
        editText.setText(str)
        editText.setText(str)
        editText.isEnabled = false
        editText.isFocusable = false
        container!!.addView(editText)
    }

    private fun dropEditText(string: String) {
        for (c in string.toCharArray()) {
            if (c == '#')
                dropNumericEditText(c)
            else
                dropAlphaNumericEditText(c)
        }
    }

    private fun dropNumericEditText(c: Char) {
        val editText = getEditText(c.toString())
        editText.inputType = TYPE_CLASS_NUMBER
        container!!.addView(editText)
        lstEditTexts.add(editText)
    }

    private fun dropAlphaNumericEditText(c: Char) {
        val editText = getEditText(c.toString())
        container!!.addView(editText)
        lstEditTexts.add(editText)
    }

    private fun getEditText(str: String): TextView {
        val editText = View.inflate(context, charTextView, null) as EditText
        editText.setText("-")
        return editText
    }

    private fun triggerOnFieldsChangeListener() {
        if (onFieldsChangedListener == null) return
        val value = value
        var isDone = true
        for (field in lstEditTexts) {
            val valueInput = field.text.toString()
            if (valueInput.isEmpty() || valueInput.equals(
                    "-",
                    ignoreCase = true
                ) || valueInput.equals(" ", ignoreCase = true)
            ) {
                isDone = false
                break
            }
        }
        onFieldsChangedListener!!.onFieldsChanged(this, value, isDone)
    }

    private fun isSpecialChar(c: Char): Boolean {
        return c == '#' || c == '^' // # for numbers and ^ for alphanumeric
    }

    fun setOnFieldsChangedListener(onFieldsChangedListener: OnFieldsChangedListener): MaskedEditText {
        this.onFieldsChangedListener = onFieldsChangedListener
        return this
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable) {
        val newStr = s.toString()
        if (newStr.length >= oldInput.length) {
            val lastChar = newStr[newStr.length - 1]
            lstEditTexts[newStr.length - 1 % lstEditTexts.size].text = lastChar + ""
        } else {
            lstEditTexts[newStr.length % lstEditTexts.size].text = "-"
        }
        oldInput = newStr
        triggerOnFieldsChangeListener()
    }

    interface OnFieldsChangedListener {
        fun onFieldsChanged(maskedEditText: MaskedEditText, value: String, isDone: Boolean)
    }

}
