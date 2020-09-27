package hu.xannosz.blue.queen;

import com.amihaiemil.docker.Container;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class Data {

    @Getter
    private final Set<Container> containers = new HashSet<>();

    public void addContainer(Container container) {
        containers.add(container);
    }
}
