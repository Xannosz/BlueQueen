package hu.xannosz.blue.queen;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class Data {

    @Getter
    private final Set<Task> tasks = new HashSet<>();

    public void addTask(Task task) {
        tasks.add(task);
    }
}
