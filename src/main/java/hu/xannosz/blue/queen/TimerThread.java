package hu.xannosz.blue.queen;

import hu.xannosz.microtools.Sleep;

import java.util.Date;

public class TimerThread extends Thread {

    @Override
    public void run() {
        Sleep.sleepSeconds(30);
        while (Data.INSTANCE.getNextRestartDate().after(new Date())) {
            Sleep.sleepMillis(Data.INSTANCE.getCheckingDelay());
            DockerHolder.checkAllTasks(Data.INSTANCE.getTasks());
        }
        reStart();
    }

    private void reStart() {
        Data.INSTANCE.getNextRestartDate().setTime(Data.INSTANCE.getNextRestartDate().getTime() + Data.INSTANCE.getTimeToRestart());
        Data.writeData();
        DockerHolder.reStart(DockerHolder.getContainerIdFromName(Data.INSTANCE.getSelfName()));
    }
}
