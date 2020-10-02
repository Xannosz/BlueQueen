package hu.xannosz.blue.queen;

import com.spotify.docker.client.messages.Container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class Util {
    public static List<Container> sortContainers(Collection<Container> containers) {
        List<Container> list = new ArrayList<>(containers);
        list.sort(Comparator.comparing(Container::id));
        return list;
    }
}
