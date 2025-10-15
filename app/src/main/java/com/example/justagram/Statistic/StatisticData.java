package com.example.justagram.Statistic;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.justagram.LoginAuth.LoginActivity;
import com.example.justagram.etc.Utility;
import com.github.mikephil.charting.charts.BarChart;
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

    public String title;
    public String metric;
    public String period;
    public EnumTimeFrame[] timeFrames;
    public EnumBreakDown[] breakDowns;
    public EnumMetricType[] metricTypes;
    public Hashtable<String, Object> cache;
    int timeFrameID;
    int breakDownID;
    int MetricTypeID;
    boolean CanSendReq = true;
    public StatisticData(String title, String metric, String period, EnumTimeFrame[] timeFrames, EnumBreakDown[] breakDowns, EnumMetricType[] metricTypes) {
        this.title = title;
        this.metric = metric;
        this.period = period;
        this.timeFrames = timeFrames;
        this.breakDowns = breakDowns;
        this.metricTypes = metricTypes;
    }

    public void SendRequest(View ctx, int timeFrameID, int breakDownID, int MetricTypeID, long since, long until, boolean refresh
            , Consumer<Object> onFinish
    ) {
        this.timeFrameID = timeFrameID;
        this.breakDownID = breakDownID;
        this.MetricTypeID = MetricTypeID;
        if (CanSendReq) {
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
        } else
            return;

        String endpoint = "https://graph.instagram.com/v24.0/" + LoginActivity.userInfo.UserID + "/insights";
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
            if ((int) json.get("request_code") > 299) {
                ctx.post(Utility.CreateRunnable((a) ->
                        Utility.showMessageBox("Fail to request for the statistic. Please check the log for more info", ctx.getContext())));
            } else {
                cache = json;
                onFinish.accept(null);
            }
        });
    }

    public void DrawInTotalValue(BarChart chart, TextView totalTitle, TextView totalValue, LinearLayout ElementValueInfoDisplay) {
        if (cache == null) {
            return;
        }
        try {
            chart.post(() -> {
                ElementValueInfoDisplay.removeAllViews();
                chart.clear();

                ArrayList<Map<String, Object>> data = (ArrayList<Map<String, Object>>) cache.get("data");
                if (data == null || data.isEmpty()) {
                    totalValue.setText("0");
                    chart.invalidate();
                    DontHaveDataToShow(chart.getContext());
                    return;
                }

                Map<String, Object> firstDataElement = data.get(0);
                totalTitle.setText("Total : " + title);

                Map<String, Object> totalValueObj = (Map<String, Object>) firstDataElement.get("total_value");
                if (totalValueObj == null) return;

                ArrayList<Map<String, Object>> breakdowns = (ArrayList<Map<String, Object>>) totalValueObj.get("breakdowns");
                if (breakdowns == null || breakdowns.isEmpty()) return;

                Map<String, Object> firstBreakdown = breakdowns.get(0);
                ArrayList<String> dimensionKeys = (ArrayList<String>) firstBreakdown.get("dimension_keys");
                ArrayList<Map<String, Object>> results = (ArrayList<Map<String, Object>>) firstBreakdown.get("results");
                if (results == null || results.isEmpty()) {
                    totalValue.setText("0");
                    chart.invalidate();
                    DontHaveDataToShow(chart.getContext());
                    return;
                }

                ArrayList<BarEntry> entries = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();
                ArrayList<Number> valuesList = new ArrayList<>();
                long totalSum = 0;
                int i = 0;

                for (Map<String, Object> result : results) {
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

                BarDataSet dataSet = new BarDataSet(entries, this.title);
                dataSet.setValueTextSize(10f);

                BarData barData = new BarData(dataSet);
                barData.setBarWidth(0.5f);

                chart.setData(barData);
                chart.getDescription().setEnabled(false);
                chart.setDrawGridBackground(false);

                XAxis xAxis = chart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);
                xAxis.setGranularityEnabled(true);

                chart.getAxisLeft().setAxisMinimum(0f);
                chart.getAxisRight().setEnabled(false);
                chart.setFitBars(true);
                chart.animateY(1000);
                chart.invalidate();
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
        container.post(() -> {
            container.removeAllViews();
            if (totalSum == 0) return; // Avoid division by zero

            for (int i = 0; i < labels.size(); i++) {
                String label = labels.get(i);
                float value = values.get(i).floatValue();
                int percentage = (int) ((value / totalSum) * 100);

                LinearLayout rowLayout = new LinearLayout(container.getContext());
                rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setPadding(20, 10, 20, 10);

                TextView labelView = new TextView(container.getContext());
                LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3f);
                labelView.setLayoutParams(labelParams);
                labelView.setText(label.replace("\n", " "));
                labelView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                rowLayout.addView(labelView);

                ProgressBar progressBar = new ProgressBar(container.getContext(), null, android.R.attr.progressBarStyleHorizontal);
                LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 7f);
                progressParams.gravity = Gravity.CENTER_VERTICAL;
                progressBar.setLayoutParams(progressParams);
                progressBar.setMax(100);
                progressBar.setProgress(percentage);
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFFFA500")));
                rowLayout.addView(progressBar);

                TextView valueView = new TextView(container.getContext());
                LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f);
                valueParams.gravity = Gravity.CENTER;
                valueView.setLayoutParams(valueParams);
                valueView.setText(String.valueOf(values.get(i).intValue()));
                valueView.setGravity(Gravity.CENTER);
                rowLayout.addView(valueView);

                TextView percentageView = new TextView(container.getContext());
                LinearLayout.LayoutParams percentageParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f);
                percentageParams.gravity = Gravity.CENTER;
                percentageView.setLayoutParams(percentageParams);
                percentageView.setText(String.format("(%d%%)", percentage));
                percentageView.setGravity(Gravity.CENTER);
                rowLayout.addView(percentageView);

                container.addView(rowLayout);
            }
        });
    }

    public void DrawInTimeSeries(LineChart chart, TextView totalTitle, TextView totalValue, LinearLayout ElementValueInfoDisplay) {
        if (cache == null) {
            return;
        }
        try {
            chart.post(() -> {
                ElementValueInfoDisplay.removeAllViews();
                chart.clear();

                totalTitle.setText("Total : " + title);
                ArrayList<Map<String, Object>> data = (ArrayList<Map<String, Object>>) cache.get("data");
                if (data == null || data.isEmpty()) {
                    totalValue.setText("0");
                    chart.invalidate();
                    DontHaveDataToShow(chart.getContext());
                    return;
                }

                Map<String, Object> firstDataElement = data.get(0);

                ArrayList<Map<String, Object>> values = (ArrayList<Map<String, Object>>) firstDataElement.get("values");
                if (values == null || values.isEmpty()) {
                    totalValue.setText("0");
                    chart.invalidate();
                    DontHaveDataToShow(chart.getContext());
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
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);
                xAxis.setGranularityEnabled(true);

                chart.getDescription().setEnabled(false);
                chart.getAxisLeft().setAxisMinimum(0f);
                chart.getAxisRight().setEnabled(false);
                chart.animateY(1000);
                chart.invalidate();
            });
        } catch (Exception e) {
            e.printStackTrace();
            chart.post(() -> Utility.showMessageBox("Failed to parse and display chart data.", chart.getContext()));
        }
    }

    void DontHaveDataToShow(Context ctx) {
        Utility.showMessageBox("No content to show", ctx);
    }
}
