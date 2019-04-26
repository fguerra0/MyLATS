package com.example.mylats;

import java.util.HashMap;

public class GattHeartRateAttributes
{
    private static HashMap<String, String> attributes = new HashMap();

    public static String UUID_HEART_RATE_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb";
    public static String UUID_HEART_RATE = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String UUID_BODY_SENSOR_LOCATION = "00002a38-0000-1000-8000-00805f9b34fb";
    public static String UUID_HEART_RATE_CONTROL_POINT = "00002a39-0000-1000-8000-00805f9b34fb";
    public static String UUID_CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String UUID_BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb";
    public static String UUID_BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";

    static
    {
        attributes.put(UUID_HEART_RATE_SERVICE, "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute");
        attributes.put(UUID_BATTERY_SERVICE, "Battery Service");

        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        attributes.put("00002a02-0000-1000-8000-00805f9b34fb", "Peripheral Privacy Flag");
        attributes.put("00002a04-0000-1000-8000-00805f9b34fb", "Peripheral Preferred Connection Parameters");

        attributes.put(UUID_HEART_RATE, "Heart Rate Measurement");
        attributes.put(UUID_BODY_SENSOR_LOCATION, "Location of Sensor");
        attributes.put(UUID_HEART_RATE_CONTROL_POINT, "Control Point");

        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name");
        attributes.put("00002a24-0000-1000-8000-00805f9b34fb", "Model Number");
        attributes.put("00002a26-0000-1000-8000-00805f9b34fb", "Firmware Revision");
        attributes.put("00002a27-0000-1000-8000-00805f9b34fb", "Hardware Revision");
        attributes.put("00002a2a-0000-1000-8000-00805f9b34fb", "Certification Data List");
        attributes.put("00002a50-0000-1000-8000-00805f9b34fb", "PnP ID");

        attributes.put(UUID_BATTERY_LEVEL, "Battery Level");
    }

    public static String lookup(String uuid, String defaultName)
    {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
