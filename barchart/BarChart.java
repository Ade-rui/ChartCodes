package com.ryrj.testgit.mychart.barchart;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.ryrj.testgit.R;
import com.ryrj.testgit.utils.DensityUtil;
import com.ryrj.testgit.utils.FontUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wrf on 2018/9/25.
 */

public class BarChart extends View {

    //内容区域
    private RectF rectContent,rectLabel;
    //坐标轴起始点
    private PointF startPoint;
    //画笔
    private Paint paintChart,paintText;
    //坐标text与它周围的间隔
    private int coordinateTextSpace;
    //坐标颜色
    private int coordinateTextColor ;
    //坐标文字大小
    private int coordinateTextSize;
    //一组柱子颜色（暂时不支持一组多个柱子了）
//    private int barColor[] = {Color.BLUE,Color.YELLOW};
    //柱子颜色
    private int barColor;
    //柱子宽度
    private int barWidth;
    //下面的label与坐标部分的上间距
    private int labelMarginTop;
    //底部label与周围控件的间距
    private int labelSpace;
    //一组label和颜色矩形之间间距
    private int labelItemSpace;
    //底栏颜色矩形的宽高
    private int rectDimension ;
    //柱子之间的距离
    private int barSpace;
    //一组柱子之间的距离
    private int barItemSpace;
    //开始画柱子的坐标
    private int startBarX;
    //柱子上面的文字与柱子的间距
    private int barTextSpace;
    //底部内容的行高
    private float oneBottomLabelHeight;
    //单柱子数据
    private List<BarBean> dataList = new ArrayList<>();
    //底部label数据
    private List<LabelBean> labelList = new ArrayList<>();
    //Y轴刻度数量
    private int YMarkCount;
    //每个刻度的高度
    private float oneMarkHeight;
    //最大刻度的高度
    private float allMarkHeight;
    //刻度最大值
    private float max;
    //刻度
    private int item;
    //所有底部label的宽度
    private int allLabelWidth;
    //label的开始X坐标
    private int labelStartX;
    //是否开始绘制
    private boolean startDraw;
    //动画完成比
    private float animPro;
    //动画时间
    private long animDuration ;
    //移动了的距离
    private int moveTotal;
    //最小的x坐标
    private int minX;
    //可点击区域
    private Region region;
    //系统默认的背景色
    private int themeBackgroundColor;

    public BarChart(Context context) {
        super(context);
        init();
    }

    public BarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BarChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(){
        paintChart = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(coordinateTextColor);
        paintText.setTextSize(coordinateTextSize);
    }

    private void init(AttributeSet attrs){
        //初始化自定义属性
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.BarChart);
        coordinateTextSpace = (int) typedArray.getDimension(R.styleable.BarChart_coordinateTextSpace,DensityUtil.dip2px(getContext(),2));
        coordinateTextColor = typedArray.getColor(R.styleable.BarChart_coordinateTextColor,Color.LTGRAY);
        coordinateTextSize = (int) typedArray.getDimension(R.styleable.BarChart_coordinateTextSize,DensityUtil.sp2px(getContext(),12));
        barColor = typedArray.getColor(R.styleable.BarChart_barColor,Color.YELLOW);
        barWidth = (int) typedArray.getDimension(R.styleable.BarChart_barWidth,DensityUtil.dip2px(getContext(),16));
        labelMarginTop = (int) typedArray.getDimension(R.styleable.BarChart_labelMarginTop,DensityUtil.dip2px(getContext(),18));
        labelSpace = (int) typedArray.getDimension(R.styleable.BarChart_labelSpace,DensityUtil.dip2px(getContext(),6));
        labelItemSpace = (int) typedArray.getDimension(R.styleable.BarChart_labelItemSpace,DensityUtil.dip2px(getContext(),12));
        rectDimension = (int) typedArray.getDimension(R.styleable.BarChart_rectDimension, DensityUtil.dip2px(getContext(),8));
        barSpace = (int) typedArray.getDimension(R.styleable.BarChart_barSpace,DensityUtil.dip2px(getContext(),1));
        barItemSpace = (int) typedArray.getDimension(R.styleable.BarChart_barItemSpace, DensityUtil.dip2px(getContext(),12));
        barTextSpace = (int) typedArray.getDimension(R.styleable.BarChart_barTextSpace,DensityUtil.dip2px(getContext(),4));
        YMarkCount =  typedArray.getInt(R.styleable.BarChart_YMarkCount,5);
        animDuration = typedArray.getInt(R.styleable.BarChart_animDuration,1000);
        typedArray.recycle();

        //获取背景主题色
        TypedArray array = getContext().getTheme().obtainStyledAttributes(new int[] {
                android.R.attr.colorBackground,
        });
        themeBackgroundColor = array.getColor(0, 0xFF00FF);
        array.recycle();
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
            resultWidth = widthSize * 2 / 3;
        }
        if(heightMode == MeasureSpec.AT_MOST){
            resultHeight = heightSize * 2 / 3;
        }

        setMeasuredDimension(resultWidth,resultHeight);

        initAfterMeasure();
    }

    private void initAfterMeasure() {
        rectContent = new RectF(getPaddingLeft(),getPaddingTop(),getMeasuredWidth() - getPaddingRight(),getMeasuredHeight() - getPaddingBottom());
        max = 0 ;
        for (BarBean barBean : dataList) {
            max = Math.max(max,barBean.num);
        }
        item = 0;
        if(max <= YMarkCount){ //如果最大值小于等于YMarkCount，那么每个刻度就为1,英文 1 * YMarkCount >= max
            item = 1;
        }else {
            item = (int) (max / YMarkCount);
        }
        item++;//YMarkCount, 相除后int的item可能会丢失精度,加入25/5 那么不丢失，加入数值大于25小于30 或者小于25大于20这两个区间，都会丢失精度所以让item+1  保证item * YMarkCount >= max
        if(item<10){
            //item为1,2,5的话 还为1,2,5 如果是3,4 就让他为5，如果是6,7,8,9就让他为10  因为我们常见的刻度单位中就是1,2,5,10,100，1000...等这样的单位
            item = (item == 3 || item == 4 || item == 6 || item == 7 || item == 8 || item == 9) ? ((item == 3 || item == 4) ? 5 : 10) : item;
            max = item * YMarkCount;
        }else {
            int countLen = String.valueOf(item).length(); //几位的数字
            int firstCount = String.valueOf(item).charAt(0); //首位的数字
            max = (int) (firstCount * Math.pow(10,countLen - 1)) * YMarkCount;
        }

        paintText.setTextSize(coordinateTextSize);
        float fontHeight = FontUtil.getFontHeight(paintText);
        oneBottomLabelHeight = rectDimension > fontHeight ? rectDimension : fontHeight;
        float maxLength = FontUtil.getFontlength(paintText,String.valueOf(max));
        //坐标起始点
        if(labelList == null || labelList.size() == 0){
            startPoint = new PointF(rectContent.left + maxLength + coordinateTextSpace,rectContent.bottom - fontHeight - coordinateTextSpace);
        }else {
            startPoint = new PointF(rectContent.left + maxLength + coordinateTextSpace,rectContent.bottom - oneBottomLabelHeight - labelMarginTop - fontHeight - coordinateTextSpace);
        }

        //画第一个柱子时  从x轴的二分之一barItemSpace开始画 这样显得很好看。因为从开始画显得挤，从barItemSpace开始画 会显得空间有些大。
        startBarX = (int) (startPoint.x + barItemSpace/2);

        //底部label区域
        rectLabel = new RectF(startPoint.x,rectContent.bottom - oneBottomLabelHeight - labelMarginTop,rectContent.right,rectContent.bottom);

        //每个刻度的大小， 用比刻度大于1刻度的总数来除以，保证不会max的时候  最大刻度到顶部 而不太美观
        oneMarkHeight = startPoint.y / (YMarkCount + 1);
        allMarkHeight = oneMarkHeight * YMarkCount;

        //计算label
        allLabelWidth = 0;
        if(labelList == null || labelList.size() == 0){
            allLabelWidth = 0;
        }else {
            paintText.setTextSize(coordinateTextSize);
            for (LabelBean labelBean : labelList) {
                allLabelWidth += (rectDimension + labelSpace + FontUtil.getFontlength(paintText,labelBean.name) + labelItemSpace);
            }
            allLabelWidth -= labelItemSpace;
            labelStartX = 0;
            float rectWidth = (rectLabel.right - rectLabel.left);
            if(allLabelWidth <= rectWidth){ //没超出
                labelStartX = (int) (startPoint.x + (rectWidth - allLabelWidth)/2);
            }else {
                //应该是0坐标点的稍微靠右一点 这样显得不拥挤
                labelStartX = (int) (startPoint.x + DensityUtil.dip2px(getContext(),2));
            }
        }

        //计算数据有没有超出一屏
        float maxWidth = rectContent.right - startBarX;
        float allWidth = 0;
        for (int i = 0; i < dataList.size(); i++) {
            allWidth += (barWidth + barItemSpace);
        }
        if(maxWidth >= allWidth){
            minX = startBarX;
        }else{
            minX = (int) (-allWidth + maxWidth);
        }

        region = new Region(new Rect(startBarX,(int)rectContent.top,(int)rectContent.right,(int)startPoint.y));
    }

    public void setDataList(List<BarBean> dataList) {
        this.dataList = dataList;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        test(canvas);
        if(!startDraw){
            startDraw = true;
            startAnimator();
        }else {
            drawChart(canvas);
        }

    }

    private void startAnimator(){
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

    private void drawChart(Canvas canvas) {

        //画坐标轴
        paintChart.setColor(coordinateTextColor);
        paintText.setColor(coordinateTextColor);
        paintChart.setStrokeWidth(0);
        paintChart.setStyle(Paint.Style.FILL);

        //画X轴上东西
        for (int i = 0; i < dataList.size(); i++) {
            BarBean barBean = dataList.get(i);
            paintChart.setColor(barColor);
            //暂时不支持一组多个bar了，所以不用加上barSpace
            float x = startBarX + i*(barWidth + barItemSpace) + moveTotal;
            float height = allMarkHeight * barBean.num/max  * animPro;
            //画柱子
            RectF rectF = new RectF(x,startPoint.y - height,x+barWidth,startPoint.y);
            canvas.drawRect(rectF,paintChart);
            //画柱子上的数字
            paintText.setTextAlign(Paint.Align.CENTER);
            float textCenter = rectF.left + (rectF.right - rectF.left)/2;
            float baseLine = rectF.top - barTextSpace - Math.abs(paintText.getFontMetrics().bottom);
            canvas.drawText(String.valueOf(barBean.num),textCenter,baseLine,paintText);
            //画对应的x轴坐标
            float XMarkBaseLine = startPoint.y + coordinateTextSpace + Math.abs(paintText.getFontMetrics().top);
            canvas.drawText(barBean.name,textCenter,XMarkBaseLine,paintText);
        }
        paintChart.setColor(coordinateTextColor);
        canvas.drawLine(startPoint.x,startPoint.y,rectContent.right,startPoint.y,paintChart);

        //画底部label
        if(labelList != null && labelList.size() > 0){
            float rectX = labelStartX ;
            float fontHeight = FontUtil.getFontHeight(paintText);
            for (int i = 0; i < labelList.size(); i++) {
                LabelBean labelBean = labelList.get(i);
                if(labelBean.color != 0){
                    paintChart.setColor(labelBean.color);
                }else {
                    paintChart.setColor(barColor);
                }
                //画矩形和文字
                RectF rectF;
                float baseLine;
                if(fontHeight < rectDimension){
                    rectF = new RectF(rectX,rectLabel.bottom - coordinateTextSpace - oneBottomLabelHeight,rectX + rectDimension,rectLabel.bottom - coordinateTextSpace);
                    baseLine = rectF.top + (oneBottomLabelHeight - fontHeight)/2 + Math.abs(paintText.getFontMetrics().top);
                }else{
                    rectF = new RectF(rectX,rectLabel.bottom - coordinateTextSpace - oneBottomLabelHeight + (oneBottomLabelHeight - rectDimension)/2,rectX + rectDimension,rectLabel.bottom - coordinateTextSpace - oneBottomLabelHeight + (oneBottomLabelHeight - rectDimension)/2 + rectDimension);
                    baseLine = rectLabel.bottom - coordinateTextSpace - Math.abs(paintText.getFontMetrics().bottom);
                }
                canvas.drawRect(rectF,paintChart);
                paintText.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(labelBean.name,rectF.right + labelSpace,baseLine,paintText);

                rectX += (rectDimension + labelSpace + FontUtil.getFontlength(paintText,labelBean.name) + labelItemSpace);
            }
        }

        //画Y轴上的东西(放在最后面 是因为这样画才能遮挡覆盖住柱子因为左移而超出的问题)
        paintChart.setColor(themeBackgroundColor);
        canvas.drawRect(new RectF(rectContent.left,rectContent.top,startPoint.x,rectLabel.top),paintChart);
        for (int i = 1; i <= YMarkCount; i++) {
            float y = startPoint.y - i * oneMarkHeight;
            //画辅助线
            paintChart.setColor(coordinateTextColor);
            canvas.drawLine(startPoint.x,y,rectContent.right,y,paintChart);
            //画Y轴坐标
            String text = String.valueOf(i * item);
            float textX = startPoint.x - coordinateTextSpace - FontUtil.getFontlength(paintText,text);
            float baseLine = y - FontUtil.getFontHeight(paintText)/2 + Math.abs(paintText.getFontMetrics().top);
            canvas.drawText(text,textX,baseLine,paintText);
            //画0坐标
            if(i == 1){
                baseLine += oneMarkHeight;
                canvas.drawText(String.valueOf(0),textX,baseLine,paintText);
            }
        }
        canvas.drawLine(startPoint.x,startPoint.y,startPoint.x,rectContent.top + oneMarkHeight/2,paintChart); //高度的话 等于YMarkCount数量个刻度 再加上半个  这样显得美观，不会有顶到头部的感觉。
    }

    private GestureDetector gestureDetector = new GestureDetector(getContext(),new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(Math.abs(distanceX) > Math.abs(distanceY)){
                onMove(-distanceX);
                return true;
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(Math.abs(velocityX) > Math.abs(velocityY)){
                BarChart.this.onFling(velocityX/100);
                return true;
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    });

    //手指抬起后的滑动
    private void onFling(float distanceX) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(distanceX, 0);
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

    private void onMove(float distanceX) {
        if(startBarX + moveTotal + distanceX >= startBarX){ //右滑到底
            moveTotal = 0;
        }else if(startBarX + moveTotal + distanceX <= startBarX + minX) {
            moveTotal = minX;
        }else {
            moveTotal += distanceX;
        }
        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(minX!=startBarX && region.contains((int)event.getX(),(int)event.getY())){
            return gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    private void test(Canvas canvas) {
        paintChart.setStyle(Paint.Style.STROKE);
        canvas.drawRect(rectContent,paintChart);
        if(labelList != null && labelList.size() > 0){
            canvas.drawRect(rectLabel,paintChart);
        }
        paintChart.setStrokeWidth(20);
        paintChart.setColor(Color.RED);
        canvas.drawPoint(startPoint.x,startPoint.y,paintChart);
    }

    public int getCoordinateTextSpace() {
        return coordinateTextSpace;
    }

    public void setCoordinateTextSpace(int coordinateTextSpace) {
        this.coordinateTextSpace = coordinateTextSpace;
    }

    public int getCoordinateTextColor() {
        return coordinateTextColor;
    }

    public void setCoordinateTextColor(int coordinateTextColor) {
        this.coordinateTextColor = coordinateTextColor;
    }

    public int getCoordinateTextSize() {
        return coordinateTextSize;
    }

    public void setCoordinateTextSize(int coordinateTextSize) {
        this.coordinateTextSize = coordinateTextSize;
    }

    public int getBarColor() {
        return barColor;
    }

    public void setBarColor(int barColor) {
        this.barColor = barColor;
    }

    public int getBarWidth() {
        return barWidth;
    }

    public void setBarWidth(int barWidth) {
        this.barWidth = barWidth;
    }

    public int getLabelMarginTop() {
        return labelMarginTop;
    }

    public void setLabelMarginTop(int labelMarginTop) {
        this.labelMarginTop = labelMarginTop;
    }

    public int getLabelSpace() {
        return labelSpace;
    }

    public void setLabelSpace(int labelSpace) {
        this.labelSpace = labelSpace;
    }

    public int getLabelItemSpace() {
        return labelItemSpace;
    }

    public void setLabelItemSpace(int labelItemSpace) {
        this.labelItemSpace = labelItemSpace;
    }

    public int getRectDimension() {
        return rectDimension;
    }

    public void setRectDimension(int rectDimension) {
        this.rectDimension = rectDimension;
    }

    public int getBarSpace() {
        return barSpace;
    }

    public void setBarSpace(int barSpace) {
        this.barSpace = barSpace;
    }

    public int getBarItemSpace() {
        return barItemSpace;
    }

    public void setBarItemSpace(int barItemSpace) {
        this.barItemSpace = barItemSpace;
    }

    public int getBarTextSpace() {
        return barTextSpace;
    }

    public void setBarTextSpace(int barTextSpace) {
        this.barTextSpace = barTextSpace;
    }

    public int getYMarkCount() {
        return YMarkCount;
    }

    public void setYMarkCount(int YMarkCount) {
        this.YMarkCount = YMarkCount;
    }

    public List<LabelBean> getLabelList() {
        return labelList;
    }

    public void setLabelList(List<LabelBean> labelList) {
        this.labelList = labelList;
    }

    public long getAnimDuration() {
        return animDuration;
    }

    public void setAnimDuration(long animDuration) {
        this.animDuration = animDuration;
    }
}
