package com.bashizip.bhlib;


import java.util.ArrayList;

public class BusinessHours extends BasePojo implements Comparable<BusinessHours> {

    static int[] week_days_int = {1, 2, 3, 4, 5, 6, 7};
    private int dayIndex;
    private String dayOfWeek;
    private String from;
    private String to, to24, from24;
    private String shortDayOfWeek;

    public BusinessHours() {


    }


    public BusinessHours(String dayOfWeek, String from, String to) {
        this.dayOfWeek = dayOfWeek;
        this.from = from;
        this.to = to;
    }

    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getShortDayOfWeek() {
        return shortDayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setShortDayOfWeek(String shortDayOfWeek) {
        this.shortDayOfWeek = shortDayOfWeek;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTo24() {
        return to24;
    }

    public void setTo24(String to24) {
        this.to24 = to24;
    }

    public String getFrom24() {
        return from24;
    }

    public void setFrom24(String from24) {
        this.from24 = from24;
    }

    @Override
    public String toString() {

        ArrayList<String> foo = new ArrayList<>();

        foo.add(dayOfWeek + ", " + from + " - " + to);
        foo.add("\\"+"\""+shortDayOfWeek+ " "+from24+ "-"+to24+"\\"+"\"");

        return dayOfWeek + ", " + from + " - " + to;
    }

    @Override
    public int compareTo(BusinessHours businessHours) {
        return 0;
    }
}
