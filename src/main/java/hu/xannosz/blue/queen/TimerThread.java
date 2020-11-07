package hu.xannosz.blue.queen;

import hu.xannosz.microtools.Sleep;

import java.util.Date;

public class TimerThread extends Thread {

    @Override
    public void run() {
        Sleep.sleepSeconds(30);
        while (Data.INSTANCE.getNextReStartDate().after(new Date())) {
            Sleep.sleepMillis(Data.INSTANCE.getCheckingDelay());
        }
        reStart();
    }

    private void reStart() {
        Data.INSTANCE.getNextReStartDate().setTime(Data.INSTANCE.getNextReStartDate().getTime() + Data.INSTANCE.getTimeToRestart());
        Data.writeData();
        DockerHolder.reStart(DockerHolder.getContainerIdFromName(Data.INSTANCE.getSelfName()));
    }
}
