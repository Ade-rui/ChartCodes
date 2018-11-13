package com.ryrj.testgit.mychart.linechart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ryrj.testgit.R;
import com.ryrj.testgit.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LineActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line);
        LineChart viewById = (LineChart) findViewById(R.id.line_chart);

        ArrayList<List<LineBean>> lists = new ArrayList<List<LineBean>>();
        for (int i = 0; i < 2; i++) {
            ArrayList<LineBean> lineBeen = new ArrayList<>();
            for (int i1 = 0; i1 < 10; i1++) {
                lineBeen.add(new LineBean("数据" + i1,new Random().nextInt(20)));
            }
            lists.add(lineBeen);
        }
        viewById.setPointSpace(DensityUtil.dip2px(this,100));
        viewById.setDataList(lists);

    }
}
