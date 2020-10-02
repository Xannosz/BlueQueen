package hu.xannosz.blue.queen;

import com.google.gson.Gson;
import com.spotify.docker.client.messages.Container;
import hu.xannosz.microtools.pack.Douplet;
import hu.xannosz.veneos.core.HtmlClass;
import hu.xannosz.veneos.core.Page;
import hu.xannosz.veneos.core.Theme;
import hu.xannosz.veneos.core.ThemeHandler;
import hu.xannosz.veneos.core.css.CssAttribute;
import hu.xannosz.veneos.core.css.CssComponent;
import hu.xannosz.veneos.core.css.HtmlSelector;
import hu.xannosz.veneos.core.css.Selector;
import hu.xannosz.veneos.core.html.*;
import hu.xannosz.veneos.next.JsonDisplay;
import hu.xannosz.veneos.next.OneButtonForm;
import hu.xannosz.veneos.next.Redirect;
import org.json.JSONObject;

import java.util.Set;

import static hu.xannosz.blue.queen.Constants.*;
import static hu.xannosz.blue.queen.DockerHolder.getValidName;
import static hu.xannosz.blue.queen.DockerHolder.isManaged;

public class PageCreator {

    private static final Theme theme = new Theme();
    private static final HtmlClass oneButton = new HtmlClass();

    static {

        CssComponent all = new CssComponent(new Selector(".all"));
        all.addAttribute(CssAttribute.BORDER_RADIUS, "25px");
        theme.add(all);

        CssComponent div = new CssComponent(HtmlSelector.DIV);
        div.addAttribute(CssAttribute.COLOR, "#268bd2");
        div.addAttribute(CssAttribute.PADDING, "20px");
        div.addAttribute(CssAttribute.MARGIN, "2%");
        div.addAttribute(CssAttribute.BACKGROUND_COLOR, "#073642");
        theme.add(div);

        CssComponent oneBut = new CssComponent(new Selector(oneButton));
        oneBut.addAttribute(CssAttribute.FLOAT, "left");
        oneBut.addAttribute(CssAttribute.MARGIN, "2px");
        theme.add(oneBut);

        CssComponent em = new CssComponent(HtmlSelector.EM);
        em.addAttribute(CssAttribute.COLOR, "#dc322f");
        em.addAttribute(CssAttribute.TEXT_DECORATION,"none");
        theme.add(em);

        CssComponent strong = new CssComponent(HtmlSelector.STRONG);
        strong.addAttribute(CssAttribute.COLOR, "#859900");
        theme.add(strong);

        CssComponent i = new CssComponent(HtmlSelector.I);
        i.addAttribute(CssAttribute.COLOR, "#586e75");
        theme.add(i);

        CssComponent body = new CssComponent(HtmlSelector.BODY);
        body.addAttribute(CssAttribute.BACKGROUND_COLOR, "#002b36");
        theme.add(body);

        ThemeHandler.registerTheme(theme);
    }

    public static Douplet<Integer, Page> createInspect(String containerId, String containerName) {
        Page page = new Page();
        page.addTheme(theme);
        page.setTitle("Blue Queen | " + containerName);
        Div div = new Div();
        div.add(new JsonDisplay(new JSONObject(new Gson().toJson(DockerHolder.inspectContainer(containerId))), 3, page));
        page.addComponent(div);
        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> createEdit(Task task) {
        Page page = new Page();
        page.addTheme(theme);
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
        page.addTheme(theme);
        page.setTitle("Blue Queen | " + containerName);
        Div div = new Div();
        div.add(new P(DockerHolder.getContainerLog(containerId)));
        page.addComponent(div);
        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> redirectPage() {
        Page page = new Page();
        page.addTheme(theme);
        page.addComponent(new Redirect("/"));
        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> createList(Set<Task> tasks) {
        Page page = new Page();
        page.addTheme(theme);
        page.setTitle("Blue Queen");
        page.setAutoRefresh(5);
        for (Container container : Util.sortContainers(DockerHolder.getContainers())) {
            Div div = new Div();
            boolean isManaged = isManaged(container, tasks);

            div.add(createInformationLine(getValidName(container, tasks), container.image()
                    , container.state(), container.status(), isManaged));
            if ("running".equals(container.state())) {
                div.add(new OneButtonForm("/" + container.id() + "/" + STOP, "Stop").addClass(oneButton));
            } else {
                div.add(new OneButtonForm("/" + container.id() + "/" + START, "Start").addClass(oneButton));
            }
            div.add(new OneButtonForm("/" + container.id() + "/" + RESTART, "Restart").addClass(oneButton));
            if (isManaged) {
                div.add(new OneButtonForm("/" + container.id() + "/" + RE_PULL, "Re pull").addClass(oneButton));
                div.add(new OneButtonForm("/" + container.id() + "/" + EDIT, "Edit").addClass(oneButton));
            }
            div.add(new OneButtonForm("/" + container.id() + "/" + LOGS, "Logs").addClass(oneButton));
            div.add(new OneButtonForm("/" + container.id() + "/" + INSPECT, "Inspect").addClass(oneButton));
            div.add(new OneButtonForm("/" + container.id() + "/" + DELETE, "Delete").addClass(oneButton));

            page.addComponent(div);
        }
        return new Douplet<>(200, page);
    }

    private static HtmlComponent createInformationLine(String validName, String image, String state, String status, boolean isManaged) {
        StringBuilder builder = new StringBuilder();
        builder.append(StringModifiers.B.set(validName)).append(StringModifiers.BR);
        builder.append(" ").append(StringModifiers.I.set("image:")).append(" ").append(image);
        builder.append(" ").append(StringModifiers.I.set("state:")).append(" ").append(state);
        builder.append(" ").append(StringModifiers.I.set("status:")).append(" ").append(status);
        if (isManaged) {
            builder.append(" ").append(StringModifiers.STRONG.set("MANAGED"));
        } else {
            builder.append(" ").append(StringModifiers.EM.set("NOT MANAGED"));
        }
        return new P(builder.toString());
    }
}
