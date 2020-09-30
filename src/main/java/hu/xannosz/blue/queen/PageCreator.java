package hu.xannosz.blue.queen;

import com.google.gson.Gson;
import hu.xannosz.microtools.pack.Douplet;
import hu.xannosz.veneos.core.Page;
import hu.xannosz.veneos.core.html.Div;
import hu.xannosz.veneos.core.html.Form;
import hu.xannosz.veneos.core.html.Input;
import hu.xannosz.veneos.core.html.P;
import hu.xannosz.veneos.next.JsonDisplay;
import hu.xannosz.veneos.next.OneButtonForm;
import org.json.JSONObject;

import java.util.Set;

import static hu.xannosz.blue.queen.Constants.*;

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

        Form form = new Form("/" + task.getId() + "/" + EDIT_FORM);

        Input id = new Input("text");
        id.setName("id");
        id.setValue(task.getId());
        form.add(id);

        Input image = new Input("text");
        image.setName("image");
        image.setValue(task.getImage());
        form.add(image);

        div.add(form);

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

    public static Douplet<Integer, Page> createList(Set<Task> tasks) {
        Page page = new Page();
        page.setTitle("Blue Queen");
        for (Task task : tasks) {
            Div div = new Div();

            div.add(new P("" + task.getId()));
            if (task.isShouldRunning()) {
                div.add(new OneButtonForm("/" + task.getId() + "/" + STOP, "Stop"));
            } else {
                div.add(new OneButtonForm("/" + task.getId() + "/" + START, "Start"));
            }
            div.add(new OneButtonForm("/" + task.getId() + "/" + RESTART, "Restart"));
            div.add(new OneButtonForm("/" + task.getId() + "/" + EDIT, "Edit"));
            div.add(new OneButtonForm("/" + task.getId() + "/" + LOGS, "Logs"));
            div.add(new OneButtonForm("/" + task.getId() + "/" + INSPECT, "Inspect"));
            div.add(new OneButtonForm("/" + task.getId() + "/" + DELETE, "Delete"));

            page.addComponent(div);
        }
        return new Douplet<>(200, page);
    }
}
