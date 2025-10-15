package com.example.justagram.fragment.Statistic;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.justagram.LoginAuth.LoginActivity;
import com.example.justagram.R;
import com.example.justagram.etc.Utility;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Consumer;


public class StatisticData {

    public StatisticData(String title,String metric,String period, EnumTimeFrame[] timeFrames,EnumBreakDown[] breakDowns,EnumMetricType[] metricTypes)
    {
        this.title = title;
        this.metric = metric;
        this.period = period;
        this.timeFrames = timeFrames;
        this.breakDowns = breakDowns;
        this.metricTypes = metricTypes;
    }
    public String title;
    public String metric;
    public String period;
    public EnumTimeFrame[] timeFrames;
    public EnumBreakDown[] breakDowns;
    public EnumMetricType[] metricTypes;
    public Hashtable<String,Object> cache;
    int timeFrameID;
    int breakDownID;
    int MetricTypeID;
    boolean CanSendReq  =true   ;
    public void SendRequest(View ctx, int timeFrameID,int breakDownID, int MetricTypeID, long since, long until,boolean refresh
        ,Consumer<Object> onFinish
    )
    {
        this.timeFrameID = timeFrameID;
        this.breakDownID = breakDownID;
        this.MetricTypeID = MetricTypeID;
        if (CanSendReq )
        {
            CanSendReq = false;
            Thread t = new Thread(Utility.CreateRunnable((a) -> {
                try {
                    Thread.sleep(200);
                    CanSendReq = true;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }));
            t.start();
        }
        else
            return;

        String endpoint = "https://graph.instagram.com/v24.0/" + LoginActivity.userInfo.UserID  + "/insights";
        var Params = "?metric=" + metric;
        if (timeFrames != null)
            Params += "&timeframe=" + timeFrames[timeFrameID];
        if (breakDowns != null)
            Params += "&breakdown=" + breakDowns[breakDownID];
        if (metricTypes != null)
            Params += "&metric_type=" + metricTypes[MetricTypeID];
        Params += "&period=" + period +
                "&since=" + since + "&until=" + until +
                "&access_token=" + LoginActivity.userInfo.GetAccessToken();
        Utility.SimpleGetRequest(endpoint + Params, (json) -> {
            if ((int)json.get("request_code") > 299)
            {
                ctx.post(Utility.CreateRunnable((a) ->
                      Utility.showMessageBox("Fail to request for the statistic. Please check the log for more info",ctx.getContext() ))  );
            }
            else
            {
                cache = json;
                onFinish.accept(null);
            }
        });
    }
//{
//  "data": [
//    {
//      "name": "reach",
//      "period": "day",
//      "total_value": {
//        "value": 257,
//        "breakdowns": [
//          {
//            "dimension_keys": ["media_product_type"],
//            "results": [
//              {
//                "dimension_values": ["POST"],
//                "value": 5
//              },
//              {
//                "dimension_values": ["REEL"],
//                "value": 255
//              }
//            ]
//          }
//        ]
//      },
//      "id": "17841474853201686/insights/reach/day"
//    }
//  ]
//}
    public void DrawInTotalValue(BarChart chart, TextView totalTitle, TextView totalValue, LinearLayout ElementValueInfoDisplay) {
        if (cache == null) {
            return;
        }
        try {
            chart.post(() -> {
                ElementValueInfoDisplay.removeAllViews(); // remove all child on the value display
                chart.clear(); // clear all the chart data and invalidate

                ArrayList<Map<String, Object>> data = (ArrayList<Map<String, Object>>) cache.get("data");
                if (data == null || data.isEmpty()) {
                    DontHaveDataToShow(chart,totalValue);
                    return;
                }

                Map<String, Object> firstDataElement = data.get(0);
                totalTitle.setText("Total : " + title);
                // set the totalValue title to the title of the filter
                Map<String, Object> totalValueObj = (Map<String, Object>) firstDataElement.get("total_value");
                if (totalValueObj == null) return;

                ArrayList<Map<String, Object>> breakdowns = (ArrayList<Map<String, Object>>) totalValueObj.get("breakdowns");
                if (breakdowns == null || breakdowns.isEmpty()) return;

                Map<String, Object> firstBreakdown = breakdowns.get(0);
                ArrayList<String> dimensionKeys = (ArrayList<String>) firstBreakdown.get("dimension_keys");
                ArrayList<Map<String, Object>> results = (ArrayList<Map<String, Object>>) firstBreakdown.get("results");
                if (results == null || results.isEmpty()) {
                    DontHaveDataToShow(chart,totalValue);
                    return;
                }

                ArrayList<BarEntry> entries = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();
                ArrayList<Number> valuesList = new ArrayList<>();
                long totalSum = 0;
                int i = 0;

                for (Map<String, Object> result : results) {
                    // get the value
                    ArrayList<String> dimensionValues = (ArrayList<String>) result.get("dimension_values");
                    Number valueNumber = (Number) result.get("value");
                    int value = valueNumber.intValue();
                    totalSum += value;
                    valuesList.add(value);

                    StringBuilder labelBuilder = new StringBuilder();
                    for (int j = 0; j < dimensionKeys.size(); j++) {
                        labelBuilder.append(dimensionKeys.get(j)).append(": ").append(dimensionValues.get(j));
                        if (j < dimensionKeys.size() - 1) {
                            labelBuilder.append("\n");
                        }
                    }
                    labels.add(labelBuilder.toString());
                    entries.add(new BarEntry(i, value));
                    i++;
                }

                totalValue.setText(String.valueOf(totalSum));
                AddInfoIntoInfoDisplay(ElementValueInfoDisplay, labels, valuesList, totalSum);
                // set the text below the right bottom corner of this ,title not being show because we not set that yet
                BarDataSet dataSet = new BarDataSet(entries,this.title);
                dataSet.setValueTextSize(10f);

                BarData barData = new BarData(dataSet);
                barData.setBarWidth(0.5f);

                chart.setData(barData);
                chart.setDrawGridBackground(false);
                
                XAxis xAxis = chart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);
                xAxis.setGranularityEnabled(true);

                chart.getAxisLeft().setAxisMinimum(0f);
                chart.getDescription().setEnabled(false);
                // set space of the bar btwn the edge
                chart.setFitBars(true);
                chart.animateY(1000);
            });

        } catch (Exception e) {
            e.printStackTrace();
            chart.post(() -> Utility.showMessageBox("Failed to parse and display chart data.", chart.getContext()));
        }
    }

//{
//  "data": [
//    {
//      "name": "reach",
//      "period": "day",
//      "values": [
//        {
//          "value": 1,
//          "end_time": "2025-10-09T07:00:00+0000"
//        },
//        {
//          "value": 0,
//          "end_time": "2025-10-10T07:00:00+0000"
//        },
//        {
//          "value": 2,
//          "end_time": "2025-10-11T07:00:00+0000"
//        },
//        {
//          "value": 253,
//          "end_time": "2025-10-12T07:00:00+0000"
//        }
//      ],
//      "title": "Số người tiếp cận",
//      "description": "Tổng số lượt xem (duy nhất) đối tượng phương tiện của Tài khoản kinh doanh",
//      "id": "17841474853201686/insights/reach/day"
//    }
//  ]
//}
    public void DrawInTimeSeries(LineChart chart, TextView totalTitle, TextView totalValue, LinearLayout ElementValueInfoDisplay) {
        if (cache == null) {
            return;
        }
        try {
            chart.post(() -> {
                ElementValueInfoDisplay.removeAllViews();
                chart.clear();

                totalTitle.setText("Total : " + title);
                // get the array data in the first req
                ArrayList<Map<String, Object>> data = (ArrayList<Map<String, Object>>) cache.get("data");
                if (data == null || data.isEmpty()) {
                    DontHaveDataToShow(chart,totalValue);
                    return;
                }
                // get the first element in data
                Map<String, Object> firstDataElement = data.get(0);

                ArrayList<Map<String, Object>> values = (ArrayList<Map<String, Object>>) firstDataElement.get("values");
                if (values == null || values.isEmpty()) {
                    DontHaveDataToShow(chart,totalValue);
                    return;
                }

                ArrayList<Entry> entries = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();
                ArrayList<Number> valuesList = new ArrayList<>();
                long totalSum = 0;
                int i = 0;

                for (Map<String, Object> valueMap : values) {
                    Number valueNumber = (Number) valueMap.get("value");
                    float value = valueNumber.floatValue();
                    totalSum += value;
                    valuesList.add(value);

                    String endTime = (String) valueMap.get("end_time");
                    String label = endTime.substring(0, 10);

                    entries.add(new Entry(i, value));
                    labels.add(label);
                    i++;
                }

                totalValue.setText(String.valueOf(totalSum));
                AddInfoIntoInfoDisplay(ElementValueInfoDisplay, labels, valuesList, totalSum);

                LineDataSet dataSet = new LineDataSet(entries, this.title);
                dataSet.setValueTextSize(10f);

                LineData lineData = new LineData(dataSet);
                chart.setData(lineData);

                XAxis xAxis = chart.getXAxis();
                //set the lable of the graph
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1);
                xAxis.setGranularityEnabled(true);

                chart.getDescription().setEnabled(false);
                chart.getAxisLeft().setAxisMinimum(0f);
                chart.getAxisRight().mAxisMaximum = 0;
                chart.animateY(1000);
            });
        } catch (Exception e) {
            e.printStackTrace();
            chart.post(() -> Utility.showMessageBox("Failed to parse and display chart data.", chart.getContext()));
        }
    }
    void AddInfoIntoInfoDisplay(LinearLayout container, ArrayList<String> labels, ArrayList<Number> values, float totalSum) {
        if (container == null || labels == null || values == null || labels.size() != values.size()) {
            return;
        }
        // queueing a main thread message
        container.post(() -> {
            // remove all child view
            container.removeAllViews();
            if (totalSum == 0) return; // Avoid division by zero

            Context context = container.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            for (int i = 0; i < labels.size(); i++) {
                // Inflate the XML layout for the row
                View rowView = inflater.inflate(R.layout.items_statistic_request_value_display, container, false);

                // Find the views within the inflated layout
                TextView labelView = rowView.findViewById(R.id.statistic_label);
                ProgressBar progressBar = rowView.findViewById(R.id.statistic_progress_bar);
                TextView valueView = rowView.findViewById(R.id.statistic_value);
                TextView percentageView = rowView.findViewById(R.id.statistic_percentage);

                // Get the data for the current row
                String label = labels.get(i);
                float value = values.get(i).floatValue();
                int percentage = (int) ((value / totalSum) * 100);

                // Populate the views with data
                labelView.setText(label.replace("\n", " "));
                valueView.setText(String.valueOf(values.get(i).intValue()));
                percentageView.setText(String.format("(%d%%)", percentage));

                progressBar.setProgress(percentage);
                // You can still set the tint programmatically if needed
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFFFA500")));


                // Add the fully populated row to the container
                container.addView(rowView);
            }
        });
    }
    void DontHaveDataToShow(Chart chart,TextView totalValue)
    {
        totalValue.setText("0");
        chart.invalidate();
        // neu khong co data thi hien thong bao
        Utility.showMessageBox("No content to show",chart.getContext());
    }

}
