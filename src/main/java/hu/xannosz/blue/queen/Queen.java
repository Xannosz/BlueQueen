package hu.xannosz.blue.queen;

import com.amihaiemil.docker.Container;
import hu.xannosz.microtools.pack.Douplet;
import hu.xannosz.veneos.core.HttpHandler;
import hu.xannosz.veneos.core.Page;
import hu.xannosz.veneos.core.VeneosServer;

import java.io.IOException;
import java.util.Map;

import static hu.xannosz.blue.queen.Constants.*;
import static hu.xannosz.blue.queen.DockerHolder.getContainerFromName;
import static hu.xannosz.blue.queen.PageCreator.*;

public class Queen implements HttpHandler {

    private final Data data;

    public Queen(Data data) {
        this.data = data;
        VeneosServer server = new VeneosServer();
        server.createServer(8888);
        server.setHandler(this);

        init();
    }

    private void init() {
        DockerHolder.startAllTask(data.getTasks());
    }

    @Override
    public Douplet<Integer, Page> getResponse(RequestMethod requestMethod, String s, Map<String, String> map) {

        String[] path = s.split("/");
        String id = "";
        String method = "";
        if (path.length == 3) {
            id = path[1];
            method = path[2];
        }

        Container container = getContainerFromName(id);


        if (container != null) {
            if (method.equals(STOP)) {
                try {
                    container.stop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (method.equals(START)) {
                try {
                    container.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (method.equals(RESTART)) {
                try {
                    container.restart();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (method.equals(EDIT)) {
                return createEdit(container);
            }
            if (method.equals(LOGS)) {
                return createLogs(container);
            }
            if (method.equals(INSPECT)) {
                return createInspect(container);
            }
            if (method.equals(DELETE)) {
                container.clear();
            }
        }

        return createList(data.getTasks());
    }
}
