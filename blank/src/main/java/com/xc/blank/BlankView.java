package com.xc.blank;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;

import static com.xc.blank.Status.CORRECTED;
import static com.xc.blank.Status.ERROR;

public class BlankView extends View {

    private static final String TAG = "BlankView";
    private static final String PARAGRAPHTIPS = "\n";
    private static final String BLOCKTIPS = "_____";
    private Paint paint;
    private Paint linePaint;
    private float lineWidth;
    private int width;
    private float lineHeight;
    private float lineSpace;
    private float paragraphSpace;//段落间距
    private float lineDetaY;//横线的高度
    private Paint.FontMetrics fontMetrics;

    private boolean isCheckOptions;//选了某个选项选过之后是否不可选了，是否可选条件根据选项大于空格

    private List<TextInfo> textInfos = new ArrayList<>();
    private List<LineInfo> lineInfos = new ArrayList<>();//下划线信息
    private List<FillAnswerInfo> fillAnswerInfos = new ArrayList<>();
    private List<ChoiceOptions> answerResult = new ArrayList<>();
    private List<TextInfo> answers = new ArrayList<>();//填充的答案
    private List<List<LineInfo>> allLineInfos = new ArrayList<>();//主要是用来标记是一组画横线的内容
    List<LineInfo> selectLineInfos = null;//用来标识已经被选中的横线

    private int lineIndex = 0;
    private float totalHeight = 0;//总高度
    private float originHeight = 0;//原来的总高度
    private String orginText;
    private boolean isParsing;//是否是解析类型的

    public BlankView(Context context) {
        this(context, null);
    }

    public BlankView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlankView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#333333"));
        paint.setTextSize(getResources().getDimension(R.dimen.sp_15));

        lineSpace = getResources().getDimension(R.dimen.dp_5);
        paragraphSpace = getResources().getDimension(R.dimen.dp_8);
        lineDetaY = getResources().getDimension(R.dimen.dp_0_5);
        lineWidth = getResources().getDimension(R.dimen.dp_75);
        fontMetrics = paint.getFontMetrics();
        lineHeight = fontMetrics.descent - fontMetrics.ascent;

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#333333"));
        linePaint.setStrokeWidth(lineDetaY);
        linePaint.setStyle(Paint.Style.FILL);
    }

    private void setText() {
        String[] split = orginText.split(PARAGRAPHTIPS);//分段落
        boolean isOnlyInvalidate = caculateLines(split);
        Log.d("setText", "isOnlyInvalidate:" + isOnlyInvalidate);
        if (isOnlyInvalidate) {
            invalidate();
        } else {
            requestLayout();
        }
    }

    /**
     * 解析的话调用该方法
     */
    public void setText(ChoiceBlankBean choiceBlankBean) {
        if (choiceBlankBean == null) {
            return;
        }
        orginText = choiceBlankBean.getQuestionContent();
        List<ChoiceOptions> stuAnswer = choiceBlankBean.getStuAnswer();
        List<ChoiceOptions> choiceOptions = choiceBlankBean.getChoiceOptions();
        int count = 0;
        initParsing(stuAnswer);
        String[] split = initFillAnswerInfos(choiceOptions, count);
        boolean isOnlyInvalidate = caculateLines(split);
        if (isOnlyInvalidate) {
            invalidate();
        } else {
            requestLayout();
        }
    }

    /**
     * 初始化空格的默认答案
     */
    @NotNull
    private String[] initFillAnswerInfos(List<ChoiceOptions> choiceOptions, int count) {
        String[] split = orginText.split(PARAGRAPHTIPS);//分段落
        for (String s : split) {
            String[] s1 = s.split(BLOCKTIPS);
            for (int i = 0; i < s1.length; i++) {
                //不是解析的时候才会去添加默认横线信息
                if (!isParsing && (i < s1.length - 1 || (i == s1.length - 1 && orginText.endsWith(BLOCKTIPS)))) {
                    FillAnswerInfo fillAnswerInfo = new FillAnswerInfo();
                    fillAnswerInfo.lineWidth = lineWidth;
                    fillAnswerInfo.lineText = "";
                    fillAnswerInfos.add(fillAnswerInfo);
                    answerResult.add(new ChoiceOptions());
                }
                count++;
            }
        }
        if (count <= choiceOptions.size()) {
            isCheckOptions = true;
        }
        return split;
    }

    /**
     * 初始化解析
     */
    private void initParsing(List<ChoiceOptions> stuAnswer) {
        if (stuAnswer != null && stuAnswer.size() > 0) {
            //解析学生的答案
            for (int i = 0; i < stuAnswer.size(); i++) {
                ChoiceOptions choiceOption = stuAnswer.get(i);
                FillAnswerInfo fillAnswerInfo = new FillAnswerInfo();
                fillAnswerInfo.lineWidth = paint.measureText(choiceOption.getOptValue());
                fillAnswerInfo.lineText = choiceOption.getOptValue();
                int answer = choiceOption.getAnswer();
                if (answer == 1) {
                    fillAnswerInfo.status = CORRECTED;
                } else if (answer == 0) {
                    fillAnswerInfo.status = ERROR;
                }
                fillAnswerInfos.add(fillAnswerInfo);
            }
            isParsing = true;

        }
    }

    /**
     * 计算行、空格、下划线、下划线答案信息
     * 返回值:是否只是绘制操作
     */
    private boolean caculateLines(String[] split) {
        lineInfos.clear();
        textInfos.clear();
        answers.clear();
        allLineInfos.clear();
        lineIndex = 0;
        float top = -fontMetrics.ascent;
        for (String s : split) {
            float hasWidth = width;
            float left = 0;
            String[] s1 = s.split(BLOCKTIPS);
            Log.d("BlankView", "s1.size:" + s1.length);
            for (int i = 0; i < s1.length; i++) {
                String s2 = s1[i];
                float v = paint.measureText(s2);
                if (v <= hasWidth) {//没超过剩余的宽度
                    addBlockText(top, left, s2);
                    hasWidth -= v;
                    left += v;
                } else {
                    //超过了剩余宽度，需要进行截取
                    int lineCount = 0;
                    while (s2.length() > 0) {
                        if (lineCount == 0) {
                            int hasWordCount = paint.breakText(s2, true, hasWidth, null);
                            String substring = s2.substring(0, hasWordCount);
                            addBlockText(top, left, substring);
                            top += lineHeight + lineSpace;
                            left = 0;
                            hasWidth = width;
                            s2 = s2.substring(hasWordCount);
                        } else {
                            int hasWordCount = paint.breakText(s2, true, width, null);
                            if (hasWordCount == s2.length()) {
                                float v1 = paint.measureText(s2);
                                //说明不够一行
                                String substring = s2.substring(0, hasWordCount);
                                addBlockText(top, left, substring);
                                left = v1;
                                hasWidth -= v1;
                                s2 = s2.substring(hasWordCount);
                            } else if (hasWordCount > 0 && hasWordCount < s2.length()) {
                                String substring = s2.substring(0, hasWordCount);
                                addBlockText(top, left, substring);
                                top += lineHeight + lineSpace;
                                left = 0;
                                hasWidth = width;
                                s2 = s2.substring(hasWordCount);
                            }

                        }
                        lineCount++;
                    }
                }

                //--------------计算横线的信息
                if (i < s1.length - 1 || (i == s1.length - 1 && orginText.endsWith(BLOCKTIPS))) {
                    FillAnswerInfo fillAnswerInfo = fillAnswerInfos.get(lineIndex);
                    String lineText = fillAnswerInfo.lineText;
                    Float currentLineWidth = fillAnswerInfo.lineWidth;//拿到当前自己的横线长度
                    Status status = fillAnswerInfo.status;
                    if (currentLineWidth <= hasWidth) {//没超过剩余的宽度
                        List<LineInfo> relatedLineInfos = new ArrayList<>();
                        LineInfo textInfo = addBlockUnderLine(top, left, currentLineWidth, status);
                        //添加横线的文字
                        addUnderLineText(top, left, lineText, status);
                        relatedLineInfos.add(textInfo);
                        hasWidth -= currentLineWidth;
                        left += currentLineWidth;
                        allLineInfos.add(relatedLineInfos);
                    } else {

                        //超过了剩余宽度，需要进行截取
                        int lineCount = 0;
                        float lineW = currentLineWidth;
                        List<LineInfo> relatedLineInfos = new ArrayList<>();
                        String hasLineText = null;
                        while (lineW > 0) {
                            if (lineCount == 0) {
                                lineW -= hasWidth;
                                LineInfo textInfo = new LineInfo();
                                textInfo.start = left;
                                textInfo.end = width;
                                textInfo.lineTop = top + fontMetrics.descent + lineDetaY;
                                textInfo.index = lineIndex;
                                textInfo.status = status;
                                lineInfos.add(textInfo);
                                //LineInfo textInfo = addBlockUnderLine(top, left, (float) width-left, view);
                                //添加横线到右边距离问题
                                if (!TextUtils.isEmpty(lineText)) {
                                    int word = paint.breakText(lineText, true, hasWidth, null);
                                    String text = lineText.substring(0, word);
                                    float textWidth = paint.measureText(text);
                                    TextInfo textInfo1 = new TextInfo();
                                    textInfo1.text = text;
                                    textInfo1.left = left;
                                    textInfo1.top = top;
                                    textInfo1.status = status;
                                    //修正横线的问题
                                    textInfo.end = left + textWidth;
                                    lineW += hasWidth - textWidth;
                                    hasLineText = lineText.substring(word);
                                    answers.add(textInfo1);
                                }
                                relatedLineInfos.add(textInfo);
                                top += lineHeight + lineSpace;
                                left = 0;
                                hasWidth = width;
                            } else {
                                float deta = lineW - width;
                                if (deta < 0) {
                                    //说明不够一行
                                    LineInfo textInfo = addBlockUnderLine(top, left, left + lineW, status);
                                    hasLineText = addHashUnderLineText(top, left, lineW, hasLineText, status);
                                    relatedLineInfos.add(textInfo);
                                    left = left + lineW;
                                    hasWidth -= lineW;
                                } else {
                                    LineInfo textInfo = addBlockUnderLine(top, left, (float) width, status);
                                    hasLineText = addHashUnderLineText(top, left, width, hasLineText, status);
                                    relatedLineInfos.add(textInfo);
                                    top += lineHeight + lineSpace;
                                    left = 0;
                                    hasWidth = width;
                                }
                                lineW -= width;

                            }
                            lineCount++;

                        }
                        allLineInfos.add(relatedLineInfos);
                    }
                    lineIndex++;
                }
            }
            top += lineHeight + lineSpace + paragraphSpace;
        }
        //最后一行有横线的时候
        //int size = allLineInfos.get(allLineInfos.size() - 1).size();
        float lineTop = lineInfos.get(lineInfos.size() - 1).lineTop;
        float textTop = textInfos.get(textInfos.size() - 1).top;
        if (orginText.endsWith(BLOCKTIPS) || lineTop > textTop) {//加上最后一个横线的高度
            totalHeight = top - paragraphSpace - lineSpace - lineHeight + fontMetrics.descent + 2 * lineDetaY;
        } else {
            totalHeight = top - paragraphSpace - lineSpace - lineHeight + fontMetrics.descent;
        }

        //originHeight = Log.d(TAG, "currentLineCount:" + currentLineCount);
        Log.d(TAG, "originCount:" + originHeight);
        Log.d(TAG, "totalHeight:" + totalHeight);
        if (totalHeight == originHeight) {//如果原来的高度和现在不一样则需要onMeasure
            return true;
        }
        originHeight = totalHeight;
        return false;
    }

    @NotNull
    private LineInfo addBlockUnderLine(float top, float left, Float currentLineWidth, Status status) {
        LineInfo textInfo = new LineInfo();
        textInfo.start = left;
        textInfo.end = left + currentLineWidth;
        textInfo.lineTop = top + fontMetrics.descent + lineDetaY;
        textInfo.index = lineIndex;
        //textInfo.view = view;
        textInfo.status = status;
        lineInfos.add(textInfo);
        return textInfo;
    }

    /**
     * 添加块信息的text
     */
    private void addBlockText(float top, float left, String s2) {
        TextInfo textInfo = new TextInfo();
        textInfo.left = left;
        textInfo.top = top;
        textInfo.text = s2;
        textInfos.add(textInfo);
    }

    /**
     * 添加不够一行的答案内容
     */
    @org.jetbrains.annotations.Nullable
    private String addHashUnderLineText(float top, float left, float lineW, String hasLineText, Status status) {
        if (!TextUtils.isEmpty(hasLineText)) {
            int word = paint.breakText(hasLineText, true, lineW, null);
            TextInfo textInfo1 = new TextInfo();
            textInfo1.text = hasLineText.substring(0, word);
            textInfo1.left = left;
            textInfo1.top = top;
            textInfo1.status = status;
            hasLineText = hasLineText.substring(word);
            answers.add(textInfo1);
        }
        return hasLineText;
    }

    /**
     * 添加够一行的答案内容
     */
    private void addUnderLineText(float top, float left, String lineText, Status status) {
        if (!TextUtils.isEmpty(lineText)) {
            TextInfo textInfo1 = new TextInfo();
            textInfo1.text = lineText;
            textInfo1.left = left;
            textInfo1.top = top;
            textInfo1.status = status;
            answers.add(textInfo1);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        Log.d("onSizeChanged", "width:" + width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (TextInfo textInfo : textInfos) {
            paint.setColor(Color.parseColor("#333333"));
            canvas.drawText(textInfo.text, textInfo.left, textInfo.top, paint);
        }
        //绘制横线
        for (LineInfo lineInfo : lineInfos) {
            if (!isParsing) {
                if (lineInfo.isSelect) {
                    linePaint.setColor(Color.parseColor("#52BCFF"));
                } else {
                    linePaint.setColor(Color.parseColor("#333333"));
                }
            } else {
                if (lineInfo.status == ERROR) {
                    linePaint.setColor(Color.parseColor("#FF7C64"));
                } else if (lineInfo.status == CORRECTED) {
                    linePaint.setColor(Color.parseColor("#06D265"));
                }
            }
            canvas.drawLine(lineInfo.start, lineInfo.lineTop, lineInfo.end, lineInfo.lineTop, linePaint);
        }
        //绘制用户的答案
        for (TextInfo answer : answers) {
            if (isParsing) {
                if (answer.status == ERROR) {
                    paint.setColor(Color.parseColor("#FF7C64"));
                } else if (answer.status == CORRECTED) {
                    paint.setColor(Color.parseColor("#06D265"));
                }
            }
            canvas.drawText(answer.text, answer.left, answer.top, paint);
        }
        //requestLayout();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //找出当前按下的时候那个点
        if (isParsing) {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();
            LineInfo lineInfo = checkSelectInfo(x, y);
            if (lineInfo != null) {
                //如果上面有内容，则重置成横线内容
                FillAnswerInfo fillAnswerInfo = fillAnswerInfos.get(lineInfo.index);
                if (!TextUtils.isEmpty(fillAnswerInfo.lineText)) {
                    fillAnswerInfo.lineWidth = lineWidth;
                    fillAnswerInfo.lineText = "";
                    answerResult.set(lineInfo.index, new ChoiceOptions());
                    if (onStatusChange != null) {
                        onStatusChange.onReduce(fillAnswerInfo.view);
                    }
                    setText();
                    return true;
                }
                drawUnderLine(lineInfo);
            } else {
                Log.d("onTouchEvent", "lineInfo is null");
            }
        }
        return true;
    }

    /**
     * 绘制横线
     */
    private void drawUnderLine(LineInfo lineInfo) {
        //重置已经被选中过了的
        if (selectLineInfos != null) {
            for (LineInfo selectLineInfo : selectLineInfos) {
                selectLineInfo.isSelect = false;
            }
            selectLineInfos = null;
        }
        Log.d("drawUnderLine", "allLineInfos:" + allLineInfos.size());
        for (List<LineInfo> allLineInfo : allLineInfos) {
            for (LineInfo info : allLineInfo) {
                if (info == lineInfo) {
                    selectLineInfos = allLineInfo;
                    break;
                }
            }
        }
        //如果之前的已经有被选中的，则取消之前被选中的
        if (selectLineInfos != null) {
            for (LineInfo selectLineInfo : selectLineInfos) {
                selectLineInfo.isSelect = true;
            }
            invalidate();
        }
    }

    /**
     * 检查是否选中了下划线
     */
    private LineInfo checkSelectInfo(float x, float y) {
        LineInfo selectLineInfo = null;
        for (LineInfo lineInfo : lineInfos) {
            if (x >= lineInfo.start
                    && x <= lineInfo.end
                    && y >= lineInfo.lineTop - lineDetaY - lineHeight
                    && y <= lineInfo.lineTop) {
                selectLineInfo = lineInfo;
                break;
            }
        }
        return selectLineInfo;
    }

    /**
     * 填充答案
     */
    public boolean fillAnswer(ChoiceOptions choiceOptions, View view) {
        //直接给已经选中过的下划线处加文字
        if (selectLineInfos != null && selectLineInfos.size() > 0) {
            float answerWidth = paint.measureText(choiceOptions.getOptValue());//测量出答案的长度
            FillAnswerInfo fillAnswerInfo = fillAnswerInfos.get(selectLineInfos.get(0).index);
            fillAnswerInfo.lineWidth = answerWidth;
            fillAnswerInfo.lineText = choiceOptions.getOptValue();
            fillAnswerInfo.view = view;
            answerResult.set(selectLineInfos.get(0).index, choiceOptions);
            selectLineInfos = null;
            setText();
            return isCheckOptions;
        } else {
            //按照顺序进行填充答案
            for (int i = 0; i < fillAnswerInfos.size(); i++) {
                FillAnswerInfo fillAnswerInfo = fillAnswerInfos.get(i);
                if (TextUtils.isEmpty(fillAnswerInfo.lineText)) {
                    float answerWidth = paint.measureText(choiceOptions.getOptValue());//测量出答案的长度
                    fillAnswerInfo.lineWidth = answerWidth;
                    fillAnswerInfo.lineText = choiceOptions.getOptValue();
                    fillAnswerInfo.view = view;
                    answerResult.set(i, choiceOptions);
                    setText();
                    return isCheckOptions;
                }
            }
            return false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width, height;
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        //宽度的测量
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            StringBuilder sb = new StringBuilder();
            for (TextInfo textInfo : textInfos) {
                sb.append(textInfo.text);
            }
            for (TextInfo textInfo : answers) {
                sb.append(textInfo.text);
            }
            if (TextUtils.isEmpty(sb.toString())) {
                width = 0;
            } else {
                width = (int) Math.min(widthSize, paint.measureText(sb.toString()) + paddingLeft + paddingRight);
            }
        }
        //高度的测量
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = (int) totalHeight + paddingTop + paddingBottom;
        }
        setMeasuredDimension(width, height);
    }

    public List<ChoiceOptions> getAnswerResult() {
        return answerResult;
    }

    public OnStatusChange onStatusChange;

    public void setOnStatusChange(OnStatusChange onStatusChange) {
        this.onStatusChange = onStatusChange;
    }

    public interface OnStatusChange {

        void onReduce(View view);
    }
}
