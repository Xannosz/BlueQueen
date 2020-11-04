package hu.xannosz.blue.queen;

import hu.xannosz.microtools.Sleep;

import java.util.Date;

public class TimerThread extends Thread {

    private Data data = Data.readData();

    @Override
    public void run() {
        Sleep.sleepSeconds(30);
        while (data.getNextReStartDate().after(new Date())) {
            data = Data.readData();
            Sleep.sleepMillis(data.getCheckingDelay());
        }
        reStart();
    }

    private void reStart() {
        data.getNextReStartDate().setTime(data.getNextReStartDate().getTime() + data.getTimeToRestart());
        data.writeData();
        DockerHolder.reStart(DockerHolder.getContainerIdFromName(data.getSelfName()));
    }
}
