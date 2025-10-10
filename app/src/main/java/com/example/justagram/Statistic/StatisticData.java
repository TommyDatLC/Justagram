package com.example.justagram.Statistic;


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
public class StatisticData {

    public StatisticData(String title,String RequestQuery, EnumTimeFrame[] timeFrames,EnumBreakDown[] breakDowns,EnumMetricType[] metricTypes)
    {
        this.title = title;
        this.RequestQuery = RequestQuery;
        this.timeFrames = timeFrames;
        this.breakDowns = breakDowns;
        this.metricTypes = metricTypes;
    }
    public String title;
    public String RequestQuery;
    public EnumTimeFrame[] timeFrames;
    public EnumBreakDown[] breakDowns;
    public EnumMetricType[] metricTypes;

}


/*<table class="_4-ss _5k9x"><thead><tr><th style="width:15%">
          Metric
        </th><th style="width:10%">
          Period
        </th><th style="width:13%">
          Timeframe
        </th><th style="width:11%">
          Breakdown
        </th><th style="width:12%">
          Metric Type
        </th><th>
          Description
        </th></tr></thead><tbody class="_5m37" id="u_0_4_c1"><tr class="row_0"><td><p><code>accounts_engaged</code></p>
</td><td><p><code>day</code></p>
</td><td><p>n/a</p>
</td><td><p>n/a</p>
</td><td><p><code>total_value</code></p>
</td><td><p>The number of accounts that have interacted with your content, including in ads. Content includes posts, stories, reels, videos and live videos. Interactions can include actions such as likes, saves, comments, shares or replies.</p>
<br><p>This metric is estimated.</p>
</td></tr><tr class="row_1 _5m29"><td><p><code>comments</code></p>
</td><td><p><code>day</code></p>
</td><td><p>n/a</p>
</td><td><p><code>media_product_type</code></p>
</td><td><p><code>total_value</code></p>
</td><td><p>The number of comments on your posts, reels, videos and live videos.</p>
<br><p>This metric is <a href="https://business.facebook.com/business/help/metrics-labeling" data-auto-logging-id="fe5263f37">in development</a>.</p>
</td></tr><tr class="row_2"><td><p><code>engaged_audience_demographics</code></p>
</td><td><p><code>lifetime</code></p>
</td><td><p>One of:</p>
<br><p><code>last_14_days</code>,
<code>last_30_days</code>,
<code>last_90_days</code>,
<code>prev_month</code>,
<code>this_month</code>,
<code>this_week</code></p>
</td><td><p><code>age</code>,
<br><code>city</code>,
<br><code>country</code>,
<br><code>gender</code></p>
</td><td><p><code>total_value</code></p>
</td><td><p>The demographic characteristics of the engaged audience, including countries, cities and gender distribution.  <code>this_month</code>Return the data in the last 30 days and  <code>this_week</code>Return data in the last 7 days.</p>
<br><p>Does not support  <code>since</code>or <code>until</code>. See <a href="#range-2" data-auto-logging-id="f5fde3ac3">Range</a> for more information.</p>
<br><p>Not returned if the IG User has less than 100 engagements during the timeframe.</p>
<br><p><strong>Note:</strong> The <code>last_14_days</code>, <code>last_30_days</code>,  <code>last_90_days</code>and  <code>prev_month</code>timeframes will no longer be be supported with v20.0. See the <a href="/docs/instagram-api/changelog#may-21--2024" data-auto-logging-id="fba52c7a5">changelog</a> for more information.</p>
</td></tr><tr class="row_3 _5m29"><td><p><code>follows_and_unfollows</code></p>
</td><td><p><code>day</code></p>
</td><td><p>n/a</p>
</td><td><p><code>follow_type</code></p>
</td><td><p><code>total_value</code></p>
</td><td><p>The number of accounts that follow you and the number of accounts that unfollowed you or left Instagram in the selected time period.</p>
<br><p>Not returned if the IG User has less than 100 followers.</p>
</td></tr><tr class="row_4"><td><p><code>follower_demographics</code></p>
</td><td><p><code>lifetime</code></p>
</td><td><p>One of:</p>
<br><p><code data-moz-translations-id="0">last_14_days</code>,
<code data-moz-translations-id="1">last_30_days</code>,
<code data-moz-translations-id="2">last_90_days</code>,
<code data-moz-translations-id="3">prev_month</code>,
<code data-moz-translations-id="4">this_month</code>,
<code data-moz-translations-id="5">this_week</code></p>
</td><td><p><code>age</code>,
<br><code>city</code>,
<br><code>country</code>,
<br><code>gender</code></p>
</td><td><p><code>total_value</code></p>
</td><td><p>The demographic characteristics of followers, including countries, cities and gender distribution.</p>
<br><p>Does not support  <code>since</code>or <code>until</code>. See <a href="#range-2" data-auto-logging-id="f3921b370">Range</a> for more information.</p>
<br><p>Not returned if the IG User has less than 100 followers.</p>
</td></tr><tr class="row_5 _5m29"><td><p><code>impressions</code> <strong>Deprecated for v22.0+ and all versions April 21, 2025.</strong></p>
</td><td><p><code>day</code></p>
</td><td><p>n/a</p>
</td><td><p>n/a</p>
</td><td><p><code>total_value</code>,
<code>time_series</code></p>
</td><td><p>The number of times your posts, stories, reels, videos and live videos were on screen, including in ads.</p>
</td></tr><tr class="row_6"><td><p><code>likes</code></p>
</td><td><p><code>day</code></p>
</td><td><p>n/a</p>
</td><td><p><code>media_product_type</code></p>
</td><td><p><code>total_value</code></p>
</td><td><p>The number of likes on your posts, reels, and videos.</p>
</td></tr><tr class="row_7 _5m29"><td><p><code>profile_links_taps</code></p>
</td><td><p><code>day</code></p>
</td><td><p>n/a</p>
</td><td><p><code>contact_button_type</code></p>
</td><td><p><code>total_value</code></p>
</td><td><p>The number of taps on your business address, call button, email button and text button.</p>
</td></tr><tr class="row_8"><td><p><code>reach</code></p>
</td><td><p><code>day</code></p>
</td><td><p>n/a</p>
</td><td><p><code>media_product_type</code>,
<code>follow_type</code></p>
</td><td><p><code data-moz-translations-id="0">total_value</code>,
<code data-moz-translations-id="1">time_series</code></p>
</td><td><p>The number of unique accounts that have seen your content, at least once, including in ads. Content includes posts, stories, reels, videos and live videos. Reach is different from impressions, which includes may multiple views of your content by the same accounts.</p>
<br><p>This metric is estimated.</p>
</td></tr><tr class="row_9 _5m29"><td><p><code>replies</code></p>
</td><td><p><code>day</code></p>
</td><td><p>n/a</p>
</td><td><p>n/a</p>
</td><td><p><code>total_value</code></p>
</td><td><p>The number of replies you received from your story, including text and quick replies reactions.</p>
</td></tr><tr class="row_10"><td><p><code>saved</code></p>
</td><td><p><code>day</code></p>
</td><td><p>n/a</p>
</td><td><p><code>media_product_type</code></p>
</td><td><p><code>total_value</code></p>
</td><td><p>The number of saves of your posts, reels, and videos.</p>
</td></tr><tr class="row_11 _5m29"><td><p><code>shares</code></p>
</td><td><p><code>day</code></p>
</td><td><p>n/a</p>
</td><td><p><code>media_product_type</code></p>
</td><td><p><code>total_value</code></p>
</td><td><p>The number of shares of your posts, stories, reels, videos and live videos.</p>
</td></tr><tr class="row_12"><td><p><code>total_interactions</code></p>
</td><td><p><code>day</code></p>
</td><td><p>n/a</p>
</td><td><p><code>media_product_type</code></p>
</td><td><p><code>total_value</code></p>
</td><td><p>The total number of post interactions, story interactions, reels interactions, video interactions and live video interactions, including any interactions on boosted content.</p>
</td></tr><tr class="row_13 _5m29"><td><p><code>views</code></p>
</td><td><p><code>day</code></p>
</td><td><p>n/a</p>
</td><td><p><code>follower_type</code>, <code>media_product_type</code></p>
</td><td><p><code>total_value</code></p>
</td><td><p>The number of times your content was played or displayed. Content reels, posts, stories.</p>
<br><p>This metric is <a href="https://business.facebook.com/business/help/metrics-labeling" data-moz-translations-id="0" data-auto-logging-id="f5cec2fb3">in development</a>.</p>
</td></tr></tbody></table>

 */