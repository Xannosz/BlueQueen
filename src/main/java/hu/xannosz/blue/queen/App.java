package hu.xannosz.blue.queen;

public class App {
    public static void main(String[] args) {
        Data data = new Data();
        Task task = new Task();
        task.setId("minecraft");
        task.setImage("minecraft-test");
        task.setShouldRunning(true);
        data.addTask(task);
        DockerHolder.init();
        Queen queen = new Queen(data);
    }
}
