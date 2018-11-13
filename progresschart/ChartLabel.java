package com.ryrj.testgit.mychart.progresschart;

/**
 * Created by wrf on 2018/9/5.
 */

public class ChartLabel {

    private String text;
    private int textSize;
    private int textColor;

    public ChartLabel() {
    }

    public ChartLabel(String text, int textSize, int textColor) {
        this.text = text;
        this.textSize = textSize;
        this.textColor = textColor;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
}
