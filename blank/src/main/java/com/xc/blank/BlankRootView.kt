package com.xc.blank

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.FrameLayout
import android.widget.TextView
import com.xc.blank.BlankView.OnStatusChange
import kotlinx.android.synthetic.main.blank_root_view.view.blankView
import kotlinx.android.synthetic.main.blank_root_view.view.flow

class BlankRootView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    FrameLayout(context, attrs, defStyleAttr), OnClickListener, OnStatusChange {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        LayoutInflater.from(context).inflate(R.layout.blank_root_view, this)
        blankView.setOnStatusChange(this)
    }

    fun setText(jsonData: ChoiceBlankBean?) {
        if (jsonData?.stuAnswer != null) {
            initTextView(jsonData)
        } else {
            initFlowLayout(jsonData)
            initTextView(jsonData)
        }
    }

    private fun initTextView(jsonData: ChoiceBlankBean?) {
        blankView.setText(jsonData)
    }

    private fun initFlowLayout(jsonData: ChoiceBlankBean?) {
        val lp = MarginLayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
        )
        lp.rightMargin = resources.getDimension(R.dimen.dp_15).toInt()
        lp.topMargin = resources.getDimension(R.dimen.dp_15).toInt()
        jsonData?.choiceOptions?.run {
            forEach {
                addView(lp, it)
            }
        }
    }

    private fun addView(lp: MarginLayoutParams, choiceOptions: ChoiceOptions) {
        val v = View.inflate(context, R.layout.item_task_answer, null)
        val mTvKey = v.findViewById<View>(R.id.mTvKey) as TextView
        mTvKey.setOnClickListener(this)
        mTvKey.text = choiceOptions.optValue
        mTvKey.tag = choiceOptions
        flow.addView(v, lp)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.mTvKey -> {
                //选中流失布局的item
                val key = v.tag as ChoiceOptions?
                key?.run {
                    val isCheckOptions = blankView.fillAnswer(key, v)
                    if (isCheckOptions) {
                        v.isEnabled = false
                    }
                }
            }
        }
    }

    override fun onReduce(v: View?) {
        v?.run {
            v.isEnabled = true
        }
    }

    fun getAnswerResult(): List<ChoiceOptions> {
        return blankView.answerResult
    }

}