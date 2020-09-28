package hu.xannosz.blue.queen;

import com.amihaiemil.docker.Container;
import com.amihaiemil.docker.Docker;
import com.amihaiemil.docker.Image;
import com.amihaiemil.docker.TcpDocker;
import hu.xannosz.microtools.pack.Douplet;
import hu.xannosz.veneos.core.HttpHandler;
import hu.xannosz.veneos.core.Page;
import hu.xannosz.veneos.core.VeneosServer;
import hu.xannosz.veneos.core.html.Div;
import hu.xannosz.veneos.core.html.P;
import hu.xannosz.veneos.next.JsonDisplay;
import hu.xannosz.veneos.next.OneButtonForm;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Queen implements HttpHandler {

    private static final String INSPECT = "inspect";
    private static final String STOP = "stop";
    private static final String START = "start";
    private static final String RESTART = "restart";
    private static final String EDIT = "edit";
    private static final String DELETE = "delete";

    private final Data data;
    private final Docker docker;

    public Queen(Data data) {
        this.data = data;
        VeneosServer server = new VeneosServer();
        server.createServer(8888);
        server.setHandler(this);
        docker = new TcpDocker(URI.create("http://localhost:2375"));

        init();
    }

    private void init() {
        for (Task task : data.getTasks()) {
            final Image helloWorld;
            try {
                final Image image = docker.images().pull(task.getImage(), "latest");
                final Container running = image.run();
                running.rename(task.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

        Set<Container> containers = new HashSet<>();
        docker.containers().all().forEachRemaining((containers::add));
        Container container = getContainerFromId(id, containers);


        if (container != null) {
            if (method.equals(INSPECT)) {
                return createInspect(id, container);
            }
            if (method.equals(RESTART)) {
                try {
                    container.restart();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Page page = new Page();
        page.setTitle("Blue Queen");
        for (Task task : data.getTasks()) {
            Div div = new Div();
            try {
                div.add(new P("" + task.getId()));
                div.add(new OneButtonForm("/" + getContainerFromName(task.getId(), containers).containerId() + "/" + STOP, "Stop"));
                div.add(new OneButtonForm("/" + getContainerFromName(task.getId(), containers).containerId() + "/" + RESTART, "Restart"));
                div.add(new OneButtonForm("/" + getContainerFromName(task.getId(), containers).containerId() + "/" + EDIT, "Edit"));
                div.add(new OneButtonForm("/" + getContainerFromName(task.getId(), containers).containerId() + "/" + INSPECT, "Inspect"));
                div.add(new OneButtonForm("/" + getContainerFromName(task.getId(), containers).containerId() + "/" + DELETE, "Delete"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            page.addComponent(div);
        }
        return new Douplet<>(200, page);
    }

    private Douplet<Integer, Page> createInspect(String id, Container container) {
        Page page = new Page();
        page.setTitle("Blue Queen");
        Div div = new Div();
        try {
            page.setTitle("Blue Queen | " + container.inspect().getString("Name"));
            div.add(new JsonDisplay(new JSONObject(container.inspect().toString()), 3, page));
        } catch (IOException e) {
            e.printStackTrace();
        }
        page.addComponent(div);

        return new Douplet<>(200, page);
    }

    private Container getContainerFromId(String id, Set<Container> containers) {
        for (Container cr : containers) {
            if (cr.containerId().equals(id)) {
                return cr;
            }
        }
        return null;
    }

    private Container getContainerFromName(String name, Set<Container> containers) {
        for (Container cr : containers) {
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
