package com.ivanbo97.mqtt.irrigationsyssimulator.utils;

import com.ivanbo97.mqtt.irrigationsyssimulator.IrrigationSysSimulator;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class MainSysLoop implements Runnable {

    private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
    private Calendar calendar = Calendar.getInstance();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private int idxForIncreasingMoisture = 1;

    @Override
    public void run() {
        System.out.println("Publish Messages");
        calendar = Calendar.getInstance();
        System.out.println("Current Time: " + timeFormatter.format(calendar.getTime()));
        IrrigationSysSimulator.currentDate = LocalDate.now().format(dateFormatter);
        IrrigationSysSimulator.currentTime = timeFormatter.format(calendar.getTime());

        // publish messages
        IrrigationSysSimulator.publishSysState();

        IrrigationSystemState currentSystemState = IrrigationSysSimulator.getIrrigationSystemState();
        // check for automodes
        if (currentSystemState.isPumpRunning()) {
            if (idxForIncreasingMoisture % 5 == 0) {
                IrrigationSysSimulator.currentSoilMoisture += 2;
            }
            IrrigationSysSimulator.currentSoilMoisture += 1;
            idxForIncreasingMoisture++;
        }
        if (currentSystemState.isAutoMode1On()) {
            IrrigationSysSimulator.delayedIrrigationStart();
        }

        if(currentSystemState.isRequestForMode2Sent()){
            IrrigationSysSimulator.holdUpHumidityTask();
        }
    }
}
