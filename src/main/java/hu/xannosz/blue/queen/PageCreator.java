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
import hu.xannosz.veneos.next.*;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

import static hu.xannosz.blue.queen.Constants.*;
import static hu.xannosz.blue.queen.DockerHolder.getValidName;
import static hu.xannosz.blue.queen.DockerHolder.isManaged;

public class PageCreator {

    private static final Theme theme = new Theme();
    private static final HtmlClass oneButton = new HtmlClass();
    private static final HtmlClass delete = new HtmlClass();

    private static final HtmlClass containerName = new HtmlClass();
    private static final HtmlClass label = new HtmlClass();

    private static final HtmlClass running = new HtmlClass();
    private static final HtmlClass exited = new HtmlClass();

    private static final HtmlClass managed = new HtmlClass();
    private static final HtmlClass notManaged = new HtmlClass();

    private static final HtmlClass upButton = new HtmlClass();

    static {
        CssComponent pre = new CssComponent(HtmlSelector.PRE);
        pre.addAttribute(CssAttribute.COLOR, "#268bd2");
        pre.addAttribute(CssAttribute.PADDING, "2px");
        pre.addAttribute(CssAttribute.MARGIN, "2%");
        pre.addAttribute(CssAttribute.BACKGROUND_COLOR, "#073642");
        theme.add(pre);

        CssComponent div = new CssComponent(HtmlSelector.DIV);
        div.addAttribute(CssAttribute.COLOR, "#268bd2");
        div.addAttribute(CssAttribute.PADDING, "10px 20px 15px 20px");
        div.addAttribute(CssAttribute.MARGIN, "2%");
        div.addAttribute(CssAttribute.BACKGROUND_COLOR, "#073642");
        div.addAttribute(CssAttribute.BORDER_RADIUS, "25px");
        theme.add(div);

        CssComponent oneBut = new CssComponent(new Selector(oneButton));
        oneBut.addAttribute(CssAttribute.FLOAT, "left");
        oneBut.addAttribute(CssAttribute.MARGIN, "2px");
        theme.add(oneBut);

        CssComponent oneButInline = new CssComponent(HtmlSelector.BUTTON.getSelector());
        oneButInline.addAttribute(CssAttribute.BACKGROUND_COLOR, "#073642");
        oneButInline.addAttribute(CssAttribute.COLOR, "#268bd2");
        oneButInline.addAttribute(CssAttribute.BORDER_RADIUS, "25px");
        oneButInline.addAttribute(CssAttribute.BORDER_COLOR, "#002b36");
        theme.add(oneButInline);

        CssComponent deleteComp = new CssComponent(new Selector(new Selector(delete).getSyntax() + " " + HtmlSelector.BUTTON));
        deleteComp.addAttribute(CssAttribute.BACKGROUND_COLOR, "#dc322f");
        theme.add(deleteComp);

        CssComponent buttonHover = new CssComponent(new Selector(HtmlSelector.BUTTON.toString() + ":hover"));
        buttonHover.addAttribute(CssAttribute.BACKGROUND_COLOR, "#002b36");
        buttonHover.addAttribute(CssAttribute.BORDER_COLOR, "#073642");
        theme.add(buttonHover);

        CssComponent buttonClicked = new CssComponent(new Selector(HtmlSelector.BUTTON.toString() + ":active"));
        buttonClicked.addAttribute(CssAttribute.COLOR, "#6c71c4");
        theme.add(buttonClicked);

        CssComponent input = new CssComponent(HtmlSelector.INPUT.getSelector());
        input.addAttribute(CssAttribute.BACKGROUND_COLOR, "#073642");
        input.addAttribute(CssAttribute.COLOR, "#268bd2");
        input.addAttribute(CssAttribute.BORDER_RADIUS, "25px");
        input.addAttribute(CssAttribute.BORDER_COLOR, "#002b36");
        theme.add(input);

        CssComponent notManagedComp = new CssComponent(new Selector(notManaged));
        notManagedComp.addAttribute(CssAttribute.COLOR, "#dc322f");
        theme.add(notManagedComp);

        CssComponent managedComp = new CssComponent(new Selector(managed));
        managedComp.addAttribute(CssAttribute.COLOR, "#859900");
        theme.add(managedComp);

        CssComponent containerNameComp = new CssComponent(new Selector(containerName));
        containerNameComp.addAttribute(CssAttribute.FONT_WEIGHT, "bold");
        theme.add(containerNameComp);

        CssComponent labelComp = new CssComponent(new Selector(label));
        labelComp.addAttribute(CssAttribute.COLOR, "#586e75");
        labelComp.addAttribute(CssAttribute.FONT_STYLE, "italic");
        theme.add(labelComp);

        CssComponent runningComp = new CssComponent(new Selector(running));
        runningComp.addAttribute(CssAttribute.COLOR, "#859900");
        theme.add(runningComp);

        CssComponent exitedComp = new CssComponent(new Selector(exited));
        exitedComp.addAttribute(CssAttribute.COLOR, "#dc322f");
        theme.add(exitedComp);

        CssComponent upButtonComp = new CssComponent(new Selector(upButton));
        upButtonComp.addAttribute(CssAttribute.DISPLAY, "none");
        theme.add(upButtonComp);

        CssComponent body = new CssComponent(HtmlSelector.BODY);
        body.addAttribute(CssAttribute.BACKGROUND_COLOR, "#002b36");
        theme.add(body);

        ThemeHandler.registerTheme(theme);
    }

    public static Douplet<Integer, Page> createInspect(String containerId, String containerName, Map<String, String> dataMap) {
        Page page = new Page();
        page.addTheme(theme);
        page.setTitle("Blue Queen | " + containerName);
        Div div = new Div();
        div.add(new JsonDisplay(new JSONObject(new Gson().toJson(DockerHolder.inspectContainer(containerId))), 3, page));
        page.addComponent(div);
        page.addComponent(new ScrollUpButton("Top", new ButtonPosition("10%", "10%"), page).setDatas(dataMap).addClass(upButton));
        FixedButton fixedButton = new FixedButton("/", "Cancel", new ButtonPosition("10%", "20%"));
        fixedButton.setDatas(dataMap);
        page.addComponent(fixedButton);
        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> createEdit(Task task, Map<String, String> dataMap) {
        boolean newTask = task == null;

        Page page = new Page();
        page.addTheme(theme);
        page.setTitle("Blue Queen | " + (newTask ? "New Task" : task.getId()));
        Div div = new Div();

        Form form = new Form("/" + (newTask ? "new" : DockerHolder.getContainerIdFromName(task.getId())) + "/" + EDIT_FORM);
        form.setDatas(dataMap);

        form.add(new Label(ID, " task id: ").addClass(label));
        Input id = new Input("text");
        id.setName(ID);
        if (!newTask) {
            id.setValue(task.getId());
        }
        form.add(id);

        form.add(new Label(IMAGE, " image: ").addClass(label));
        Input image = new Input("text");
        image.setName(IMAGE);
        if (!newTask) {
            image.setValue(task.getImage());
        }
        form.add(image);

        form.add(new Label(SHOULD_RUN, " should run (bool) : ").addClass(label));
        Input run = new Input("text");
        run.setName(SHOULD_RUN);
        if (!newTask) {
            run.setValue("" + task.isShouldRunning());
        }
        form.add(run);

        form.add(new StringHtmlComponent(StringModifiers.BR.toString()));

        form.add(new Label("ports", "ports : ").addClass(label));
        form.add(new StringHtmlComponent(StringModifiers.BR.toString()));
        form.add(new Label("ports", "host............|").addClass(label));
        form.add(new Label("ports", "|............docker").addClass(label));
        form.add(new StringHtmlComponent(StringModifiers.BR.toString()));
        int p = 0; //portNumber
        if (!newTask) {
            for (Douplet<Integer, Integer> port : task.getPorts()) {
                Input inputH = new Input("text");
                inputH.setName(PORT + "H" + p);
                inputH.setValue("" + port.getFirst());
                form.add(inputH);

                Input inputD = new Input("text");
                inputD.setName(PORT + "D" + p);
                inputD.setValue("" + port.getSecond());
                form.add(inputD);

                form.add(new StringHtmlComponent(StringModifiers.BR.toString()));
                p++;
            }
        }
        for (int i = 0; i < 3; i++) {
            Input inputH = new Input("text");
            inputH.setName(PORT + "H" + p);
            form.add(inputH);

            Input inputD = new Input("text");
            inputD.setName(PORT + "D" + p);
            form.add(inputD);

            form.add(new StringHtmlComponent(StringModifiers.BR.toString()));
            p++;
        }

        form.add(new Label("volumes", "volumes : ").addClass(label));
        form.add(new StringHtmlComponent(StringModifiers.BR.toString()));
        form.add(new Label("volumes", "host............|").addClass(label));
        form.add(new Label("volumes", "|............docker").addClass(label));
        form.add(new StringHtmlComponent(StringModifiers.BR.toString()));
        int v = 0; //volumeNumber
        if (!newTask) {
            for (Douplet<String, String> port : task.getVolumes()) {
                Input inputH = new Input("text");
                inputH.setName(VOLUME + "H" + v);
                inputH.setValue("" + port.getFirst());
                form.add(inputH);

                Input inputD = new Input("text");
                inputD.setName(VOLUME + "D" + v);
                inputD.setValue("" + port.getSecond());
                form.add(inputD);

                form.add(new StringHtmlComponent(StringModifiers.BR.toString()));
                v++;
            }
        }
        for (int i = 0; i < 3; i++) {
            Input inputH = new Input("text");
            inputH.setName(VOLUME + "H" + v);
            form.add(inputH);

            Input inputD = new Input("text");
            inputD.setName(VOLUME + "D" + v);
            form.add(inputD);

            form.add(new StringHtmlComponent(StringModifiers.BR.toString()));
            v++;
        }

        Button send = new Button("Ok");
        send.setSubmit();
        form.add(send);

        div.add(form);

        div.add(new OneButtonForm("/", "Cancel").setDatas(dataMap).addClass(oneButton));

        page.addComponent(div);

        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> createLogs(String containerId, String containerName, Map<String, String> dataMap) {
        Page page = new Page();
        page.addTheme(theme);
        page.setTitle("Blue Queen | " + containerName);
        Div div = new Div();
        try {
            div.add(new JsonDisplay(new JSONObject(DockerHolder.getContainerLog(containerId)), 3, page));
        } catch (Exception e) {
            div.add(new P(DockerHolder.getContainerLog(containerId)));
        }
        page.addComponent(div);
        page.addComponent(new ScrollUpButton("Top", new ButtonPosition("10%", "10%"), page).setDatas(dataMap).addClass(upButton));
        FixedButton fixedButton = new FixedButton("/", "Cancel", new ButtonPosition("10%", "20%"));
        fixedButton.setDatas(dataMap);
        page.addComponent(fixedButton);
        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> redirectPage(Map<String, String> dataMap) {
        Page page = new Page();
        page.addTheme(theme);
        Redirect redirect = new Redirect("/", page);
        redirect.setDatas(dataMap);
        page.addComponent(redirect);
        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> createList(Set<Task> tasks, Map<String, String> dataMap) {
        Page page = new Page();
        page.addTheme(theme);
        page.setTitle("Blue Queen");
        for (Container container : Util.sortContainers(DockerHolder.getContainers())) {
            Div div = new Div();
            boolean isManaged = isManaged(container, tasks);

            div.add(createInformationLine(getValidName(container, tasks), container.image()
                    , "" + container.state(), container.status(), isManaged));
            if ("running".equals(container.state())) {
                div.add(new OneButtonForm("/" + container.id() + "/" + STOP, "Stop").setDatas(dataMap).addClass(oneButton));
            } else {
                div.add(new OneButtonForm("/" + container.id() + "/" + START, "Start").setDatas(dataMap).addClass(oneButton));
            }
            div.add(new OneButtonForm("/" + container.id() + "/" + RESTART, "Restart").setDatas(dataMap).addClass(oneButton));
            if (isManaged) {
                div.add(new OneButtonForm("/" + container.id() + "/" + RE_PULL, "Re pull").setDatas(dataMap).addClass(oneButton));
                div.add(new OneButtonForm("/" + container.id() + "/" + EDIT, "Edit").setDatas(dataMap).addClass(oneButton));
            }
            div.add(new OneButtonForm("/" + container.id() + "/" + LOGS, "Logs").setDatas(dataMap).addClass(oneButton));
            div.add(new OneButtonForm("/" + container.id() + "/" + INSPECT, "Inspect").setDatas(dataMap).addClass(oneButton));
            div.add(new OneButtonForm("/" + container.id() + "/" + DELETE, "Delete").setDatas(dataMap).addClass(oneButton));

            page.addComponent(div);
        }

        page.addComponent(new FixedButton("/new/edit", "Add new docker", new ButtonPosition("10%", "10%")).setDatas(dataMap));
        page.addComponent(new Redirect("/", 5000, page).setDatas(dataMap));

        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> createLoginPage() {
        Page page = new Login("/", "Log in", "user name :", "password :");
        page.setTitle("Blue Queen");
        page.addTheme(theme);
        return new Douplet<>(200, page);
    }

    private static HtmlComponent createInformationLine(String validName, String image, String state, String status, boolean isManaged) {
        StringBuilder builder = new StringBuilder();

        builder.append(new Span(validName).addClass(containerName).getSyntax()).append(StringModifiers.BR);
        builder.append(" ").append(new Span("image:").addClass(label).getSyntax()).append(" ").append(image);

        if (state.equals("running")) {
            builder.append(" ").append(new Span("state:").addClass(label).getSyntax()).append(" ")
                    .append(new Span(state).addClass(running).getSyntax());
        } else if (state.equals("created") || state.equals("exited")) {
            builder.append(" ").append(new Span("state:").addClass(label).getSyntax()).append(" ")
                    .append(new Span(state).addClass(exited).getSyntax());
        } else {
            builder.append(" ").append(new Span("state:").addClass(label).getSyntax()).append(" ").append(state);
        }

        builder.append(" ").append(new Span("status:").addClass(label).getSyntax()).append(" ").append(status);

        builder.append(" ").append(new Span("is managed:").addClass(label).getSyntax());
        if (isManaged) {
            builder.append(" ").append(new Span("MANAGED").addClass(managed).getSyntax());
        } else {
            builder.append(" ").append(new Span("NOT MANAGED").addClass(notManaged).getSyntax());
        }
        return new P(builder.toString());
    }

    public static Douplet<Integer, Page> createDelete(String containerId, String containerName, Map<String, String> dataMap) {
        Page page = new Page();
        page.addTheme(theme);
        page.setTitle("Blue Queen | " + containerName);
        Div div = new Div();
        div.add(new P("Delete " + containerName + " ?"));
        div.add(new OneButtonForm("/" + containerId + "/" + DELETE_OK, "Delete").setDatas(dataMap).addClass(oneButton).addClass(delete));
        div.add(new OneButtonForm("/", "Cancel").setDatas(dataMap).addClass(oneButton));
        page.addComponent(div);
        return new Douplet<>(200, page);
    }
}
