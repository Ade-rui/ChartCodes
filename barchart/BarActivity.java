package com.ryrj.testgit.mychart.barchart;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ryrj.testgit.R;
import com.ryrj.testgit.utils.DensityUtil;

import java.util.ArrayList;
import java.util.Random;

public class BarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar);
        BarChart viewById = (BarChart) findViewById(R.id.bar_chart);
        ArrayList<BarBean> barBeen = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            barBeen.add(new BarBean("数据" + i,new Random().nextInt(20)));
        }
        ArrayList<LabelBean> labelBeen = new ArrayList<>();
        labelBeen.add(new LabelBean("员工姓名"));
        labelBeen.add(new LabelBean("单位：元"));
        viewById.setLabelList(labelBeen);
        viewById.setBarItemSpace(DensityUtil.dip2px(this,30));
        viewById.setCoordinateTextColor(Color.BLACK);
        viewById.setDataList(barBeen);
    }
}
