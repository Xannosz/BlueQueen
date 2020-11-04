package hu.xannosz.blue.queen;

import com.google.common.base.Strings;
import hu.xannosz.microtools.pack.Douplet;
import hu.xannosz.veneos.core.HttpHandler;
import hu.xannosz.veneos.core.Page;
import hu.xannosz.veneos.core.VeneosServer;

import java.util.*;

import static hu.xannosz.blue.queen.Constants.*;
import static hu.xannosz.blue.queen.DockerHolder.getValidName;
import static hu.xannosz.blue.queen.PageCreator.*;

public class Queen implements HttpHandler {

    private Data data;
    private final Set<String> tokens = new HashSet<>();

    public Queen() {
        DockerHolder.init();
        data = Data.readData();
        VeneosServer server = new VeneosServer();
        server.createServer(8888);
        server.setHandler(this);
        server.setLogger(LogHandlerImpl.INSTANCE);

        startTimer();
        init();
    }

    private void init() {
        DockerHolder.stopAllTasks(data.getTasks());
        DockerHolder.startAllTasks(data.getTasks());
    }

    @Override
    public Douplet<Integer, Page> getResponse(RequestMethod requestMethod, String s, Map<String, String> map) {

        data = Data.readData();

        String token = null;
        String user = map.get(USER);
        String password = map.get(PASSWD);
        Map<String, String> dataMap = new HashMap<>();

        if (user != null && password != null) {
            if (data.getUserPassword().isEmpty()) {
                data.addUserPassword(user, password);
            }
            if (data.authenticate(user, password)) {
                token = "token" + createToken().replace("-", "");
                tokens.add(token);
            }
        } else {
            token = map.get(TOKEN);
        }

        if (token == null || !tokens.contains(token)) {
            return createLoginPage();
        } else {
            dataMap.put(TOKEN, token);
        }

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
                return PageCreator.redirectPage(dataMap);
            }
            if (method.equals(START)) {
                DockerHolder.start(containerId);
                for (Task task : data.getTasks()) {
                    if (task.getId().equals(containerName)) {
                        task.setShouldRunning(true);
                    }
                }
                return PageCreator.redirectPage(dataMap);
            }
            if (method.equals(RESTART)) {
                DockerHolder.reStart(containerId);
                return PageCreator.redirectPage(dataMap);
            }
            if (method.equals(RE_PULL)) {
                for (Task task : data.getTasks()) {
                    if (task.getId().equals(containerName)) {
                        DockerHolder.delete(containerId);
                        DockerHolder.startTask(task);
                    }
                }
                return PageCreator.redirectPage(dataMap);
            }
            if (method.equals(EDIT)) {
                if (containerId.equals("new")) {
                    return createEdit(null, dataMap);
                }
                for (Task task : data.getTasks()) {
                    if (task.getId().equals(containerName)) {
                        return createEdit(task, dataMap);
                    }
                }
                return PageCreator.redirectPage(dataMap);
            }
            if (method.equals(EDIT_FORM)) {
                if (containerId.equals("new")) {
                    Task task = createTask(map);

                    String oldContainer = DockerHolder.getContainerIdFromName(map.get(ID));
                    if (oldContainer != null) {
                        DockerHolder.delete(oldContainer);
                    }

                    for (Task t : new HashSet<>(data.getTasks())) {
                        if (t.getId().equals(task.getId())) {
                            data.getTasks().remove(t);
                        }
                    }
                    data.getTasks().add(task);

                    DockerHolder.startTask(task);
                    return PageCreator.redirectPage(dataMap);
                }
                for (Task task : new HashSet<>(data.getTasks())) {
                    if (task.getId().equals(containerName)) {
                        DockerHolder.delete(containerId);
                        data.getTasks().remove(task);
                        Task newTask = createTask(map);
                        data.getTasks().add(newTask);
                        DockerHolder.startTask(newTask);
                    }
                }
                return PageCreator.redirectPage(dataMap);
            }
            if (method.equals(LOGS)) {
                return createLogs(containerId, containerName, dataMap);
            }
            if (method.equals(INSPECT)) {
                return createInspect(containerId, containerName, dataMap);
            }
            if (method.equals(DELETE)) {
                return PageCreator.createDelete(containerId, containerName, dataMap);
            }
            if (method.equals(DELETE_OK)) {
                DockerHolder.delete(containerId);
                for (Task task : new HashSet<>(data.getTasks())) {
                    if (task.getId().equals(containerName)) {
                        data.getTasks().remove(task);
                    }
                }
                return PageCreator.redirectPage(dataMap);
            }
        }

        data.writeData();

        return createList(data.getTasks(), dataMap, data.getNextReStartDate());
    }

    private String createToken() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            builder.append(UUID.randomUUID());
        }
        return builder.toString();
    }

    private Task createTask(Map<String, String> map) {
        Task task = new Task();
        task.setId(map.get(ID).trim());
        task.setImage(map.get(IMAGE).trim());
        task.setShouldRunning(Boolean.parseBoolean(map.get(SHOULD_RUN)));
        for (int i = 0; i < map.size(); i++) {
            try {
                int portH = Integer.parseInt(map.get(PORT + "H" + i).trim());
                if (portH != 0) {
                    int portD = Integer.parseInt(map.get(PORT + "D" + i).trim());
                    if (portD != 0) {
                        task.addPort(portH, portD);
                    }
                }
            } catch (Exception e) {
                //Not a problem
            }

            try {
                String volumeH = map.get(VOLUME + "H" + i).trim();
                if (!Strings.isNullOrEmpty(volumeH)) {
                    String volumeD = map.get(VOLUME + "D" + i).trim();
                    if (!Strings.isNullOrEmpty(volumeD)) {
                        task.addVolume(volumeH, volumeD);
                    }
                }
            } catch (Exception e) {
                //Not a problem
            }
        }
        return task;
    }

    private void startTimer() {
        new TimerThread().start();
    }

    public static void main(String[] args) {
        Queen queen = new Queen();
    }
}
