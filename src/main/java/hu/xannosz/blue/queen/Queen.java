package hu.xannosz.blue.queen;

import com.google.common.base.Strings;
import hu.xannosz.microtools.pack.Douplet;
import hu.xannosz.veneos.core.VeneosServer;
import hu.xannosz.veneos.core.handler.HttpHandler;
import hu.xannosz.veneos.core.html.structure.Page;

import java.util.*;

import static hu.xannosz.blue.queen.Constants.*;
import static hu.xannosz.blue.queen.DockerHolder.getValidName;
import static hu.xannosz.blue.queen.PageCreator.*;

public class Queen implements HttpHandler {

    private final Set<String> tokens = new HashSet<>();

    public Queen() {
        DockerHolder.init();
        Data.readData();
        VeneosServer server = new VeneosServer();
        server.createServer(8888);
        server.setHandler(this);
        server.setLogger(LogHandlerImpl.INSTANCE);

        startTimer();
        init();
    }

    private void init() {
        DockerHolder.stopAllTasks(Data.INSTANCE.getTasks());
        DockerHolder.startAllTasks(Data.INSTANCE.getTasks());
    }

    @Override
    public Douplet<Integer, Page> getResponse(RequestMethod requestMethod, String s, Map<String, String> map) {

        String token = null;
        String user = map.get(USER);
        String password = map.get(PASSWD);
        Map<String, String> dataMap = new HashMap<>();

        if (user != null && password != null) {
            if (Data.INSTANCE.getUserPassword().isEmpty()) {
                Data.INSTANCE.getEnabledUsers().add(user);
                Data.INSTANCE.addUserPassword(user, password);
            }
            if (Data.INSTANCE.authenticate(user, password)) {
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

        if (path.length == 2) {
            method = path[1];
        }

        String containerName = getValidName(containerId, Data.INSTANCE.getTasks());

        if (containerId != null) {
            if (method.equals(STOP)) {
                DockerHolder.stop(containerId);
                return PageCreator.redirectPage(dataMap);
            }
            if (method.equals(START)) {
                DockerHolder.start(containerId);
                return PageCreator.redirectPage(dataMap);
            }
            if (method.equals(RESTART)) {
                DockerHolder.reStart(containerId);
                return PageCreator.redirectPage(dataMap);
            }
            if (method.equals(RE_PULL)) {
                for (Task task : Data.INSTANCE.getTasks()) {
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
                for (Task task : Data.INSTANCE.getTasks()) {
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

                    for (Task t : new HashSet<>(Data.INSTANCE.getTasks())) {
                        if (t.getId().equals(task.getId())) {
                            Data.INSTANCE.getTasks().remove(t);
                        }
                    }
                    Data.INSTANCE.getTasks().add(task);

                    DockerHolder.startTask(task);
                    return PageCreator.redirectPage(dataMap);
                }
                for (Task task : new HashSet<>(Data.INSTANCE.getTasks())) {
                    if (task.getId().equals(containerName)) {
                        DockerHolder.delete(containerId);
                        Data.INSTANCE.getTasks().remove(task);
                        Task newTask = createTask(map);
                        Data.INSTANCE.getTasks().add(newTask);
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
                for (Task task : new HashSet<>(Data.INSTANCE.getTasks())) {
                    if (task.getId().equals(containerName)) {
                        Data.INSTANCE.getTasks().remove(task);
                    }
                }
                return PageCreator.redirectPage(dataMap);
            }
        }

        if (method.equals(SETTINGS)) {
            return PageCreator.createSettings(dataMap);
        }
        if (method.equals(SETTINGS_OK)) {
            try {
                Data.INSTANCE.setCheckingDelay(Integer.parseInt(map.get(CHECKING_DELAY)));
                Data.INSTANCE.setNextRestartDate(new Date(map.get(NEXT_RESTART_DATE).trim()));
                Data.INSTANCE.setTimeToRestart(Integer.parseInt(map.get(TIME_TO_RESTART)));
                Data.INSTANCE.setMainPage(map.get(MAIN_PAGE).trim());
                Data.writeData();
            } catch (Exception e) {
                //No problem
            }
            return PageCreator.redirectPage(dataMap);
        }

        Data.writeData();

        return createList(Data.INSTANCE.getTasks(), dataMap, Data.INSTANCE.getNextRestartDate());
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
        task.setShouldRunning(Task.ShouldRunning.valueOf(map.get(SHOULD_RUN).trim()));
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
