package com.ivanbo97.mqtt.irrigationsyssimulator;

import com.ivanbo97.mqtt.irrigationsyssimulator.callback.IrrigationSysCallback;
import com.ivanbo97.mqtt.irrigationsyssimulator.ssl.SSLConfigurator;
import com.ivanbo97.mqtt.irrigationsyssimulator.utils.IrrigationSystemState;
import com.ivanbo97.mqtt.irrigationsyssimulator.utils.MainSysLoop;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import javax.net.ssl.SSLSocketFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.ivanbo97.mqtt.irrigationsyssimulator.utils.ApplicationConstatnts.*;

public class IrrigationSysSimulator {

    private static MqttAsyncClient client;
    MqttConnectOptions conOpt;
    private static IrrigationSystemState irrigationSystemState;
    public static int currentSoilMoisture = 0;
    static int moistureOffset = 5;


    private static String currentDate;
    private static String currentTime;

    private static String receivedDateForAutomode1;
    private static String receivedIrrigationDuration;
    private static String receivedStartTime;
    private static String receivedHoldUpMoistureLvl;


    private static Timer delayedStartTaskTime = new Timer();


    public static void main(String[] args) {

        try {
            IrrigationSysSimulator simulator = new IrrigationSysSimulator();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public IrrigationSysSimulator() throws MqttException {

        //This sample stores in a temporary directory... where messages temporarily
        // stored until the message has been delivered to the server.
        //..a real application ought to store them somewhere
        // where they are not likely to get deleted or tampered with
        String tmpDir = System.getProperty("java.io.tmpdir");
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);


        // Construct the object that contains connection parameters
        // such as cleanSession and LWT
        conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);

        InputStream caCrtFile = null;
        InputStream crtFile = null;
        InputStream keyFile = null;

        try {
            caCrtFile = new FileInputStream(CA_CRT_PATH);
            crtFile = new FileInputStream(CRT_PATH);
            keyFile = new FileInputStream(KEY_PATH);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        SSLSocketFactory sslSocketFactory = null;
        try {
            sslSocketFactory = SSLConfigurator.getSocketFactory(caCrtFile, crtFile, keyFile, "");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        conOpt.setSocketFactory(sslSocketFactory);
        conOpt.setPassword(SYS_PASS.toCharArray());
        conOpt.setUserName(SYS_USR_NAME);

       conOpt.setMaxInflight(1000);
        // Construct the MqttClient instance
        client = new MqttAsyncClient(BROKER_URL, CLIENT_ID, dataStore);

        // Set this wrapper as the callback handler
        IrrigationSysCallback mqttCallback = new IrrigationSysCallback();
        client.setCallback(mqttCallback);
        IMqttActionListener connectionListener = new IMqttActionListener() {
            public void onSuccess(IMqttToken iMqttToken) {
                System.out.println("Connected successfully to broker. Subscription initiated...");
                try {
                    irrigationSystemState = new IrrigationSystemState();
                    client.subscribe(SUBSCRIPTION_TOPICS, SUBSCRPTION_TOPICS_QOS);
                    System.out.println("Successful subscription to topics! Running main Task...");
                    // mainTaskSimulation();
                    // Thread mainTaskThread = new Thread(new MainSysLoop());
                    // mainTaskThread.start();
                    ScheduledExecutorService executor =
                            Executors.newSingleThreadScheduledExecutor();
                    Runnable mainSysTask = new MainSysLoop();
                    executor.scheduleAtFixedRate(mainSysTask, 0, 1, TimeUnit.SECONDS);
                } catch (MqttException e) {
                    e.printStackTrace();
                    return;
                }
            }

            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {

            }
        };
        client.connect(conOpt, this, connectionListener);

    }

    static class AutoMode1TaskTerminator extends TimerTask {
        public void run() {
            System.out.println("Irrigation Time passed!!");
            delayedIrrigationStop();
            delayedStartTaskTime.cancel(); //Terminate the timer thread
        }
    }


    public static void onReceivedMqttData(String currentTopic, String currentMessage) {
        boolean isAutoMode1On = irrigationSystemState.isAutoMode1On();
        boolean isAutoMode2On = irrigationSystemState.isAutoMode2On();
        System.out.println("IN onReceievedMqttData with topic:" + currentTopic + " and:" + currentMessage);

        if (currentTopic.equals(PUMP_MANAGE_TOPIC) && !isAutoMode1On && !isAutoMode2On) {
            managePump(currentMessage);
            return;
        }

        if (currentTopic.equals(AUTO_MODE1_TOPIC) && currentMessage.equals("on")) {
            irrigationSystemState.setAutoMode1On(true);
            return;
        }

        if (currentTopic.equals(AUTO_MODE1_TOPIC) && currentMessage.equals("off")) {
            irrigationSystemState.setAutoMode1On(false);
            stopPump();
            return;
        }

        if (currentTopic.equals(AUTO_MODE1_DATE_TOPIC)) {
            // get date and store it
            receivedDateForAutomode1 = currentMessage;
        }

        if (currentTopic.equals(AUTO_MODE1_TIME_TOPIC)) {
            // get time and store it
            receivedStartTime = currentMessage;
        }

        if (currentTopic.equals(AUTO_MODE1_DURATION_TOPIC)) {
            // get duration and store it
            receivedIrrigationDuration = currentMessage;
        }

        if (currentTopic.equals(AUTO_MODE2_TOPIC) && currentMessage.equals("on")) {
            irrigationSystemState.setAutoMode2On(true);
            return;
        }

        if (currentTopic.equals(AUTO_MODE2_TOPIC) && currentMessage.equals("off")) {
            irrigationSystemState.setAutoMode2On(false);
            stopPump();
        }

        if (currentTopic.equals(AUTO_MODE2_MOISTURE_TOPIC)) {
            // get desired humidity
            receivedHoldUpMoistureLvl = currentMessage;
        }
    }

    private static void managePump(String pumpMessage) {
        System.out.println("In managePump() with message: " + pumpMessage);
        if (pumpMessage.equals("on")) {
            startPump();
            return;
        }
        if (pumpMessage.equals("off")) {
            stopPump();
            return;
        }
    }

    public static void publishSysState() {
        MqttMessage moistureValMsg = new MqttMessage(String.valueOf(currentSoilMoisture).getBytes());

        String autoMode1State = irrigationSystemState.isAutoMode1On() ? "on" : "off";
        MqttMessage autoMode1StateMsg = new MqttMessage(autoMode1State.getBytes());

        String autoMode2State = irrigationSystemState.isAutoMode2On() ? "on" : "off";
        MqttMessage autoMode2StateMsg = new MqttMessage(autoMode2State.getBytes());

        String pumpState = irrigationSystemState.isPumpRunning() ? "on" : "off";
        MqttMessage pumpStateMsg = new MqttMessage(pumpState.getBytes());

        try {
            client.publish(MOISTURE_VALUE_TOPIC, moistureValMsg);
            client.publish(PUMP_STATE_TOPIC, pumpStateMsg);
            client.publish(AUTOMODE1_STATE_TOPIC, autoMode1StateMsg);
            client.publish(AUTOMODE2_STATE_TOPIC, autoMode2StateMsg);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private static void startPump() {
        irrigationSystemState.setPumpRunning(true);
        for (int i = 0; i < 600; i++) {
            //Pump start simulation time
        }
    }

    private static void stopPump() {

        for (int i = 0; i < 300; i++) {
            //Pump stop simulation time
        }

        irrigationSystemState.setPumpRunning(false);
    }

    public static void delayedIrrigationStart() {
        if (receivedDateForAutomode1.equals(currentDate) &&
                receivedStartTime.equals(currentTime) &&
                !irrigationSystemState.isPumpRunning()) {

            startPump();
            long durationIntervalMs = (60L * 1000L * Long.valueOf(receivedIrrigationDuration));
            delayedStartTaskTime.schedule(new AutoMode1TaskTerminator(), durationIntervalMs);
        }
    }

    private static void delayedIrrigationStop() {
        stopPump();
        irrigationSystemState.setAutoMode1On(false);
    }

    public static void holdUpHumidityTask() {
        int desiredMoisture = Integer.valueOf(receivedHoldUpMoistureLvl);
        if (currentSoilMoisture <= -moistureOffset) {
            startPump();
        }

        if (currentSoilMoisture >= desiredMoisture) {
            stopPump();
        }
    }

    public static IrrigationSystemState getIrrigationSystemState() {
        return irrigationSystemState;
    }
}