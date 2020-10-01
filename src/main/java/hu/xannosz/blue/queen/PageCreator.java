package hu.xannosz.blue.queen;

import com.google.gson.Gson;
import com.spotify.docker.client.messages.Container;
import hu.xannosz.microtools.pack.Douplet;
import hu.xannosz.veneos.core.Page;
import hu.xannosz.veneos.core.html.*;
import hu.xannosz.veneos.next.JsonDisplay;
import hu.xannosz.veneos.next.OneButtonForm;
import hu.xannosz.veneos.next.Redirect;
import org.json.JSONObject;

import java.util.Set;

import static hu.xannosz.blue.queen.Constants.*;
import static hu.xannosz.blue.queen.DockerHolder.getValidName;

public class PageCreator {
    public static Douplet<Integer, Page> createInspect(String containerId, String containerName) {
        Page page = new Page();
        page.setTitle("Blue Queen | " + containerName);
        Div div = new Div();
        div.add(new JsonDisplay(new JSONObject(new Gson().toJson(DockerHolder.inspectContainer(containerId))), 3, page));
        page.addComponent(div);
        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> createEdit(Task task) {
        Page page = new Page();
        page.setTitle("Blue Queen | " + task.getId());
        Div div = new Div();

        Form form = new Form("/" + DockerHolder.getContainerIdFromName(task.getId()) + "/" + EDIT_FORM);

        Input id = new Input("text");
        id.setName(ID);
        id.setValue(task.getId());
        form.add(id);

        Input image = new Input("text");
        image.setName(IMAGE);
        image.setValue(task.getImage());
        form.add(image);

        Input run = new Input("text");
        run.setName(SHOULD_RUN);
        run.setValue("" + task.isShouldRunning());
        form.add(run);

        Button send = new Button("Ok");
        send.setSubmit();
        form.add(send);

        div.add(form);

        div.add(new OneButtonForm("/", "Cancel"));

        page.addComponent(div);

        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> createLogs(String containerId, String containerName) {
        Page page = new Page();
        page.setTitle("Blue Queen | " + containerName);
        Div div = new Div();
        div.add(new P(DockerHolder.getContainerLog(containerId)));
        page.addComponent(div);
        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> redirectPage() {
        Page page = new Page();
        page.addComponent(new Redirect("/"));
        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> createList(Set<Task> tasks) {
        Page page = new Page();
        page.setTitle("Blue Queen");
        for (Container container : DockerHolder.getContainers()) {
            Div div = new Div();

            div.add(new P(getValidName(container, tasks) + " " + container.image()
                    + " " + container.state() + " " + container.status()));
            if ("running".equals(container.state())) {
                div.add(new OneButtonForm("/" + container.id() + "/" + STOP, "Stop"));
            } else {
                div.add(new OneButtonForm("/" + container.id() + "/" + START, "Start"));
            }
            div.add(new OneButtonForm("/" + container.id() + "/" + RESTART, "Restart"));
            div.add(new OneButtonForm("/" + container.id() + "/" + RE_PULL, "Re pull"));
            div.add(new OneButtonForm("/" + container.id() + "/" + EDIT, "Edit"));
            div.add(new OneButtonForm("/" + container.id() + "/" + LOGS, "Logs"));
            div.add(new OneButtonForm("/" + container.id() + "/" + INSPECT, "Inspect"));
            div.add(new OneButtonForm("/" + container.id() + "/" + DELETE, "Delete"));

            page.addComponent(div);
        }
        return new Douplet<>(200, page);
    }
}
