package dissertation.GPSCompanionApp.helpers;

import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;

/**
 * Created by Peter on 14/01/2017.
 */

public class Utils {

    public static GregorianCalendar getGregDateTimeFrom(String dateTime){
        String[] date = dateTime.split("-");
        String[] time = dateTime.split(":");
        int year = Integer.parseInt(date[0]);
        int month = Integer.parseInt(date[1]) - 1;
        int day = Integer.parseInt(date[2].substring(0, 2));
        int hour = Integer.parseInt(time[0].substring(time[0].length() - 2));
        int minute = Integer.parseInt(time[1]);
        int second = Integer.parseInt(time[2].substring(0, 2));
        return new GregorianCalendar(year,month,day,hour,minute,second);
    }

    public static String getTimeReadable(String dateTime){
        String[] timeSplit = dateTime.split("T");
        String[] time = timeSplit[1].split(":");
        String hour = time[0];
        String minute = time[1];
        String second = time[2].substring(0,2);
        return hour + ":" + minute + ":" + second;
    }

    public static String getDateTimeReadable(String dateTime){
        String[] dateTimeSplit = dateTime.split("T");

        String[] date = dateTimeSplit[0].split("-");
        String year = date[0];
        String month = date[1];
        String day = date[2];

        String[] time = dateTimeSplit[1].split(":");
        String hour = time[0];
        String minute = time[1];
        String second = time[2].substring(0,2);
        return day + "/" + month + "/" + year + " at: " + hour + ":" + minute + ":" + second;
    }

    public static String getDateReadable(String inputStr){
        String[] fields = inputStr.split("-");
        String year = fields[0];
        String month = fields[1];
        String day = fields[2].substring(0,2);
        return day + "/" + month + "/" + year;
    }

    public static String getDateReadable(GregorianCalendar dateTime){
        int year = dateTime.get(Calendar.YEAR);
        int month = dateTime.get(Calendar.MONTH) + 1;
        int day = dateTime.get(Calendar.DAY_OF_MONTH);

        String monthPadded = Integer.toString(month);
        if (monthPadded.length() == 1) {
            monthPadded =  "0" + monthPadded;
        }

        String daysPadded = Integer.toString(day);
        if (daysPadded.length() == 1) {
            daysPadded =  "0" + daysPadded;
        }

        return daysPadded + "/" + monthPadded + "/" + year;
    }

    public static String getDurationFormat(long duration){
        long mins = duration / 1000 / 60;
        long hours = mins / 60;
        String output;
        if (Math.abs(hours) >= 1){
            output = hours + " hrs";
        } else {
            output = mins + " mins";
        }
        return output;
    }

    public static int getIndexOfValue(String[] list, String value){
        double doubleValue = Double.parseDouble(value);
        for (int i = 0; i < list.length; i++){
            double listValue = Double.parseDouble(list[i]);
            if (doubleValue == listValue){
                return i;
            }
        }
        return -1;
    }

    public static String formatValue(double value){
        NumberFormat format = NumberFormat.getIntegerInstance();
        return format.format(value);
    }

    public static String mpsToMph(double mpsValue){
        return formatValue(mpsValue * 2.236936);
    }
}
