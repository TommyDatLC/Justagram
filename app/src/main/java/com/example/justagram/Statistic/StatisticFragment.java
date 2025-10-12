package com.example.justagram.Statistic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.justagram.R;
import com.example.justagram.etc.DateTime;
import com.example.justagram.etc.Utility;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StatisticFragment extends Fragment {

    // Tu dong them vao mang

    ArrayList<StatisticData> AvalableRequest = new ArrayList<StatisticData>();
    void InitAvalableRequest()
    {
// Reusable arrays
        EnumTimeFrame[] TF_ALL_RANGE = new EnumTimeFrame[] {
                EnumTimeFrame.last_14_days,
                EnumTimeFrame.last_30_days,
                EnumTimeFrame.last_90_days,
                EnumTimeFrame.prev_month,
                EnumTimeFrame.this_month,
                EnumTimeFrame.this_week
        };

        EnumBreakDown[] BD_AGE_CITY_COUNTRY_GENDER = new EnumBreakDown[] {
                EnumBreakDown.age,
                EnumBreakDown.city,
                EnumBreakDown.country,
                EnumBreakDown.gender
        };

        EnumBreakDown[] BD_MEDIA_PRODUCT_TYPE = new EnumBreakDown[] {
                EnumBreakDown.media_product_type
        };

        EnumBreakDown[] BD_FOLLOW_TYPE = new EnumBreakDown[] {
                EnumBreakDown.follow_type
        };

        EnumBreakDown[] BD_CONTACT_BUTTON = new EnumBreakDown[] {
                EnumBreakDown.contact_button_type
        };

        EnumMetricType[] MT_TOTAL = new EnumMetricType[] {
                EnumMetricType.total_value
        };

        EnumMetricType[] MT_TIME_SERIES = new EnumMetricType[] {
                EnumMetricType.time_series
        };

        EnumMetricType[] MT_BOTH = new EnumMetricType[] {
                EnumMetricType.total_value,
                EnumMetricType.time_series
        };

// 1 accounts_engaged
        AvalableRequest.add(new StatisticData(
                "Account engaged",
                "accounts_engaged",
                "day",
                null,
                null,
                MT_TOTAL
        ));

// 2 comments
        AvalableRequest.add(new StatisticData(
                "Comments",
                "comments",
                "day",
                null,
                BD_MEDIA_PRODUCT_TYPE,
                MT_TOTAL
        ));

// 3 engaged_audience_demographics
        AvalableRequest.add(new StatisticData(
                "Engaged audience demographics",
                "engaged_audience_demographics",
                "lifetime",
                TF_ALL_RANGE,
                BD_AGE_CITY_COUNTRY_GENDER,
                MT_TOTAL
        ));

// 4 follows_and_unfollows
        AvalableRequest.add(new StatisticData(
                "Follows and unfollows",
                "follows_and_unfollows",
                "day",
                null,
                BD_FOLLOW_TYPE,
                MT_TOTAL
        ));

// 5 follower_demographics
        AvalableRequest.add(new StatisticData(
                "Follower demographics",
                "follower_demographics",
                "lifetime",
                TF_ALL_RANGE,
                BD_AGE_CITY_COUNTRY_GENDER,
                MT_TOTAL
        ));

// 6 impressions (deprecated per table)
        AvalableRequest.add(new StatisticData(
                "Impressions (deprecated)",
                "impressions",
                "day",
                null,
                null,
                MT_BOTH
        ));

// 7 likes
        AvalableRequest.add(new StatisticData(
                "Likes",
                "likes",
                "day",
                null,
                BD_MEDIA_PRODUCT_TYPE,
                MT_TOTAL
        ));

// 8 profile_links_taps
        AvalableRequest.add(new StatisticData(
                "Profile links taps",
                "profile_links_taps",
                "day",
                null,
                BD_CONTACT_BUTTON,
                MT_TOTAL
        ));

// 9 reach
        AvalableRequest.add(new StatisticData(
                "Reach",
                "reach",
                "day",
                null,
                new EnumBreakDown[] { EnumBreakDown.media_product_type, EnumBreakDown.follow_type },
                MT_BOTH
        ));

// 10 replies
        AvalableRequest.add(new StatisticData(
                "Replies",
                "replies",
                "day",
                null,
                null,
                MT_TOTAL
        ));

// 11 saved
        AvalableRequest.add(new StatisticData(
                "Saved",
                "saved",
                "day",
                null,
                BD_MEDIA_PRODUCT_TYPE,
                MT_TOTAL
        ));

// 12 shares
        AvalableRequest.add(new StatisticData(
                "Shares",
                "shares",
                "day",
                null,
                BD_MEDIA_PRODUCT_TYPE,
                MT_TOTAL
        ));

// 13 total_interactions
        AvalableRequest.add(new StatisticData(
                "Total interactions",
                "total_interactions",
                "day",
                null,
                BD_MEDIA_PRODUCT_TYPE,
                MT_TOTAL
        ));

// 14 views
        AvalableRequest.add(new StatisticData(
                "Views",
                "views",
                "day",
                null,
                // Table lists follower_type and media_product_type; enum has follow_type so we use that.
                new EnumBreakDown[] { EnumBreakDown.follow_type, EnumBreakDown.media_product_type },
                MT_TOTAL
        ));

    }
    View thisLayout;
    Spinner spn_metric,spn_timeFrame,spn_breakDown,spn_metricType;
    BarChart barchart_totalvalue ;
    LineChart linechart_totalvalue ;
    TextView txtview_totalvalue ;
    TextView txtview_totaltitle ;
    LinearLayout card_ElementValueInfoDisplay;
    View btn_refresh;
    private Boolean checkValidTime(DateTime t) {
        long since = SinceTime.ConvertToUnixTime();
        long until = UntilTime.ConvertToUnixTime();
        long now = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis() / 1000;

        boolean isTimeRangeValid = (since <= until && since <= now && until <= now);



        if (!isTimeRangeValid) {
            Utility.showMessageBox("The time you have set does not valid", getContext());
        }

        return isTimeRangeValid;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        thisLayout = inflater.inflate(R.layout.fragment_statistic,container,false);

        card_ElementValueInfoDisplay = thisLayout.findViewById(R.id.container_ValueInfoDisplay);
        btn_refresh = thisLayout.findViewById(R.id.btn_refresh);
        barchart_totalvalue = thisLayout.findViewById(R.id.grph_TotalValue);
        txtview_totalvalue = thisLayout.findViewById(R.id.txtview_totalvalue);
        txtview_totaltitle = thisLayout.findViewById(R.id.txtview_totaltitle);
        linechart_totalvalue = thisLayout.findViewById(R.id.grph_TimeSeries);
        TextView txtview_ChooseDateSince = thisLayout.findViewById(R.id.txtview_dateSelectorSince),
                txtview_ChooseDateUntil = thisLayout.findViewById(R.id.txtview_dateSelectorUntil);

        TextView txtview_ChooseTimeSince= thisLayout.findViewById(R.id.txtview_timeSelectorSince)
                ,txtview_ChooseTimeUntil = thisLayout.findViewById(R.id.txtview_timeSelectorUntil);
             spn_metric = thisLayout.findViewById(R.id.spn_Metric);
             spn_timeFrame = thisLayout.findViewById(R.id.spn_timeframe);
             spn_breakDown = thisLayout.findViewById(R.id.spn_breakdown);
             spn_metricType = thisLayout.findViewById(R.id.spn_metricType);
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSpinnerChoosen(null);
            }
        });
        txtview_ChooseDateSince.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTime.OpenDateSelector(getContext(),SinceTime);
            }
        });
        txtview_ChooseDateUntil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTime.OpenDateSelector(getContext(),UntilTime);
            }
        });
        txtview_ChooseTimeSince.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTime.OpenTimeSelector(getContext(),SinceTime);
            }
        });
        txtview_ChooseTimeUntil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTime.OpenTimeSelector(getContext(),UntilTime);
            }
        });
        SinceTime.onTimeChange = (a) -> {
            txtview_ChooseTimeSince.setText(SinceTime.GetTimeString());
            txtview_ChooseDateSince.setText(SinceTime.GetDateString());
        };
        UntilTime.onTimeChange= (a) ->
        {
            txtview_ChooseTimeUntil.setText(UntilTime.GetTimeString());
            txtview_ChooseDateUntil.setText(UntilTime.GetDateString());
        };
        SinceTime.onDateChange = (a) -> {
            txtview_ChooseTimeSince.setText(SinceTime.GetTimeString());
            txtview_ChooseDateSince.setText(SinceTime.GetDateString());
        };
        UntilTime.onDateChange = (a) ->
        {
            txtview_ChooseTimeUntil.setText(UntilTime.GetTimeString());
            txtview_ChooseDateUntil.setText(UntilTime.GetDateString());
        };
        SinceTime.SetToCurrentTime();
        UntilTime.SetToCurrentTime();
        SinceTime.setDay(SinceTime.getDay() - 1);
        SinceTime.isValid = (d) -> checkValidTime(d);
        UntilTime.isValid = (d) -> checkValidTime(d);
        AttachEventSpinner(spn_metric,true);
        AttachEventSpinner(spn_metricType,false);
        AttachEventSpinner(spn_breakDown,false);
        AttachEventSpinner(spn_timeFrame,false);
        InitAvalableRequest();
        // Get all the statistic data
        List<String> allMetricTitle = AvalableRequest.stream().map(item -> item.title).collect(Collectors.toList());
        SetSpinnerList(allMetricTitle,R.id.spn_Metric);
        LoadNewMetric(0);
        return thisLayout;

        // make a translater
        // Listen to event on spn_Metric change
        // when change send the request to the server
        // when change spn_Metric change all the timeFrame,spn_breakdown,spn_metricType
        // make the time selection working
        // Rendering Data out of the hand
            // Rendering Total value data
            // Rendering Time Series data
        // Write all the value of the
    }
    void AttachEventSpinner(Spinner spn,boolean isMetricSpinner)
    {
        var onSelect = new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isMetricSpinner)
                    LoadNewMetric(position);
                else
                    onSpinnerChoosen(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        spn.setOnItemSelectedListener(onSelect);
    }
    void SetSpinnerList(List<String> list,int id)
    {
        Spinner spinner = thisLayout.findViewById(id);
        if (list == null || list.isEmpty()) {
            spinner.setAdapter(null);
            return;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                R.layout.custom_dropdown_textview,
                list
        );
        adapter.setDropDownViewResource(R.layout.custom_dropdown_textview);
        spinner.setAdapter(adapter);

    }
    DateTime SinceTime = new DateTime()  ,
            UntilTime = new DateTime();



    void onSpinnerChoosen(StatisticData metricData) {


        int metricPos = spn_metric.getSelectedItemPosition();
        int timeframePos = spn_timeFrame.getSelectedItemPosition();
        int breakdownPos = spn_breakDown.getSelectedItemPosition();
        int metricTypePos = spn_metricType.getSelectedItemPosition();

        StatisticData finalMetricData = (metricData != null) ? metricData : AvalableRequest.get(metricPos);

        if (finalMetricData.metricTypes == null || finalMetricData.metricTypes.length == 0 || spn_metricType.getAdapter() == null) {
            barchart_totalvalue.setVisibility(View.GONE);
            linechart_totalvalue.setVisibility(View.GONE);
            return;
        }

        EnumMetricType selectedType = finalMetricData.metricTypes[metricTypePos];

        Consumer<Object> onFinishCallback = (a) -> {
            thisLayout.post(() -> {
                if (selectedType == EnumMetricType.total_value) {

                    ((View)linechart_totalvalue.getParent().getParent()).setVisibility(View.GONE);
                    ((View)barchart_totalvalue.getParent().getParent()).setVisibility(View.VISIBLE);
                    finalMetricData.DrawInTotalValue(barchart_totalvalue, txtview_totaltitle, txtview_totalvalue, card_ElementValueInfoDisplay);
                } else { // time_series
                    ((View)barchart_totalvalue.getParent().getParent()).setVisibility(View.GONE);
                    ((View)linechart_totalvalue.getParent().getParent()).setVisibility(View.VISIBLE);
                    finalMetricData.DrawInTimeSeries(linechart_totalvalue, txtview_totaltitle, txtview_totalvalue, card_ElementValueInfoDisplay);
                }
            });
        };

        finalMetricData.SendRequest(thisLayout,
                timeframePos,
                breakdownPos,
                metricTypePos,
                SinceTime.ConvertToUnixTime(),
                UntilTime.ConvertToUnixTime(),
                false,
                onFinishCallback);
    }

    void LoadNewMetric(int newIndex)
    {

        StatisticData metricReqInfo =  AvalableRequest.get(newIndex);

        TranslateAndSetSpinnerList(spn_breakDown,metricReqInfo.breakDowns,
                (obj) -> StatisticRequestUtils.TranslateEnumBreakDownToRequest((EnumBreakDown)obj));
        TranslateAndSetSpinnerList(spn_timeFrame,metricReqInfo.timeFrames,
                (obj) -> StatisticRequestUtils.TranslateEnumTimeFrameToRequest((EnumTimeFrame)obj));
        TranslateAndSetSpinnerList(spn_metricType,metricReqInfo.metricTypes,
                (obj) -> StatisticRequestUtils.TranslateEnumMetricTypeToRequest((EnumMetricType) obj));
        onSpinnerChoosen(metricReqInfo);

    }
    void TranslateAndSetSpinnerList(Spinner spinner,Object[] arr, Function<Object,String> translater)
    {
        if (arr != null)
        {
            spinner.setVisibility(View.VISIBLE);
            List<String> newMetrixType = Arrays.stream(arr).map(items -> translater.apply(items)).collect(Collectors.toList());
            SetSpinnerList(newMetrixType,spinner.getId());
        }
        else
        {
            spinner.setVisibility(View.GONE);
        }
    }

}
