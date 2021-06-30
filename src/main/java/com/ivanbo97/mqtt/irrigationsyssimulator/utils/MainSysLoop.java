package com.ivanbo97.mqtt.irrigationsyssimulator.utils;

import com.ivanbo97.mqtt.irrigationsyssimulator.IrrigationSysSimulator;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class MainSysLoop implements Runnable {

    private static String currentDate;

    private static String currentTime;

    private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
    private Calendar cal = Calendar.getInstance();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Override
    public void run() {


        System.out.println("Publish Messages");

        currentDate = LocalDate.now().format(dateFormatter);
        currentTime = timeFormatter.format(cal.getTime());

        // get humidity

        // publish messages
        IrrigationSysSimulator.publishSysState();

        // check for automodes

        if (IrrigationSysSimulator.getIrrigationSystemState().isPumpRunning()) {
            IrrigationSysSimulator.currentSoilMoisture += 1;
        }
        if (IrrigationSysSimulator.getIrrigationSystemState().isAutoMode1On()) {
            IrrigationSysSimulator.delayedIrrigationStart();
        }

        if (IrrigationSysSimulator.getIrrigationSystemState().isAutoMode2On()) {
            IrrigationSysSimulator.holdUpHumidityTask();
        }


    }
}
