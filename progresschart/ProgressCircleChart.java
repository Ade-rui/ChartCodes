package com.ryrj.testgit.mychart.progresschart;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.ryrj.testgit.R;
import com.ryrj.testgit.utils.DensityUtil;
import com.ryrj.testgit.utils.FontUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wrf on 2018/9/20.
 */

public class ProgressCircleChart extends View {

    //内容区域
    private RectF rectContent;
    //图标和文字的画笔
    private Paint paintChart,paintText;
    //中心点
    private PointF centerPoint;
    //圆环颜色
    private int colorRing;
    //圆环宽度
    private int ringWidth;
    //圆环默认背景色
    private int colorRingDef;
    //背景颜色
    private int backColor;
    //开始角度
    private float startAngle = -90;
    //占用角度
    private float sweepAngle;
    //中间的label
    private List<ChartLabel> labelList = new ArrayList<>();
    //占用百分比
    private float per;
    //是否开始绘制
    private boolean startDraw;
    //动画百分比
    private float animPro;
    //是否需要动画
    private boolean startAnimator;
    //圆环半径
    private float chartRaduis;
    //动画时间
    private long animDuration;
    //label全高度
    private int allHeight;
    //中间label的间距
    private float labelSpace = DensityUtil.dip2px(getContext(),4);

    public ProgressCircleChart(Context context) {
        super(context);
        init();
    }

    public ProgressCircleChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ProgressCircleChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(){
        paintChart = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    private void init(AttributeSet attrs){
        //自定义属性
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ProgressCircleChart);
        colorRing = typedArray.getColor(R.styleable.ProgressCircleChart_colorRing, Color.YELLOW);
        ringWidth = (int) typedArray.getDimension(R.styleable.ProgressCircleChart_ringWidth,DensityUtil.dip2px(getContext(),10));
        colorRingDef = typedArray.getColor(R.styleable.ProgressCircleChart_colorRingDef,Color.LTGRAY);
        backColor = typedArray.getColor(R.styleable.ProgressCircleChart_android_background,Color.WHITE);
        startAnimator = typedArray.getBoolean(R.styleable.ProgressCircleChart_startAnimator,true);
        animDuration = typedArray.getInt(R.styleable.ProgressCircleChart_animDuration,2000);
        labelSpace = typedArray.getDimension(R.styleable.ProgressCircleChart_labelSpace,DensityUtil.dip2px(getContext(),4));
        typedArray.recycle();
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int resultWidth = widthSize;
        int resultHeight = heightSize;

        if(widthMode == MeasureSpec.AT_MOST){
            resultWidth = widthSize * 2/3;
        }
        if(heightMode == MeasureSpec.AT_MOST){
            resultHeight = heightSize * 2/3;
        }

        setMeasuredDimension(resultWidth,resultHeight);

        initAfterMeasure();
    }

    private void initAfterMeasure() {
        rectContent = new RectF(getPaddingLeft(),getPaddingTop(),getMeasuredWidth() - getPaddingRight(),getMeasuredHeight() - getPaddingBottom());
        centerPoint = new PointF(rectContent.right/2,rectContent.bottom/2);
        sweepAngle = per * 360f;
        chartRaduis = rectContent.right < rectContent.bottom ? rectContent.right : rectContent.bottom;
        chartRaduis = chartRaduis/2;
        ringWidth = (int) (chartRaduis * 1 / 5);

        allHeight = 0;

        for (int i = 0; i < labelList.size(); i++) {
            ChartLabel chartLabel = labelList.get(i);
            paintText.setTextSize(chartLabel.getTextSize());
            float fontHeight = FontUtil.getFontHeight(paintText);
            allHeight += (fontHeight + labelSpace);
        }
        allHeight -= labelSpace;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制背景
        drawDefualt(canvas);

        //绘制图标
        if(!startDraw && startAnimator){
            startDraw = true;
            startAnimation();
        }else {
            drawChart(canvas);
        }
    }

    private void drawChart(Canvas canvas) {
        if(per == 0){
            drawCenterText(canvas);
            return;
        }
        if(!startAnimator){
            animPro = 1.0f;
        }
        RectF rectF = new RectF(centerPoint.x - chartRaduis, centerPoint.y - chartRaduis, centerPoint.x + chartRaduis, centerPoint.y + chartRaduis);
        paintChart.setColor(colorRing);
        canvas.drawArc(rectF,startAngle,sweepAngle*animPro,true,paintChart);
        paintChart.setColor(backColor);
        canvas.drawCircle(centerPoint.x,centerPoint.y,chartRaduis - ringWidth,paintChart);

        if(labelList == null || labelList.size() == 0){
            return;
        }

        drawCenterText(canvas);
    }

    private void drawCenterText(Canvas canvas) {
        float top = centerPoint.y - allHeight/2;
        for (int i = 0; i < labelList.size(); i++) {
            ChartLabel chartLabel = labelList.get(i);
            paintText.setColor(chartLabel.getTextColor());
            paintText.setTextSize(chartLabel.getTextSize());
            paintText.setTextAlign(Paint.Align.CENTER);
            float textHeight = FontUtil.getFontHeight(paintText);
            top += i * (textHeight + labelSpace);
            canvas.drawText(chartLabel.getText(),centerPoint.x,top + Math.abs(paintText.getFontMetrics().top),paintText);
        }
    }

    private void startAnimation(){
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animPro = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.setDuration(animDuration);
        valueAnimator.start();
    }

    private void drawDefualt(Canvas canvas) {
        paintChart.setColor(colorRingDef);
        canvas.drawCircle(centerPoint.x,centerPoint.y,chartRaduis,paintChart);
        paintChart.setColor(backColor);
        canvas.drawCircle(centerPoint.x,centerPoint.y,chartRaduis - ringWidth,paintChart);
    }

    public int getColorRing() {
        return colorRing;
    }

    public void setColorRing(int colorRing) {
        this.colorRing = colorRing;
    }

    public int getRingWidth() {
        return ringWidth;
    }

    public void setRingWidth(int ringWidth) {
        this.ringWidth = ringWidth;
    }

    public int getColorRingDef() {
        return colorRingDef;
    }

    public void setColorRingDef(int colorRingDef) {
        this.colorRingDef = colorRingDef;
    }

    public int getBackColor() {
        return backColor;
    }

    public void setBackColor(int backColor) {
        this.backColor = backColor;
    }

    public List<ChartLabel> getLabelList() {
        return labelList;
    }

    public void setLabelList(List<ChartLabel> labelList) {
        this.labelList = labelList;
    }

    public float getPer() {
        return per;
    }

    public void setPer(float per) {
        this.per = per;
    }

    public float getAnimPro() {
        return animPro;
    }

    public void setAnimPro(float animPro) {
        this.animPro = animPro;
    }

    public boolean isStartAnimator() {
        return startAnimator;
    }

    public void setStartAnimator(boolean startAnimator) {
        this.startAnimator = startAnimator;
    }

    public float getLabelSpace() {
        return labelSpace;
    }

    public void setLabelSpace(float labelSpace) {
        this.labelSpace = labelSpace;
    }
}
