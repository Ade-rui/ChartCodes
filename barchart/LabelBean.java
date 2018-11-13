package com.ryrj.testgit.mychart.barchart;

/**
 * Created by wrf on 2018/9/28.
 */

public class LabelBean {

    public int color;
    public String name;

    public LabelBean() {
    }

    public LabelBean(String name) {
        this.name = name;
    }

    public LabelBean(int color, String name) {
        this.color = color;
        this.name = name;
    }
}
