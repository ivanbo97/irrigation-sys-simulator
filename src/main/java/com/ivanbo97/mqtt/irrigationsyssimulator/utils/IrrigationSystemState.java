package com.ivanbo97.mqtt.irrigationsyssimulator.utils;

public class IrrigationSystemState {

    private boolean pumpRunning;
    private boolean autoMode1On;
    private boolean autoMode2On;
    private boolean requestForMode2Sent;

    public IrrigationSystemState() {
        pumpRunning = false;
        autoMode1On = false;
        autoMode2On = false;
        requestForMode2Sent = false;
    }

    public boolean isPumpRunning() {
        return pumpRunning;
    }

    public void setPumpRunning(boolean pumpRunning) {
        this.pumpRunning = pumpRunning;
    }

    public boolean isAutoMode1On() {
        return autoMode1On;
    }

    public void setAutoMode1On(boolean autoMode1On) {
        this.autoMode1On = autoMode1On;
    }

    public boolean isAutoMode2On() {
        return autoMode2On;
    }

    public void setAutoMode2On(boolean autoMode2On) {
        this.autoMode2On = autoMode2On;
    }

    public boolean isRequestForMode2Sent() {
        return requestForMode2Sent;
    }

    public void setRequestForMode2Sent(boolean requestForMode2Sent) {
        this.requestForMode2Sent = requestForMode2Sent;
    }
}
