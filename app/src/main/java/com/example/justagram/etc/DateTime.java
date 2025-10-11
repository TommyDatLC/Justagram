package com.example.justagram.etc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Consumer;

public class DateTime {
    static final Calendar calendar = Calendar.getInstance();
    public static void OpenDateSelector(Context ctx, DateTime needToChange) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datepicker = new DatePickerDialog(ctx, (view, Syear, Smonth, Sday) -> {
            needToChange.SetDate(Syear,Smonth + 1,Sday);
        },
                year,
                month,
                day
        );
        datepicker.show();
    }

    public static void OpenTimeSelector(Context ctx, DateTime needToChange)
    {
        int h = calendar.get(Calendar.HOUR_OF_DAY); // Sử dụng HOUR_OF_DAY cho định dạng 24 giờ
        int m = calendar.get(Calendar.MINUTE);

        TimePickerDialog tpk = new TimePickerDialog(ctx, (view, hourOfDay, minute) -> {
            needToChange.SetTime(hourOfDay,minute);
        },
                h,
                m,
                true // Sử dụng định dạng 24 giờ
        );
        tpk.show();
    }
    public DateTime()
    {
        SetToCurrentTime();
    }
    public Consumer<Object> onDateChange;
    public Consumer<Object> onTimeChange;
    public void SetDate(int y,int m,int d)
    {
        year = y;
        month = m;
        day = d;
        if (onDateChange != null)
        onDateChange.accept(null);
    }
    public void SetTime(int h,int m)
    {
        hour = h;
        minute = m;
        if (onTimeChange != null)
        onTimeChange.accept(null);
    }
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second = 0;
    public long ConvertToUnixTime()
    {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        // Lưu ý: Calendar.set() dùng tháng bắt đầu từ 0 (0 = tháng 1)
        calendar.set(year, month - 1, day, (hour - 7) % 24, minute, second);
        var t = calendar.HOUR;
        return calendar.getTimeInMillis() / 1000;
    }

    public String GetTimeString() {
        // Định dạng chuỗi để đảm bảo giờ và phút luôn có 2 chữ số (ví dụ: 09:05)
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }
    public String GetDateString() {
        // Định dạng chuỗi ngày/tháng/năm
        return String.format(Locale.getDefault(), "%02d/%02d/%d", day, month, year);
    }
    public void SetToCurrentTime() {
        // Lấy thời gian hiện tại
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1; // Calendar.MONTH bắt đầu từ 0, nên cần +1
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);
        this.second = now.get(Calendar.SECOND);

        // Gọi các hàm SetDate và SetTime để cập nhật giá trị
        // Điều này đảm bảo logic trong các hàm đó (ví dụ: gọi onDateChange) được thực thi
        SetDate(currentYear, currentMonth, currentDay);
        SetTime(currentHour, currentMinute);
    }
}
