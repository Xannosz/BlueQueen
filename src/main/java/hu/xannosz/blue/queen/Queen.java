package hu.xannosz.blue.queen;

import hu.xannosz.microtools.pack.Douplet;
import hu.xannosz.veneos.core.HttpHandler;
import hu.xannosz.veneos.core.Page;
import hu.xannosz.veneos.core.VeneosServer;

import java.util.HashSet;
import java.util.Map;

import static hu.xannosz.blue.queen.Constants.*;
import static hu.xannosz.blue.queen.DockerHolder.getValidName;
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
        String containerId = "";
        String method = "";
        if (path.length == 3) {
            containerId = path[1];
            method = path[2];
        }

        String containerName = getValidName(containerId, data.getTasks());

        if (containerId != null) {
            if (method.equals(STOP)) {
                DockerHolder.stop(containerId);
                for (Task task : data.getTasks()) {
                    if (task.getId().equals(containerName)) {
                        task.setShouldRunning(false);
                    }
                }
                return PageCreator.redirectPage();
            }
            if (method.equals(START)) {
                DockerHolder.start(containerId);
                for (Task task : data.getTasks()) {
                    if (task.getId().equals(containerName)) {
                        task.setShouldRunning(true);
                    }
                }
                return PageCreator.redirectPage();
            }
            if (method.equals(RESTART)) {
                DockerHolder.reStart(containerId);
                return PageCreator.redirectPage();
            }
            if (method.equals(RE_PULL)) {
                for (Task task : data.getTasks()) {
                    if (task.getId().equals(containerName)) {
                        DockerHolder.delete(containerId);
                        DockerHolder.startTask(task);
                    }
                }
                return PageCreator.redirectPage();
            }
            if (method.equals(EDIT)) {
                for (Task task : data.getTasks()) {
                    if (task.getId().equals(containerName)) {
                        return createEdit(task);
                    }
                }
                return PageCreator.redirectPage();
            }
            if (method.equals(EDIT_FORM)) {
                for (Task task : data.getTasks()) {
                    DockerHolder.delete(containerId);
                    if (task.getId().equals(containerName)) {
                        task.setId(map.get(ID));
                        task.setImage(map.get(IMAGE));
                        task.setShouldRunning(Boolean.parseBoolean(map.get(SHOULD_RUN)));
                        DockerHolder.startTask(task);
                    }
                }
                return PageCreator.redirectPage();
            }
            if (method.equals(LOGS)) {
                return createLogs(containerId, containerName);
            }
            if (method.equals(INSPECT)) {
                return createInspect(containerId, containerName);
            }
            if (method.equals(DELETE)) {
                DockerHolder.delete(containerId);
                for (Task task : new HashSet<>(data.getTasks())) {
                    if (task.getId().equals(containerName)) {
                        data.getTasks().remove(task);
                    }
                }
                return PageCreator.redirectPage();
            }
        }

        return createList(data.getTasks());
    }
}
