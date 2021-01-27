package hu.xannosz.blue.queen;

import hu.xannosz.microtools.pack.Douplet;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Task {
    private String id;
    private String image;
    private ShouldRunning shouldRunning;
    private Set<Douplet<Integer, Integer>> ports = new HashSet<>();
    private Set<Douplet<String, String>> volumes = new HashSet<>();

    public void addPort(int host, int docker) {
        ports.add(new Douplet<>(host, docker));
    }

    public void addVolume(String host, String docker) {
        volumes.add(new Douplet<>(host, docker));
    }

    public enum ShouldRunning {
        TRUE,FALSE,ONCE
    }
}
