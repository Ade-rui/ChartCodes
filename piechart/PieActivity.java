package com.ryrj.testgit.mychart.piechart;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ryrj.testgit.R;
import com.ryrj.testgit.utils.DensityUtil;

import java.util.ArrayList;
import java.util.Random;

public class PieActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie2);

        PieChart viewById = (PieChart) findViewById(R.id.pie_chart);
        ArrayList<PieBean> objects = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            objects.add(new PieBean("数据"+i,new Random().nextInt(20)));
        }
        objects.add(new PieBean("没有",0));
        ArrayList<InnerLabelBean> innerLabelBeen = new ArrayList<>();
        innerLabelBeen.add(new InnerLabelBean("java", Color.GREEN, DensityUtil.sp2px(this,16)));
        innerLabelBeen.add(new InnerLabelBean("C++", Color.RED, DensityUtil.sp2px(this,16)));
        viewById.setInnerLabelBeanList(innerLabelBeen);
        viewById.setShowInnerChart(false);
        viewById.setShowZeroNum(false);
        viewById.setAnimDuration(2000);
        viewById.setDataList(objects);
    }
}
