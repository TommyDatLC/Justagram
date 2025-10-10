package com.example.justagram.Statistic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.justagram.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticFragment extends Fragment {

    // Tu dong them vao mang

    ArrayList<StatisticData> AvalableRequest = new ArrayList<StatisticData>();
    void InitAvalableRequest()
    {
        AvalableRequest.add(new StatisticData("Account engaged","accounts_engaged",
                null,
                null ,
                new EnumMetricType[] { EnumMetricType.total_value }));

        AvalableRequest.add(new StatisticData("Comments","comments",
                null,
                new EnumBreakDown[] { EnumBreakDown.media_product_type },
                new EnumMetricType[] { EnumMetricType.total_value }));

        AvalableRequest.add(new StatisticData("Engaged audience demographics","engaged_audience_demographics",
                new EnumTimeFrame[] { EnumTimeFrame.last_14_days, EnumTimeFrame.last_30_days, EnumTimeFrame.last_90_days, EnumTimeFrame.prev_month, EnumTimeFrame.this_month, EnumTimeFrame.this_week },
                new EnumBreakDown[] { EnumBreakDown.age, EnumBreakDown.city, EnumBreakDown.country, EnumBreakDown.gender },
                new EnumMetricType[] { EnumMetricType.total_value }));

        AvalableRequest.add(new StatisticData("Follows and unfollows","follows_and_unfollows",
                null,
                new EnumBreakDown[] { EnumBreakDown.follow_type },
                new EnumMetricType[] { EnumMetricType.total_value }));

        AvalableRequest.add(new StatisticData("Follower demographics","follower_demographics",
                new EnumTimeFrame[] { EnumTimeFrame.last_14_days, EnumTimeFrame.last_30_days, EnumTimeFrame.last_90_days, EnumTimeFrame.prev_month, EnumTimeFrame.this_month, EnumTimeFrame.this_week },
                new EnumBreakDown[] { EnumBreakDown.age, EnumBreakDown.city, EnumBreakDown.country, EnumBreakDown.gender },
                new EnumMetricType[] { EnumMetricType.total_value }));

        AvalableRequest.add(new StatisticData("Impressions","impressions",
                null,
                null,
                new EnumMetricType[] { EnumMetricType.total_value, EnumMetricType.time_series }));

        AvalableRequest.add(new StatisticData("Likes","likes",
                null,
                new EnumBreakDown[] { EnumBreakDown.media_product_type },
                new EnumMetricType[] { EnumMetricType.total_value }));

        AvalableRequest.add(new StatisticData("Profile links taps","profile_links_taps",
                null,
                new EnumBreakDown[] { EnumBreakDown.contact_button_type },
                new EnumMetricType[] { EnumMetricType.total_value }));

        AvalableRequest.add(new StatisticData("Reach","reach",
                null,
                new EnumBreakDown[] { EnumBreakDown.media_product_type, EnumBreakDown.follow_type },
                new EnumMetricType[] { EnumMetricType.total_value, EnumMetricType.time_series }));

        AvalableRequest.add(new StatisticData("Replies","replies",
                null,
                null,
                new EnumMetricType[] { EnumMetricType.total_value }));

        AvalableRequest.add(new StatisticData("Saved","saved",
                null,
                new EnumBreakDown[] { EnumBreakDown.media_product_type },
                new EnumMetricType[] { EnumMetricType.total_value }));

        AvalableRequest.add(new StatisticData("Shares","shares",
                null,
                new EnumBreakDown[] { EnumBreakDown.media_product_type },
                new EnumMetricType[] { EnumMetricType.total_value }));

        AvalableRequest.add(new StatisticData("Total interactions","total_interactions",
                null,
                new EnumBreakDown[] { EnumBreakDown.media_product_type },
                new EnumMetricType[] { EnumMetricType.total_value }));

        AvalableRequest.add(new StatisticData("Views","views",
                null,
                new EnumBreakDown[] { EnumBreakDown.follow_type, EnumBreakDown.media_product_type },
                new EnumMetricType[] { EnumMetricType.total_value }));
    }
    View thisLayout;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        InitAvalableRequest();


        thisLayout = inflater.inflate(R.layout.fragment_statistic,container);
        var spn_metric = thisLayout.findViewById(R.id.spn_Metric);
        var spn_timeFrame = thisLayout.findViewById(R.id.spn_timeframe);
        var spn_breakDown = thisLayout.findViewById(R.id.spn_breakdown);
        var spn_metricType = thisLayout.findViewById(R.id.spn_metricType);
        // Get all the statistic data
        List<String> allMetricTitle = AvalableRequest.stream().map(item -> item.title).collect(Collectors.toList());
        SetSpinnerList(allMetricTitle,R.id.spn_Metric);
        return thisLayout;
        // Test this fragment
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
    void SetSpinnerList(List<String> list,int id)
    {
        Spinner spinner = thisLayout.findViewById(id);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                R.layout.fragment_statistic,
                list
        );

        adapter.setDropDownViewResource(R.layout.fragment_statistic);
        spinner.setAdapter(adapter);

    }
}
/* {
  "data": [
    {
      "name": "reach",
      "period": "day",
      "values": [
        {
          "value": 0,
          "end_time": "2025-10-08T07:00:00+0000"
        },
        {
          "value": 1,
          "end_time": "2025-10-09T07:00:00+0000"
        }
      ],
      "title": "Reach",
      "description": "The total number of times that the business account's media objects have been uniquely viewed.",
      "id": "17841474853201686/insights/reach/day"
    }
  ]
}
*/