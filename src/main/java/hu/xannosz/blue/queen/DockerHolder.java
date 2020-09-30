package hu.xannosz.blue.queen;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import hu.xannosz.microtools.pack.Douplet;

import java.util.*;

import static hu.xannosz.blue.queen.Constants.PERSIST_FOLDER;

public class DockerHolder {

    private static DockerClient docker;

    public static void init() {
        docker = DefaultDockerClient.builder().uri("http://localhost:2375").build();
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
                hostConfigBuilder.appendBinds(PERSIST_FOLDER + "/" + task.getId() + "/" + volumePair.getFirst() + ":" + volumePair.getSecond());
            }

            final HostConfig hostConfig = hostConfigBuilder.portBindings(portBindings).build();

            docker.pull(task.getImage() + ":latest");
            final ContainerConfig containerConfig = ContainerConfig.builder().hostConfig(hostConfig).exposedPorts(extPorts)
                    .image(task.getImage() + ":latest").build();
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
            return docker.inspectContainer(containerId).logPath();
        } catch (InterruptedException | DockerException e) {
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
}
