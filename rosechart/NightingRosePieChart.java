package com.ryrj.testgit.mychart.rosechart;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
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

import com.ryrj.testgit.R;
import com.ryrj.testgit.utils.DensityUtil;
import com.ryrj.testgit.utils.FontUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wrf on 2018/9/12.
 */

public class NightingRosePieChart extends View {

    //所有内容的边界矩形，chart矩形，label矩形
    private RectF rectContent,rectChart,rectLabel;
    //图标半径
    private int chartRaduis;
    //内圈半径
    private int chartInnerRaduis = DensityUtil.dip2px(getContext(),15);
    //圆心
    private PointF centerPoint;
    //tag线
    private int tagLineLen = DensityUtil.dip2px(getContext(),10);
    //延长线
    private int lineLen = DensityUtil.dip2px(getContext(),12);
    //画图标的paint
    private Paint paintChart;
    //图标字体大小
    private int textSizeChartTag;
    //最大值
    private double VALUE_MAX;
    private List<RoseBean> dataList = new ArrayList<>();
    //图标开始角度
    private float startAngle = -90;
    //转过的角度
    private float sweepAngle;
    //文字与它旁边控件的间隔
    private int textSpace;
    //右侧矩形的间距
    private int labelRectSpace;
    private float roundRaduis;
    //右侧颜色label的宽高
    private int rectH;
    private int rectW;

    private PointF labelStartPoint;

    private Paint paintDashLine;

    //图表风格
    private STYLE chartStyle = STYLE.STYLE_FIX;
    //Y轴方向移动距离
    private float moveTotal;
    //当内容超时出Y轴方向的最小坐标
    private float maxTop;
    //label点击范围
    private Region region;
    //右侧文字颜色
    private int textColorTag;
    //是否需要动画
    private boolean startAnimation;
    //背景色
    private int backColor = Color.WHITE;
    //扇形点击区域
    private List<Region> chartRegion = new ArrayList<>();
    //label点击区域
    private List<Region> labelRegion = new ArrayList<>();
    //选中项
    private int selectedIndex = -1;
    //画选中的框
    private Paint paintSelected;
    //选中框的线宽度
    private int lineWidth = DensityUtil.dip2px(getContext(),3);
    //图标上是否显示数据为0的数据
    private boolean showZeroNum = true;

    public enum STYLE{
        STYLE_FIX, //开始角度固定
        STYLE_CHANGE; //开始角度随着动画从0增加到应该到的角度
    }


    /**
     * 是否开始绘制
     */
    private boolean startDraw = false;

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
    private int duration;
    private float animPro ;

    public NightingRosePieChart(Context context) {
        super(context);
        init();
    }

    public NightingRosePieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public NightingRosePieChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init() {
        paintChart = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintChart.setTextSize(textSizeChartTag);
        labelStartPoint = new PointF();
        paintDashLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDashLine.setStyle(Paint.Style.STROKE);
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{5, 5}, 0);
        paintDashLine.setPathEffect(dashPathEffect);
    }
//    <!--textSize //所有文字大小-->
//            <!--textColor //右侧文字颜色-->
//            <!--textSpace //文字与控件距离-->
//            <!--labelRectSpace //右侧label的上下间距-->
//            <!--roundRaduis //右侧颜色矩形的圆角半径-->
//            <!--rectH rectW //右侧颜色矩形的宽高-->
//    <!--chartStyle //图标风格-->
//            <!--duration //动画时间-->
//            <!--startAnimation //是否需要动画-->
    private void init(AttributeSet attrs) {
        //解析自定义属性
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.NightingRosePieChart);
        if(typedArray != null){
            textSizeChartTag = (int) typedArray.getDimension(R.styleable.NightingRosePieChart_ade_textSize,DensityUtil.sp2px(getContext(),12));
            textColorTag = typedArray.getColor(R.styleable.NightingRosePieChart_ade_textColor,Color.BLACK);
            textSpace = (int) typedArray.getDimension(R.styleable.NightingRosePieChart_ade_textSpace, DensityUtil.dip2px(getContext(),2));
            labelRectSpace = (int) typedArray.getDimension(R.styleable.NightingRosePieChart_ade_labelRectSpace,DensityUtil.dip2px(getContext(),8));
            roundRaduis = typedArray.getDimension(R.styleable.NightingRosePieChart_ade_roundRaduis,DensityUtil.dip2px(getContext(),4));
            rectH = (int) typedArray.getDimension(R.styleable.NightingRosePieChart_ade_rectH, DensityUtil.dip2px(getContext(),10));
            rectW = (int) typedArray.getDimension(R.styleable.NightingRosePieChart_ade_rectW,DensityUtil.dip2px(getContext(),20));
            int style = typedArray.getInt(R.styleable.NightingRosePieChart_ade_chartStyle,0);
            if(style == 0){
                chartStyle = STYLE.STYLE_FIX;
            }else if(style == 1){
                chartStyle = STYLE.STYLE_CHANGE;
            }else {
                chartStyle = STYLE.STYLE_FIX;
            }
            duration = typedArray.getInt(R.styleable.NightingRosePieChart_ade_duration,2000);
            startAnimation = typedArray.getBoolean(R.styleable.NightingRosePieChart_ade_startAnimation,true);
            typedArray.recycle();
        }
        init();
    }

    /**
     * 处理宽高
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int resultWidth = widthSize;
        int resultHeight =heightSize;
        //如果宽高为wrap_content 取相应父view宽高的2/3，因为父类传过来widthSize和heightSize便是父view的宽高
        if(widthMode == MeasureSpec.AT_MOST){
            resultWidth = widthSize*2/3;
        }
        if(heightMode == MeasureSpec.AT_MOST){
            resultHeight = heightSize*2/3;
        }

        setMeasuredDimension(resultWidth,resultHeight);

        initAfterMeasure();
    }

    private void initAfterMeasure() {
        //所有可见内容的最外侧rect,并且支持padding
        rectContent = new RectF(getPaddingLeft(),getPaddingTop(),getMeasuredWidth()-getPaddingRight(),getMeasuredHeight() - getPaddingBottom());
        //图标部分 占宽度2/3
        rectChart = new RectF(rectContent.left,rectContent.top,rectContent.right * 2/3,rectContent.bottom);
        centerPoint = new PointF(rectChart.right/2,rectChart.bottom/2);
        //label部分，占宽度1/3
        rectLabel = new RectF(rectChart.right,rectContent.top,rectContent.right,rectContent.bottom);
        region = new Region(new Rect((int)rectLabel.left,(int)rectLabel.top,(int)rectLabel.right,(int)rectLabel.bottom));
        chartRaduis = (int) (rectChart.right / 2 * 2 / 3);  //半径为宽度的一半的2/3
        paintSelected = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSelected.setColor(Color.LTGRAY);
        paintSelected.setStyle(Paint.Style.STROKE);
        paintSelected.setStrokeWidth(lineWidth);

        RectF rectF = new RectF(centerPoint.x-chartRaduis,centerPoint.y-chartRaduis,centerPoint.x + chartRaduis,centerPoint.y + chartRaduis);
        float startAngleTemp;
        float sweepAngleTemp = 360f/dataList.size();
        if(dataList != null && dataList.size() > 0){
            chartRegion.clear();
            for (int i = 0; i < dataList.size(); i++) {
                startAngleTemp =startAngle +  i * sweepAngleTemp;
                Path path = new Path();
                path.moveTo(centerPoint.x,centerPoint.y);
                path.lineTo((float)(centerPoint.x + (chartRaduis + lineLen) * Math.cos(Math.toRadians(startAngleTemp)))
                        ,(float)(centerPoint.y + (chartRaduis + lineLen) * Math.sin(Math.toRadians(startAngleTemp))));
                path.addArc(rectF,startAngleTemp,sweepAngleTemp);
                path.lineTo(centerPoint.x,centerPoint.y);
                path.close();
                Region region = new Region();
                region.setPath(path,new Region(new Rect((int)rectF.left,(int)rectF.top,(int)rectF.right,(int)rectF.bottom)));
                chartRegion.add(region);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(!startDraw && startAnimation){
            startDraw = true;
            startAnim();
        }else{
            drawChart(canvas);
        }
    }

    private void startAnim() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animPro = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    private void drawChart(Canvas canvas) {
        if(dataList == null || dataList.size() == 0){
            return;
        }

        //画图表部分
        for (int i = 0; i < dataList.size(); i++) {
            RoseBean roseBean = dataList.get(i);
            if(roseBean.num == 0 && !showZeroNum){
                continue;
            }
            //根据自己的值时最大值的几分之几 算出是最大半径chartRaduis的几分之几
            float mySelfRaduis = (float) (chartRaduis * roseBean.num / VALUE_MAX);
            //剩余的部分画线
            float overPlusLen = chartRaduis - mySelfRaduis;
            int[] ints = arrColorRgb[i % arrColorRgb.length];
            paintChart.setARGB(255,ints[0],ints[1],ints[2]);

            //画扇形
            float startAngleTemp;
            float sweepAngleTemp;
            if(chartStyle == STYLE.STYLE_FIX){
                startAngleTemp =  startAngle + i * sweepAngle;
                sweepAngleTemp = sweepAngle  * animPro;
            }else{
                sweepAngleTemp = sweepAngle * animPro;
                startAngleTemp = startAngle + i * sweepAngleTemp;
            }
            RectF rectF = new RectF(centerPoint.x - mySelfRaduis, centerPoint.y - mySelfRaduis, centerPoint.x + mySelfRaduis, centerPoint.y + mySelfRaduis);
            canvas.drawArc(rectF,startAngleTemp,sweepAngleTemp,true,paintChart);
            if(selectedIndex == i){
                canvas.drawArc(rectF,startAngleTemp,sweepAngleTemp,true,paintSelected);
            }

            //画线
            float startX = (float) (centerPoint.x +  mySelfRaduis * Math.cos(Math.toRadians(startAngleTemp + sweepAngleTemp/2)));
            float startY = (float) (centerPoint.y + mySelfRaduis * Math.sin(Math.toRadians(startAngleTemp + sweepAngleTemp/2)));
            float endX = (float) (centerPoint.x +  (chartRaduis + lineLen) * Math.cos(Math.toRadians(startAngleTemp + sweepAngleTemp/2)));
            float endY = (float) (centerPoint.y + (chartRaduis + lineLen) * Math.sin(Math.toRadians(startAngleTemp + sweepAngleTemp/2)));
            canvas.drawLine(startX,startY,endX,endY,paintChart);
            //画折线
            boolean isRight = true;
            float tempAngle = startAngleTemp + sweepAngleTemp/2;
            if(tempAngle<270 && tempAngle > 90){
                isRight = false;
            }
            float turningEndX = isRight ? endX + tagLineLen : endX - tagLineLen;
            float turningEndY = endY;
            canvas.drawLine(endX,endY,turningEndX,turningEndY,paintChart);
            //写文字
            paintChart.setTextSize(textSizeChartTag);
            if(selectedIndex == i){
                paintChart.setTypeface(Typeface.DEFAULT_BOLD);
            }else {
                paintChart.setTypeface(Typeface.DEFAULT);
            }
            float textStartX = isRight? turningEndX + textSpace : turningEndX - textSpace - FontUtil.getFontlength(paintChart,roseBean.name);
            float textStartBaseLine = turningEndY - FontUtil.getFontHeight(paintChart)/2 + Math.abs(paintChart.getFontMetrics().top);
            canvas.drawText(roseBean.name,textStartX,textStartBaseLine,paintChart);
        }
        //画内圈
        paintChart.setColor(backColor);
        canvas.drawCircle(centerPoint.x,centerPoint.y,chartInnerRaduis,paintChart);

        //画label部分
        paintChart.setTextSize(textSizeChartTag);
        float textHeight = FontUtil.getFontHeight(paintChart);
        float oneLableHeight = textHeight > rectH ? textHeight : rectH;
        float allHeight = 0;
        for (RoseBean roseBean : dataList) {
            Log.i("mydata","allHeight:"+allHeight + "onelabelheight:"+ (oneLableHeight + labelRectSpace));
            allHeight += (oneLableHeight + labelRectSpace);
        }
        allHeight += labelRectSpace;//再为最上面一个label与顶添加一个space距离
        if(allHeight > (rectLabel.bottom - rectLabel.top)){ //超出
            labelStartPoint.set(rectLabel.left + textSpace,rectLabel.top);
            maxTop = -allHeight + (rectLabel.bottom - rectLabel.top);
        }else{ //没超出
            labelStartPoint.set(rectLabel.left + textSpace,rectLabel.top + (rectLabel.bottom - allHeight)/2);
            maxTop = 0;
        }
        Log.i("mydata","allHeight.." + allHeight + "..content height.." + (rectLabel.bottom - rectLabel.top));
        float top = labelStartPoint.y;
        if(maxTop != 0){
            top += moveTotal;
        }
        for (int i = 0; i < dataList.size(); i++) {
            RoseBean roseBean = dataList.get(i);
            //1、画颜色矩形
            int[] ints = arrColorRgb[i % arrColorRgb.length];
            paintChart.setARGB(255,ints[0],ints[1],ints[2]);
            RectF rectF = new RectF();
            rectF.left = labelStartPoint.x;
            rectF.top = top + i * (labelRectSpace + oneLableHeight) + labelRectSpace + (oneLableHeight - rectH) / 2; //因为你不知道用的是矩形高还是文字高，所以必须得减一下，如果用的是矩形高那么此值就为0 不影响，如果用的是文字高，说明矩形小一点，所以坐标需要下移
            rectF.right = rectF.left + rectW;
            rectF.bottom = rectF.top + rectH;
            canvas.drawRoundRect(rectF, roundRaduis, roundRaduis,paintChart);
            if(selectedIndex == i){
                canvas.drawRoundRect(rectF, roundRaduis, roundRaduis,paintSelected);
            }
            //2、画文字
            paintChart.setColor(textColorTag);
            if(selectedIndex == i){
                paintChart.setTypeface(Typeface.DEFAULT_BOLD);
            }else {
                paintChart.setTypeface(Typeface.DEFAULT);
            }
            float labelNameStartX = rectF.right + textSpace;
            float labelNameStartBaseLine = top + i * (labelRectSpace + oneLableHeight) + labelRectSpace + (oneLableHeight - FontUtil.getFontHeight(paintChart)) / 2 + Math.abs(paintChart.getFontMetrics().top);
            canvas.drawText(roseBean.name,labelNameStartX,labelNameStartBaseLine,paintChart);

            //画右侧数字
            float textW = FontUtil.getFontlength(paintChart,String.valueOf(roseBean.num));
            float labelNumStartX = rectLabel.right - textSpace - textW;
            float labelNumBaseLine = labelNameStartBaseLine;
            canvas.drawText(String.valueOf(roseBean.num),labelNumStartX,labelNumBaseLine,paintChart);

            //画中间的间隔
            float dashLineStartX = labelNameStartX + FontUtil.getFontlength(paintChart,roseBean.name) + textSpace;
            float dashLineStartY = rectF.top + (rectF.bottom - rectF.top)/2;
            float dashLineEndX = labelNumStartX - textSpace;
            float dashLineEndY = dashLineStartY;
            Path path = new Path();
            path.moveTo(dashLineStartX,dashLineStartY);
            path.lineTo(dashLineEndX,dashLineEndY);
            canvas.drawPath(path,paintDashLine);
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

    public void setDataList(List<RoseBean> dataList) {
        if(dataList!=null && dataList.size() > 0){
            this.dataList.clear();
            this.dataList.addAll(dataList);
            for (int i = 0; i < dataList.size(); i++) {
                RoseBean roseBean = dataList.get(i);
                VALUE_MAX = Math.max(VALUE_MAX,roseBean.num);
            }
            sweepAngle = 360f / dataList.size();
            invalidate();
        }
    }

    public void setChartStyle(STYLE chartStyle) {
        this.chartStyle = chartStyle;
    }

    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener(){
        //确定消耗事件
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        //移动
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            onMove(-distanceY);
            return true;
        }
        //手指抬气候的滑动
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
           NightingRosePieChart.this.onFling(velocityY);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }
    };

    /**
     * Y轴滑动
     * @param velocityY
     */
    private void onFling(float velocityY) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(velocityY / 100, 0f);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
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
     * Y轴移动
     * @param distanceY
     */
    private void onMove(float distanceY) {
        if(labelStartPoint.y + moveTotal + distanceY <= maxTop){ //已经下拉到最后
            moveTotal = maxTop;
        }else if(labelStartPoint.y + moveTotal + distanceY >= rectLabel.top){ //拉到最上面了
            moveTotal = 0;
        }else{
            moveTotal += distanceY;
        }
        invalidate();
    }

    private GestureDetector gestureDetector = new GestureDetector(getContext(),simpleOnGestureListener);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(region.contains((int)event.getX(),(int)event.getY()) && maxTop != 0){
            return gestureDetector.onTouchEvent(event);
        }else if(event.getAction() == MotionEvent.ACTION_DOWN){
            for (int i = 0; i < chartRegion.size(); i++) {
                Region region = chartRegion.get(i);
                if(region.contains((int)event.getX(),(int)event.getY())){
                    if(selectedIndex != i){  //为什么要这样呢？避免一直点击一个，一直重绘 算是优化
                        selectedIndex = i;
                        invalidate();
                    }else{ // 如果是相等的话  那就把选中状态改为非选中
                        selectedIndex = -1;
                        invalidate();
                    }
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public int getTagLineLen() {
        return tagLineLen;
    }

    public void setTagLineLen(int tagLineLen) {
        this.tagLineLen = tagLineLen;
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

    public int getLabelRectSpace() {
        return labelRectSpace;
    }

    public void setLabelRectSpace(int labelRectSpace) {
        this.labelRectSpace = labelRectSpace;
    }

    public float getRoundRaduis() {
        return roundRaduis;
    }

    public void setRoundRaduis(float roundRaduis) {
        this.roundRaduis = roundRaduis;
    }

    public int getRectH() {
        return rectH;
    }

    public void setRectH(int rectH) {
        this.rectH = rectH;
    }

    public int getRectW() {
        return rectW;
    }

    public void setRectW(int rectW) {
        this.rectW = rectW;
    }

    public STYLE getChartStyle() {
        return chartStyle;
    }

    public int getTextColorTag() {
        return textColorTag;
    }

    public void setTextColorTag(int textColorTag) {
        this.textColorTag = textColorTag;
    }

    public boolean isStartAnimation() {
        return startAnimation;
    }

    public void setStartAnimation(boolean startAnimation) {
        this.startAnimation = startAnimation;
    }

    public int[][] getArrColorRgb() {
        return arrColorRgb;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setChartInnerRaduis(int chartInnerRaduis) {
        this.chartInnerRaduis = chartInnerRaduis;
    }
}
