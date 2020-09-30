package hu.xannosz.blue.queen;

import hu.xannosz.microtools.pack.Douplet;
import hu.xannosz.veneos.core.HttpHandler;
import hu.xannosz.veneos.core.Page;
import hu.xannosz.veneos.core.VeneosServer;

import java.util.Map;

import static hu.xannosz.blue.queen.Constants.*;
import static hu.xannosz.blue.queen.DockerHolder.getContainerIdFromName;
import static hu.xannosz.blue.queen.PageCreator.*;

public class Queen implements HttpHandler {

    private final Data data;

    public Queen(Data data) {
        this.data = data;
        VeneosServer server = new VeneosServer();
        server.createServer(8888);
        server.setHandler(this);
        server.setLogger(LogHandlerImpl.INSTANCE);

        init();
    }

    private void init() {
        DockerHolder.stopAllTasks(data.getTasks());
        DockerHolder.startAllTasks(data.getTasks());
    }

    @Override
    public Douplet<Integer, Page> getResponse(RequestMethod requestMethod, String s, Map<String, String> map) {

        String[] path = s.split("/");
        String containerName = "";
        String method = "";
        if (path.length == 3) {
            containerName = path[1];
            method = path[2];
        }

        String containerId = getContainerIdFromName(containerName);

        if (containerId != null) {
            if (method.equals(STOP)) {
                DockerHolder.stop(containerId);
                for (Task task : data.getTasks()) {
                    if (task.getId().equals(containerName)) {
                        task.setShouldRunning(false);
                    }
                }
            }
            if (method.equals(START)) {
                DockerHolder.start(containerId);
                for (Task task : data.getTasks()) {
                    if (task.getId().equals(containerName)) {
                        task.setShouldRunning(true);
                    }
                }
            }
            if (method.equals(RESTART)) {
                DockerHolder.reStart(containerId);
            }
            if (method.equals(EDIT)) {
                for (Task task : data.getTasks()) {
                    if (task.getId().equals(containerName)) {
                        return createEdit(task);
                    }
                }
            }
            if (method.equals(LOGS)) {
                return createLogs(containerId, containerName);
            }
            if (method.equals(INSPECT)) {
                return createInspect(containerId, containerName);
            }
            if (method.equals(DELETE)) {
                DockerHolder.delete(containerId);
            }
        }

        return createList(data.getTasks());
    }
}
