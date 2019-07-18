package com.test.geofenceservice;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class NotifyModel {
    private String app_id;
    private JsonObject headings;
    private String big_picture;
    private JsonObject contents;
    private JsonArray include_player_ids;
    private String title;
    private String collapse_id;
    private JsonObject data;
    private int small_icon;
    private int large_icon;
    private String android_accent_color;
    private JsonArray buttons;
    private JsonObject subtitle;

    public NotifyModel(String app_id, JsonObject heading, String big_picture, JsonObject contents, JsonArray include_player_ids) {
        this.app_id = app_id;
        this.headings = heading;
        this.big_picture = big_picture;
        this.contents = contents;
        this.include_player_ids = include_player_ids;
    }

    public NotifyModel() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public JsonObject getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(JsonObject subtitle) {
        this.subtitle = subtitle;
    }
    public String getCollapse_id() {
        return collapse_id;
    }

    public void setCollapse_id(String collapse_id) {
        this.collapse_id = collapse_id;
    }

    public JsonArray getButtons() {
        return buttons;
    }

    public void setButtons(JsonArray buttons) {
        this.buttons = buttons;
    }



    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public JsonObject getHeading() {
        return headings;
    }

    public void setHeading(JsonObject heading) {
        this.headings = heading;
    }

    public String getBig_picture() {
        return big_picture;
    }

    public void setBig_picture(String big_picture) {
        this.big_picture = big_picture;
    }

    public JsonObject getContents() {
        return contents;
    }

    public void setContents(JsonObject contents) {
        this.contents = contents;
    }

    public JsonArray getInclude_player_ids() {
        return include_player_ids;
    }

    public void setInclude_player_ids(JsonArray include_player_ids) {
        this.include_player_ids = include_player_ids;
    }

    public int getSmall_icon() {
        return small_icon;
    }

    public void setSmall_icon(int small_icon) {
        this.small_icon = small_icon;
    }

    public int getLarge_icon() {
        return large_icon;
    }

    public void setLarge_icon(int large_icon) {
        this.large_icon = large_icon;
    }

    public String getAndroid_accent_color() {
        return android_accent_color;
    }

    public void setAndroid_accent_color(String android_accent_color) {
        this.android_accent_color = android_accent_color;
    }

    @Override
    public String toString() {
        return "NotifyModel{" +
                "app_id='" + app_id + '\'' +
                ", headings=" + headings +
                ", big_picture='" + big_picture + '\'' +
                ", contents=" + contents +
                ", include_player_ids=" + include_player_ids +
                ", title='" + title + '\'' +
                ", collapse_id='" + collapse_id + '\'' +
                ", data=" + data +
                ", small_icon=" + small_icon +
                ", large_icon=" + large_icon +
                ", android_accent_color='" + android_accent_color + '\'' +
                ", subtitle=" + subtitle +
                ", buttons=" + buttons +
                '}';
    }
}
