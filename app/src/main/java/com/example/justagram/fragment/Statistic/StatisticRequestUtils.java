package com.example.justagram.fragment.Statistic;
enum EnumTimeFrame
{
    last_14_days, last_30_days, last_90_days, prev_month, this_month, this_week
}
enum EnumBreakDown
{

    media_product_type,
    follow_type,
    contact_button_type,
    age,
    city,
    country,
    gender
}

enum EnumMetricType
{
    total_value,
    time_series
}
public class StatisticRequestUtils {
   public static String TranslateEnumMetricTypeToRequest(EnumMetricType m)
    {
        switch (m)
        {
            case total_value:
                return "Total Value";
            case time_series:
                return "Time Series";
        }
        return "";
    }
    public static String TranslateEnumBreakDownToRequest(EnumBreakDown b)
    {
        switch (b)
        {
            case media_product_type:
                return "Media product type";
            case follow_type:
                return "Follow Type";
            case contact_button_type:
                return "Contact Button Type";
            case age:
                return "Age";
            case city:
                return "City";
            case country:
                return "Country";
            case gender:
                return "Gender";
        }
        return "";
    }

    public static String TranslateEnumTimeFrameToRequest(EnumTimeFrame t)
    {
        switch (t)
        {
            case last_14_days:
                return "Last 14 days";
            case last_30_days:
                return "Last 30 Days";
            case last_90_days:
                return "Last 90 Days";
            case prev_month:
                return "Previous Month";
            case this_month:
                return "This Month";
            case this_week:
                return "This week";
        }
        return "";
    }
}
