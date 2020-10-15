package hu.xannosz.blue.queen;

import com.google.common.base.Strings;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import hu.xannosz.microtools.pack.Douplet;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static hu.xannosz.blue.queen.Constants.PERSIST_FOLDER;

public class DockerHolder {

    private static DockerClient docker;

    public static void init() {
        docker = DefaultDockerClient.builder().uri("unix:///var/run/docker.sock").build();
        //docker = DefaultDockerClient.builder().uri("http://localhost:2375").build();
    }

    public static void startAllTasks(Set<Task> tasks) {
        for (Task task : tasks) {
            startTask(task);
        }
    }

    public static void startTask(Task task) {
        try {
            final Set<String> extPorts = new HashSet<>();
            final Map<String, List<PortBinding>> portBindings = new HashMap<>();

            for (Douplet<Integer, Integer> portPair : task.getPorts()) {
                List<PortBinding> hostPorts = new ArrayList<>();
                hostPorts.add(PortBinding.of("0.0.0.0", portPair.getFirst()));
                portBindings.put(portPair.getSecond().toString(), hostPorts);
                extPorts.add(portPair.getSecond().toString());
            }

            final HostConfig.Builder hostConfigBuilder = HostConfig.builder();

            for (Douplet<String, String> volumePair : task.getVolumes()) {
                if (!Strings.isNullOrEmpty(volumePair.getFirst()) && !Strings.isNullOrEmpty(volumePair.getSecond())) {
                    hostConfigBuilder.appendBinds(PERSIST_FOLDER + "/" + task.getId() + "/" + volumePair.getFirst() + ":" + volumePair.getSecond());
                }
            }

            final HostConfig hostConfig = hostConfigBuilder.portBindings(portBindings).build();

            docker.pull(task.getImage().split(":")[0] + ":latest");
            final ContainerConfig containerConfig = ContainerConfig.builder().hostConfig(hostConfig).exposedPorts(extPorts)
                    .image(task.getImage().split(":")[0] + ":latest").build();
            final ContainerCreation creation = docker.createContainer(containerConfig);
            final String id = creation.id();

            docker.renameContainer(id, task.getId());
            if (task.isShouldRunning()) {
                docker.startContainer(id);
            }
        } catch (Exception e) {
            LogHandlerImpl.INSTANCE.error(String.format("Pull and start image %s failed.", task.getImage()), e);
        }
    }

    public static Set<Container> getContainers() {
        try {
            return new HashSet<>(docker.listContainers(DockerClient.ListContainersParam.allContainers()));
        } catch (InterruptedException | DockerException e) {
            LogHandlerImpl.INSTANCE.error("Get containers failed.", e);
        }
        return new HashSet<>();
    }

    public static ContainerInfo inspectContainer(String containerId) {
        try {
            return docker.inspectContainer(containerId);
        } catch (InterruptedException | DockerException e) {
            LogHandlerImpl.INSTANCE.error(String.format("Inspect container %s failed.", containerId), e);
        }
        return null;
    }

    public static String getContainerLog(String containerId) {
        try {
            File file = new File(docker.inspectContainer(containerId).logPath());
            if (file.exists() && file.isFile()) {
                return FileUtils.readFileToString(file);
            }
            return docker.inspectContainer(containerId).logPath();
        } catch (InterruptedException | DockerException | IOException e) {
            LogHandlerImpl.INSTANCE.error(String.format("Inspect container %s failed.", containerId), e);
        }
        return "";
    }

    public static String getContainerIdFromName(String name) {
        for (Container cr : getContainers()) {
            if (cr.names() != null && Objects.requireNonNull(cr.names()).contains("/" + name)) {
                return cr.id();
            }
        }
        return null;
    }

    public static void stop(String id) {
        try {
            docker.stopContainer(id, 1);
        } catch (DockerException | InterruptedException e) {
            LogHandlerImpl.INSTANCE.error(String.format("Stop container %s failed.", id), e);
        }
    }

    public static void start(String id) {
        try {
            docker.startContainer(id);
        } catch (DockerException | InterruptedException e) {
            LogHandlerImpl.INSTANCE.error(String.format("Start container %s failed.", id), e);
        }
    }

    public static void reStart(String id) {
        try {
            docker.restartContainer(id, 1);
        } catch (DockerException | InterruptedException e) {
            LogHandlerImpl.INSTANCE.error(String.format("Restart container %s failed.", id), e);
        }
    }

    public static boolean isManaged(Container container, Set<Task> tasks) {
        String name = getValidName(container, tasks);
        for (Task task : tasks) {
            if (task.getId().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static void delete(String id) {
        stop(id);
        try {
            docker.killContainer(id);
        } catch (DockerException | InterruptedException e) {
            LogHandlerImpl.INSTANCE.warn(String.format("Kill container %s failed.", id), e);
        }
        try {
            docker.removeContainer(id);
        } catch (DockerException | InterruptedException e) {
            LogHandlerImpl.INSTANCE.error(String.format("Remove container %s failed.", id), e);
        }
    }

    public static void stopAllTasks(Set<Task> tasks) {
        for (Task task : tasks) {
            try {
                final String running = getContainerIdFromName(task.getId());
                if (running != null) {
                    delete(running);
                }
            } catch (Exception e) {
                LogHandlerImpl.INSTANCE.error(String.format("Stop %s task failed.", task.getId()), e);
            }
        }
    }

    public static String getValidName(String container, Set<Task> tasks) {
        Container cont = getContainerFromId(container);
        if (cont == null) {
            return null;
        }
        return getValidName(cont, tasks);
    }

    public static String getValidName(Container container, Set<Task> tasks) {
        if (container.names() == null) {
            return container.id();
        }
        for (String cnt : Objects.requireNonNull(container.names())) {
            for (Task task : tasks) {
                if (("/" + task.getId()).equals(cnt)) {
                    return task.getId();
                }
            }
        }
        if (Objects.requireNonNull(container.names()).size() > 0) {
            return Objects.requireNonNull(container.names()).get(0).substring(1);
        }
        return container.id();
    }

    public static Container getContainerFromId(String container) {
        for (Container cont : getContainers()) {
            if (cont.id().equals(container)) {
                return cont;
            }
        }
        return null;
    }
}
