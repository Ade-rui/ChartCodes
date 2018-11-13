package com.ryrj.testgit.mychart.progresschart;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ryrj.testgit.R;
import com.ryrj.testgit.utils.DensityUtil;

import java.util.ArrayList;

public class ProgressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        ProgressCircleChart viewById = (ProgressCircleChart) findViewById(R.id.progress_chart);
        viewById.setPer(0.6f);
        viewById.setLabelSpace(DensityUtil.dip2px(this,8));
        ArrayList<ChartLabel> chartLabels = new ArrayList<>();
        chartLabels.add(new ChartLabel("60%", DensityUtil.sp2px(this,18), Color.rgb(254,163,65)));
        chartLabels.add(new ChartLabel("完成率", DensityUtil.sp2px(this,10), Color.LTGRAY));
        viewById.setLabelList(chartLabels);
    }
}
