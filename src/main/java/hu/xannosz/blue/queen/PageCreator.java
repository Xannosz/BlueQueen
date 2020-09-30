package hu.xannosz.blue.queen;

import com.amihaiemil.docker.Container;
import hu.xannosz.microtools.pack.Douplet;
import hu.xannosz.veneos.core.Page;
import hu.xannosz.veneos.core.html.Div;
import hu.xannosz.veneos.core.html.Form;
import hu.xannosz.veneos.core.html.Input;
import hu.xannosz.veneos.core.html.P;
import hu.xannosz.veneos.next.JsonDisplay;
import hu.xannosz.veneos.next.OneButtonForm;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

import static hu.xannosz.blue.queen.Constants.*;

public class PageCreator {
    public static Douplet<Integer, Page> createInspect(Container container) {
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

    public static Douplet<Integer, Page> createEdit(Container container) {
        Page page = new Page();
        page.setTitle("Blue Queen");
        Div div = new Div();
        try {
            page.setTitle("Blue Queen | " + container.inspect().getString("Name"));
            Form form = new Form("/" + container.inspect().getString("Name") + "/" +EDIT_FORM);
           Input input= new Input("text");
            div.add(form);
        } catch (IOException e) {
            e.printStackTrace();
        }
        page.addComponent(div);

        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> createLogs(Container container) {
        Page page = new Page();
        page.setTitle("Blue Queen");
        Div div = new Div();
        try {
            page.setTitle("Blue Queen | " + container.inspect().getString("Name"));
            div.add(new JsonDisplay(new JSONObject(container.logs()), 3, page));
        } catch (IOException e) {
            e.printStackTrace();
        }
        page.addComponent(div);

        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> createList(Set<Task> tasks) {
        Page page = new Page();
        page.setTitle("Blue Queen");
        for (Task task : tasks) {
            Div div = new Div();
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            page.addComponent(div);
        }
        return new Douplet<>(200, page);
    }
}
