package com.ivanbo97.mqtt.irrigationsyssimulator.utils;

import com.ivanbo97.mqtt.irrigationsyssimulator.IrrigationSysSimulator;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class MainSysLoop implements Runnable {


    private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
    private Calendar cal = Calendar.getInstance();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private int idxForIncreasingMoisture = 1;

    @Override
    public void run() {
        System.out.println("Publish Messages");
        cal = Calendar.getInstance();
        System.out.println("Current Time: " + timeFormatter.format(cal.getTime()));
        IrrigationSysSimulator.currentDate = LocalDate.now().format(dateFormatter);
        IrrigationSysSimulator.currentTime = timeFormatter.format(cal.getTime());

        // get humidity

        // publish messages
        IrrigationSysSimulator.publishSysState();

        // check for automodes

        if (IrrigationSysSimulator.getIrrigationSystemState().isPumpRunning()) {
            if (idxForIncreasingMoisture % 5 == 0) {
                IrrigationSysSimulator.currentSoilMoisture += 4;
            }
            IrrigationSysSimulator.currentSoilMoisture += 1;
            idxForIncreasingMoisture++;
        }
        if (IrrigationSysSimulator.getIrrigationSystemState().isAutoMode1On()) {
            IrrigationSysSimulator.delayedIrrigationStart();
        }

        if (IrrigationSysSimulator.getIrrigationSystemState().isAutoMode2On()) {
            IrrigationSysSimulator.holdUpHumidityTask();
        }
    }
}
