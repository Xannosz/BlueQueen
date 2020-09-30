package hu.xannosz.blue.queen;

public class App {
    public static void main(String[] args) {
        Data data = new Data();

        Task task1 = new Task();
        task1.setId("sleep1");
        task1.setImage("sverrirab/sleep");
        task1.setShouldRunning(false);
        data.addTask(task1);

        Task task2 = new Task();
        task2.setId("started");
        task2.setImage("docker/getting-started");
        task2.addPort(8008, 80);
        task2.setShouldRunning(true);
        data.addTask(task2);

        DockerHolder.init();
        Queen queen = new Queen(data);
    }
}
