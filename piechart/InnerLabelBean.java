package com.ryrj.testgit.mychart.piechart;

/**
 * Created by wrf on 2018/9/25.
 * 图标内圈中要显示的label的实体类
 */

public class InnerLabelBean {

    public String content;
    public int color;
    public int textSize;

    public InnerLabelBean() {
    }

    public InnerLabelBean(String content, int color, int textSize) {
        this.content = content;
        this.color = color;
        this.textSize = textSize;
    }
}
