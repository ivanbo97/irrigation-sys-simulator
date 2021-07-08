package com.ivanbo97.mqtt.irrigationsyssimulator.utils;

public final class ApplicationConstatnts {

    public static final String SYS_USR_NAME = "irrigationsys";
    public static final String SYS_PASS = "irrigsyspasswd";
    public static final String BROKER_URL = "ssl://localhost:1883";
    public static final String CLIENT_ID = "IrrigationSysSimulator";
    public static final String CA_CRT_PATH = "D:\\JavaPrograms\\ssl_certificates\\ca.crt";
    public static final String CRT_PATH = "D:\\JavaPrograms\\ssl_certificates\\cert.crt";
    public static final String KEY_PATH = "D:\\JavaPrograms\\ssl_certificates\\key.key";

    public static final String PUMP_MANAGE_TOPIC = "pump";
    public static final String AUTO_MODE1_TOPIC = "automode1";
    public static final String AUTO_MODE1_DATE_TOPIC = "automode1/date";
    public static final String AUTO_MODE1_TIME_TOPIC = "automode1/time";
    public static final String AUTO_MODE1_DURATION_TOPIC = "automode1/duration";
    public static final String AUTO_MODE2_TOPIC = "automode2";
    public static final String AUTO_MODE2_MOISTURE_TOPIC = "automode2/moisture";

    public static final String MOISTURE_VALUE_TOPIC = "moisture";
    public static final String PUMP_STATE_TOPIC = "pumpstate";
    public static final String AUTOMODE1_STATE_TOPIC = "auto1";
    public static final String AUTOMODE2_STATE_TOPIC = "auto2";
    public static final String TEMPERATURE_TOPIC = "temperature";


    public static final String[] SUBSCRIPTION_TOPICS = {PUMP_MANAGE_TOPIC, "automode1", "automode1/date", "automode1/time",
            "automode1/duration", "automode2", "automode2/moisture"};

    public static final int SUBSCRPTION_TOPICS_QOS[] = {2, 2, 2, 2, 2, 2, 2};
}
