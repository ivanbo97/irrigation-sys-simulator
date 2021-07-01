package com.ivanbo97.mqtt.irrigationsyssimulator.callback;

import com.ivanbo97.mqtt.irrigationsyssimulator.IrrigationSysSimulator;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class IrrigationSysCallback implements MqttCallback {

    public void connectionLost(Throwable throwable) {
        System.out.println("Connection to broker is lost!");
    }

    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        System.out.println("Message arrived: " + mqttMessage.toString() + " On topic: " + topic);
        IrrigationSysSimulator.onReceivedMqttData(topic, mqttMessage.toString());
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
