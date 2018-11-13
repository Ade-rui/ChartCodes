package com.ryrj.testgit.mychart.piechart;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.ryrj.testgit.utils.DensityUtil;
import com.ryrj.testgit.utils.FontUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wrf on 2018/9/18.
 */

public class PieChart extends View {

    //区域
    private RectF rectContent,rectChart,rectLabel;
    //画笔
    private Paint paintChart,paintText,paintSelected,paintInnerText;
    //chart的中心点
    private PointF centerPoint;
    //开始角度
    private float startAngle = -90;
    //chart延长线
    private int lineLen = DensityUtil.dip2px(getContext(),12);
    //tag折线的长度
    private int tagLen = DensityUtil.dip2px(getContext(),10);
    //图表中所有文字的大小
    private int textSizeChartTag = DensityUtil.sp2px(getContext(),12);
    //文字与它旁边控件的间距
    private int textSpace = DensityUtil.dip2px(getContext(),4);
    //label开始的Y坐标
    private int labelStartY;
    //labelY坐标的最小值（高度超出时）
    private int labelMinTop;

    private int rectW = DensityUtil.dip2px(getContext(),20);
    private int rectH = DensityUtil.dip2px(getContext(),10);

    private int textColorLabel = Color.BLACK;
    private int chartRaduis;
    private int chartInnerRaduis;
    private int backColor = Color.WHITE;
    //外环默认的颜色
    private int outerDefColor = Color.LTGRAY;
    //是否显示内圈
    private boolean showInnerChart = false;
    //是否在内圈中显示label
    private boolean innerLabel = false;
    //label区域
    private Region regionLabel;
    //是否显示num为0的数据
    private boolean showZeroNum = true;

    //RGB颜色数组
    //#D95F5B红   #7189E6蓝   #5AB9C7蓝1  #B096D5紫   #6BBA97绿1  #DCAA61黄   #7DAB58绿2  #DC7F68橙
    private final int arrColorRgb[][] = {
            {113, 137, 230},   //    UIColorFromRGB(0xD95F5B),
            {217, 95, 91},     //    UIColorFromRGB(0x7189E6),
            {90, 185, 199},    //    UIColorFromRGB(0x5AB9C7),
            {170, 150, 213},   //   UIColorFromRGB(0xB096D5),
            {107, 186, 151},   //    UIColorFromRGB(0x6BBA97),
            {91, 164, 231},    //    UIColorFromRGB(0x5BA4E7),
            {220, 170, 97},//    UIColorFromRGB(0xDCAA61),
            {125, 171, 88},//    UIColorFromRGB(0x7DAB58),
            {233, 200, 88},//    UIColorFromRGB(0xE9C858),
            {213, 150, 196},//    UIColorFromRGB(0xd596c4)
            {220, 127, 104}//    UIColorFromRGB(0xDC7F68),
    };

    private List<PieBean> dataList = new ArrayList<>();
    private boolean startDraw = false;
    private long animDuration;
    private float animatedValue;
    //分数总数
    private float perNum;
    //分数最大值
    private float perMax;
    //右侧label的总高度
    private int labelAllHeight;
    //右侧label的间距
    private float labelSpace = DensityUtil.dip2px(getContext(),4);
    //右侧label文字高度
    private float labelTextHeight;
    //右侧label的高度（label的高度是 文字的高度和颜色矩形高度的最大值）
    private float oneLabelHeight;
    //颜色矩形的圆角半径
    private float roundRaduis = DensityUtil.dip2px(getContext(),6);
    //右侧label是否显示per
    private boolean showLabelNum = true;
    //移动距离
    private int moveTotal = 0;
    //图标的region
    private List<Region> chartRegion = new ArrayList<>();
    //选中索引
    private int selectedIndex = -1;
    //选中框的宽度
    private float lineWidth = DensityUtil.dip2px(getContext(),5);
    //选中框的颜色
    private int chartSelectedColor = Color.LTGRAY;
    private List<InnerLabelBean> innerLabelBeanList = new ArrayList<>();
    //inner label的总高度
    private int innerLabelAllHeight;

    public PieChart(Context context) {
        super(context);
        init();
    }

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PieChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * 初始化其他
     */
    private void init() {
        animDuration = 1000;
        paintChart = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintChart.setTextSize(textSizeChartTag);
        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setTextSize(textSizeChartTag);
        paintText.setColor(textColorLabel);
        paintSelected = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSelected.setStyle(Paint.Style.STROKE);
        paintSelected.setColor(chartSelectedColor);
        paintSelected.setStrokeWidth(lineWidth);
        paintInnerText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintInnerText.setTextAlign(Paint.Align.CENTER);
        gestureDetector = new GestureDetector(getContext(),simpleOnGestureListener);
    }

    /**
     * 初始化自定义属性
     * @param attrs
     */
    private void init(AttributeSet attrs) {

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

        //为wrap_content设置宽高
        if(widthMode == MeasureSpec.AT_MOST){
            resultWidth = widthSize * 2 / 3; //wrap_content的话 占用父view的2/3
        }
        if(heightMode == MeasureSpec.AT_MOST){
            resultHeight = heightSize * 2 / 3;
        }

        setMeasuredDimension(resultWidth,resultHeight);

        initAfterMeasure();
    }

    private void initAfterMeasure() {
        //全部可用矩形范围
        rectContent = new RectF(getPaddingLeft(),getPaddingTop(),getMeasuredWidth() - getPaddingRight(),getMeasuredHeight() - getPaddingBottom());
        //chart范围
        rectChart = new RectF(rectContent.left,rectContent.top,rectContent.right*2/3,rectContent.bottom);
        //label的范围
        rectLabel = new RectF(rectChart.right,rectContent.top,rectContent.right,rectContent.bottom);
        //chart中心点
        centerPoint = new PointF(rectChart.right/2,rectChart.bottom/2);
        regionLabel = new Region((int)rectLabel.left,(int)rectLabel.top,(int)rectLabel.right,(int)rectLabel.bottom);
        //半径
        float chartRaduisTemp = rectChart.right > rectChart.bottom ? rectChart.bottom : rectChart.right;
        paintChart.setTextSize(textSizeChartTag);
        float maxTextHeight = FontUtil.getFontHeight(paintChart);
        float maxTextLength = FontUtil.getFontlength(paintChart,String.valueOf(perMax));
        if(rectChart.right > rectChart.bottom){ //使用的是上下距离作直径
            chartRaduisTemp = chartRaduisTemp/2 - textSpace - maxTextHeight/2- lineLen ;
        }else {
            chartRaduisTemp = chartRaduisTemp/2 - textSpace- maxTextLength - textSpace - tagLen - lineLen ;
        }
        chartRaduis = (int) chartRaduisTemp;
        chartInnerRaduis = chartRaduis*3/4;
        //计算label相关变量
        labelAllHeight = 0;
        labelTextHeight = FontUtil.getFontHeight(paintText);
        oneLabelHeight = rectH > labelTextHeight ? rectH:labelTextHeight;
        labelAllHeight += labelSpace; //开始先来一个labelSpace,最后也有一个labelSpace
        for (PieBean pieBean : dataList) {
            labelAllHeight += (oneLabelHeight + labelSpace);
        }
        if(labelAllHeight > rectLabel.bottom){
            labelStartY = (int) rectLabel.top;
            labelMinTop = (int) (-labelAllHeight + rectLabel.bottom);
        }else {
            labelStartY = (int) (rectLabel.top + ((rectLabel.bottom - labelAllHeight)/2));
        }
        //计算chart的region
        chartRegion.clear();
        Path path = new Path();
        float startAngleTemp = startAngle;
        for (int i = 0; i < dataList.size(); i++) {
            PieBean pieBean = dataList.get(i);
            float sweepAngleTemp = pieBean.num/perNum * 360;
            Region region = new Region();
            float endX = (float) (centerPoint.x + chartRaduis * Math.cos(Math.toRadians(startAngleTemp)));
            float endY = (float) (centerPoint.y + chartRaduis * Math.sin(Math.toRadians(startAngleTemp)));
            path.moveTo(centerPoint.x,centerPoint.y);
            path.lineTo(endX,endY);
            path.addArc(new RectF(centerPoint.x - chartRaduis,centerPoint.y - chartRaduis,centerPoint.x + chartRaduis,centerPoint.y + chartRaduis),startAngleTemp,sweepAngleTemp);
            path.lineTo(centerPoint.x,centerPoint.y);
//            path.close();
            region.setPath(path,new Region(new Rect((int)(centerPoint.x - chartRaduis),(int)(centerPoint.y - chartRaduis),(int)(centerPoint.x + chartRaduis),(int)(centerPoint.y + chartRaduis))));
            chartRegion.add(region);
            path.reset();
            startAngleTemp+= sweepAngleTemp;
        }
        //计算innerLabel的总高度
        innerLabelAllHeight = 0;
        for (InnerLabelBean innerLabelBean : innerLabelBeanList) {
            paintInnerText.setTextSize(innerLabelBean.textSize);
            innerLabelAllHeight += (FontUtil.getFontHeight(paintInnerText) + labelSpace);
        }
        if(innerLabelAllHeight > 0){
            innerLabelAllHeight -= labelSpace;
        }
    }

    public void setDataList(List<PieBean> dataList) {
        if(dataList == null || dataList.size() == 0){
            return;
        }
        this.dataList.clear();
        this.dataList.addAll(dataList);
        perNum = 0;
        perMax = 0;
        for (PieBean pieBean : dataList) {
            perNum += pieBean.num;
            perMax = Math.max(perMax,pieBean.num);
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //test
//        test(canvas);

        if(!startDraw){
            startDraw = true;
            startAnimation();
        }else {
            drawChart(canvas);
        }
    }

    private void test(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        canvas.drawRect(rectChart,paint);
        paint.setColor(Color.BLUE);
        canvas.drawRect(rectLabel,paint);
    }


    private void drawChart(Canvas canvas) {
        if(dataList == null || dataList.size() == 0){
            return;
        }

        //background
        drawDefualt(canvas);

        //1、绘制图表部分
        float startAngleTemp = startAngle;
        float sweepAngleTemp;
        for (int i = 0; i < dataList.size(); i++) {
            PieBean pieBean = dataList.get(i);
            if(pieBean.num == 0 && !showZeroNum){
                continue;
            }
            int[] ints = arrColorRgb[i % arrColorRgb.length];
            paintChart.setARGB(255,ints[0],ints[1],ints[2]);
            if(selectedIndex == i){
                paintChart.setTypeface(Typeface.DEFAULT_BOLD);
                paintText.setTypeface(Typeface.DEFAULT_BOLD);
            }else {
                paintChart.setTypeface(Typeface.DEFAULT);
                paintText.setTypeface(Typeface.DEFAULT);
            }

            float proportion = 360f * pieBean.num/perNum;
            sweepAngleTemp = proportion  * animatedValue;
            //1.1 绘制图形
            RectF rectF = new RectF(centerPoint.x - chartRaduis,centerPoint.y - chartRaduis,centerPoint.x + chartRaduis,centerPoint.y + chartRaduis);
            canvas.drawArc(rectF,startAngleTemp,sweepAngleTemp,true,paintChart);
            if(selectedIndex == i){
                canvas.drawArc(rectF,startAngleTemp,sweepAngleTemp,true,paintSelected);
            }

            //1.2 绘制线与文字
            float lineStartX = (float) (centerPoint.x + chartRaduis * Math.cos(Math.toRadians(startAngleTemp + sweepAngleTemp/2)));
            float lineStartY = (float) (centerPoint.y + chartRaduis * Math.sin(Math.toRadians(startAngleTemp + sweepAngleTemp/2)));
            float lineEndX = (float) (centerPoint.x + (chartRaduis + lineLen) * Math.cos(Math.toRadians(startAngleTemp + sweepAngleTemp/2)));
            float lineEndY = (float) (centerPoint.y + (chartRaduis + lineLen) * Math.sin(Math.toRadians(startAngleTemp + sweepAngleTemp/2)));
            boolean isRight = true;
            if(startAngleTemp + sweepAngleTemp/2 > 90 && startAngleTemp + sweepAngleTemp/2 < 270){
                isRight = false;
            }
            float tagEndX = isRight ? lineEndX + tagLen: lineEndX - tagLen;
            float tagEndY = lineEndY;

            float textStartX = isRight ? tagEndX + textSpace : tagEndX - textSpace - FontUtil.getFontlength(paintChart,String.valueOf(pieBean.num));
            float textBaseLine = lineEndY - FontUtil.getFontHeight(paintChart)/2 + Math.abs(paintChart.getFontMetrics().top);

            canvas.drawLine(lineStartX,lineStartY,lineEndX,lineEndY,paintChart);
            canvas.drawLine(lineEndX,lineEndY,tagEndX,tagEndY,paintChart);
            canvas.drawText(String.valueOf(pieBean.num),textStartX,textBaseLine,paintChart);

            startAngleTemp += sweepAngleTemp;
        }

        //2、画label
        float startY = labelStartY + labelSpace + moveTotal;
        for (int i = 0; i < dataList.size(); i++) {
            PieBean pieBean = dataList.get(i);
            int[] ints = arrColorRgb[i % arrColorRgb.length];
            paintChart.setARGB(255,ints[0],ints[1],ints[2]);
            if(selectedIndex == i){
                paintChart.setTypeface(Typeface.DEFAULT_BOLD);
                paintText.setTypeface(Typeface.DEFAULT_BOLD);
            }else {
                paintChart.setTypeface(Typeface.DEFAULT);
                paintText.setTypeface(Typeface.DEFAULT);
            }

            float startYTemp =startY + i *(oneLabelHeight+labelSpace);
            //2.1 画颜色矩形
            float rectLeft = rectLabel.left + textSpace;
            float rectTop = oneLabelHeight > rectH ? startYTemp + (oneLabelHeight-rectH)/2 : startYTemp;
            float rectRight = rectLeft + rectW;
            float rectBottom = rectTop + rectH;
            RectF rectF = new RectF(rectLeft, rectTop, rectRight, rectBottom);
            canvas.drawRoundRect(rectF,roundRaduis,roundRaduis,paintChart);

            //2.2画name
            float nameStartX = rectRight + textSpace;
            float textBaseLine = oneLabelHeight > labelTextHeight ? startYTemp + (oneLabelHeight - labelTextHeight)/2 + Math.abs(paintText.getFontMetrics().top) : startYTemp +  Math.abs(paintText.getFontMetrics().top);
            paintText.setColor(textColorLabel);
            canvas.drawText(pieBean.name,nameStartX,textBaseLine,paintText);
            //2.3画per
            if(showLabelNum){
                float numStartX = rectLabel.right - textSpace - FontUtil.getFontlength(paintText,String.valueOf(pieBean.num));
                canvas.drawText(String.valueOf(pieBean.num),numStartX,textBaseLine,paintText);
            }
        }

        if(chartInnerRaduis > 0 && showInnerChart){
            paintChart.setColor(backColor);
            canvas.drawCircle(centerPoint.x,centerPoint.y,chartInnerRaduis,paintChart);
        }

        //画内圈中的label
        if(showInnerChart && innerLabelBeanList.size() > 0){
            float innerLabelStartY = rectChart.bottom/2 - innerLabelAllHeight/2;
            for (InnerLabelBean innerLabelBean : innerLabelBeanList) {
                paintInnerText.setTextSize(innerLabelBean.textSize);
                paintInnerText.setColor(innerLabelBean.color);
                float textBaseLine = innerLabelStartY + Math.abs(paintInnerText.getFontMetrics().top);
                canvas.drawText(innerLabelBean.content,centerPoint.x,textBaseLine,paintInnerText);
                innerLabelStartY += (FontUtil.getFontHeight(paintInnerText) + labelSpace);
            }
        }
    }

    private void drawDefualt(Canvas canvas) {
        paintChart.setColor(outerDefColor);
        canvas.drawCircle(centerPoint.x,centerPoint.y,chartRaduis,paintChart);
        if(chartInnerRaduis > 0 && showInnerChart){
            paintChart.setColor(backColor);
            canvas.drawCircle(centerPoint.x,centerPoint.y,chartInnerRaduis,paintChart);
        }
    }

    private void startAnimation(){
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animatedValue = ((float) animation.getAnimatedValue());
                invalidate();
            }
        });
        valueAnimator.setDuration(animDuration);
        valueAnimator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(regionLabel.contains((int)event.getX(),(int)event.getY()) && labelStartY == rectLabel.top){
            return gestureDetector.onTouchEvent(event);
        }else if(event.getAction() == MotionEvent.ACTION_DOWN){
            for (int i = 0; i < chartRegion.size(); i++) {
                Region region = chartRegion.get(i);
                if(region.contains((int)event.getX(),(int)event.getY())){
                    if(selectedIndex != i){
                        selectedIndex = i;
                    }else {
                        selectedIndex = -1;
                    }
                    invalidate();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(Math.abs(distanceX) > Math.abs(distanceY)){
                return false;
            }else {
                onMove(-distanceY);
                return true;
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(Math.abs(velocityY) > Math.abs(velocityX)){
                PieChart.this.onFling(velocityY);
                return true;
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    };

    /**
     * 抬起后
     * @param velocityY
     */
    private void onFling(float velocityY) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(velocityY / 100f, 0f);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                onMove(animatedValue);
            }
        });
        valueAnimator.setDuration(1000);
        valueAnimator.start();
    }

    /**
     * 移动
     * @param distanceY
     */
    private void onMove(float distanceY) {
        if(labelStartY + moveTotal + distanceY >= labelStartY){
            moveTotal = 0;
        }else if(labelStartY + moveTotal + distanceY <= labelMinTop){
            moveTotal = labelMinTop;
        }else {
            moveTotal += distanceY;
        }
        invalidate();
    }

    private GestureDetector gestureDetector;

    public float getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(float startAngle) {
        this.startAngle = startAngle;
    }

    public int getLineLen() {
        return lineLen;
    }

    public void setLineLen(int lineLen) {
        this.lineLen = lineLen;
    }

    public int getTagLen() {
        return tagLen;
    }

    public void setTagLen(int tagLen) {
        this.tagLen = tagLen;
    }

    public int getTextSizeChartTag() {
        return textSizeChartTag;
    }

    public void setTextSizeChartTag(int textSizeChartTag) {
        this.textSizeChartTag = textSizeChartTag;
    }

    public int getTextSpace() {
        return textSpace;
    }

    public void setTextSpace(int textSpace) {
        this.textSpace = textSpace;
    }

    public int getRectW() {
        return rectW;
    }

    public void setRectW(int rectW) {
        this.rectW = rectW;
    }

    public int getRectH() {
        return rectH;
    }

    public void setRectH(int rectH) {
        this.rectH = rectH;
    }

    public int getTextColorLabel() {
        return textColorLabel;
    }

    public void setTextColorLabel(int textColorLabel) {
        this.textColorLabel = textColorLabel;
    }

    public int getBackColor() {
        return backColor;
    }

    public void setBackColor(int backColor) {
        this.backColor = backColor;
    }

    public int getOuterDefColor() {
        return outerDefColor;
    }

    public void setOuterDefColor(int outerDefColor) {
        this.outerDefColor = outerDefColor;
    }

    public boolean isShowInnerChart() {
        return showInnerChart;
    }

    public void setShowInnerChart(boolean showInnerChart) {
        this.showInnerChart = showInnerChart;
    }

    public int[][] getArrColorRgb() {
        return arrColorRgb;
    }

    public boolean isInnerLabel() {
        return innerLabel;
    }

    public void setInnerLabel(boolean innerLabel) {
        this.innerLabel = innerLabel;
    }

    public long getAnimDuration() {
        return animDuration;
    }

    public void setAnimDuration(long animDuration) {
        this.animDuration = animDuration;
    }

    public boolean isShowZeroNum() {
        return showZeroNum;
    }

    public void setShowZeroNum(boolean showZeroNum) {
        this.showZeroNum = showZeroNum;
    }

    public void setInnerLabelBeanList(List<InnerLabelBean> innerLabelBeanList) {
        this.innerLabelBeanList = innerLabelBeanList;
    }
}
