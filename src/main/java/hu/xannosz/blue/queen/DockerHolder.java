package hu.xannosz.blue.queen;

import com.amihaiemil.docker.Container;
import com.amihaiemil.docker.Docker;
import com.amihaiemil.docker.Image;
import com.amihaiemil.docker.TcpDocker;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class DockerHolder {

    private static Docker docker;

    public static void init() {
        docker = new TcpDocker(URI.create("http://localhost:2375"));
    }

    public static void startAllTask(Set<Task> tasks) {
        for (Task task : tasks) {
            try {
                final Image image = docker.images().pull(task.getImage(), "latest");
                if (task.isShouldRunning()) {
                    final Container running = image.run();
                    running.rename(task.getId());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Set<Container> getContainers() {
        Set<Container> containers = new HashSet<>();
        docker.containers().all().forEachRemaining((containers::add));
        return containers;
    }

    public static Container getContainerFromId(String id) {
        for (Container cr : getContainers()) {
            if (cr.containerId().equals(id)) {
                return cr;
            }
        }
        return null;
    }

    public static Container getContainerFromName(String name) {
        for (Container cr : getContainers()) {
            try {
                if (cr.inspect().getString("Name").equals(name)) {
                    return cr;
                }
            } catch (Exception e) {
                // Empty
            }
        }
        return null;
    }
}
