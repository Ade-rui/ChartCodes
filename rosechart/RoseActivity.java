package com.ryrj.testgit.mychart.rosechart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ryrj.testgit.R;
import com.ryrj.testgit.utils.DensityUtil;

import java.util.ArrayList;
import java.util.Random;

public class RoseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rose);
        NightingRosePieChart viewById = (NightingRosePieChart) findViewById(R.id.rose_chart);
        Random random = new Random();
        ArrayList<RoseBean> roseBeen = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            roseBeen.add(new RoseBean("数据"+(i+1),random.nextInt(20) + i));
        }
        viewById.setChartStyle(NightingRosePieChart.STYLE.STYLE_FIX);
        viewById.setDataList(roseBeen);
    }
}
