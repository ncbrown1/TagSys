package com.ucsbeci.app;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeFormatter {

    private static String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
    private static String[] monthNums = {"01","02","03","04","05","06","07","08","09","10","11","12"};

    /*public static String convertTime(String original) {
        String sub = original.substring(5, 25);
        String day = sub.substring(0, 2);
        String mon = sub.substring(3, 6);
        String yr  = sub.substring(7,11);
        String clk = sub.substring( 12 );
        mon = convertMonth(mon);

        return yr + "-" + mon + "-" + day + " " + clk;
    }*/

    public static String convertTime(String original) {
            String first = original.substring(0,10);
            String second = original.substring(11,original.length()-1);
            System.out.println(first+ " " + second);
            return first + " " + second;
    }

    private static String convertMonth(String mon) {
        for(int i = 0; i < months.length; i++) {
            if(months[i].equals(mon)) {
                return monthNums[i];
            }
        }
        return "00";
    }

    public static String newTime() {
        Date time = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(time);
    }
}
