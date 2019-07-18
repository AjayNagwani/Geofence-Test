package com.test.geofenceservice;

public class NotificationModel {
    String device_id;
    String message;

    public String getDevice_info() {
        return device_info;
    }

    public void setDevice_info(String device_info) {
        this.device_info = device_info;
    }

    String device_info;
    public NotificationModel(String device_id, String message) {
        this.device_id = device_id;
        this.message = message;
    }

    public NotificationModel() {

    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
