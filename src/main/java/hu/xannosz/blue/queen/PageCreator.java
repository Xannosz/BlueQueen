package hu.xannosz.blue.queen;

import com.google.gson.Gson;
import com.spotify.docker.client.messages.Container;
import hu.xannosz.microtools.pack.Douplet;
import hu.xannosz.veneos.core.css.CssComponent;
import hu.xannosz.veneos.core.css.CssProperty;
import hu.xannosz.veneos.core.css.Theme;
import hu.xannosz.veneos.core.css.selector.HtmlSelector;
import hu.xannosz.veneos.core.css.selector.ParameterizedPseudoClass;
import hu.xannosz.veneos.core.css.selector.PseudoClass;
import hu.xannosz.veneos.core.html.HtmlClass;
import hu.xannosz.veneos.core.html.HtmlComponent;
import hu.xannosz.veneos.core.html.box.Div;
import hu.xannosz.veneos.core.html.box.Footer;
import hu.xannosz.veneos.core.html.form.*;
import hu.xannosz.veneos.core.html.str.P;
import hu.xannosz.veneos.core.html.str.Span;
import hu.xannosz.veneos.core.html.str.StringModifiers;
import hu.xannosz.veneos.core.html.structure.Page;
import hu.xannosz.veneos.core.html.table.Table;
import hu.xannosz.veneos.next.*;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static hu.xannosz.blue.queen.Constants.*;
import static hu.xannosz.blue.queen.DockerHolder.*;

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
    private static final HtmlClass copyJsonButton = new HtmlClass();

    public static final String BODY_BACKGROUND_COLOR = "#002b36";
    public static final String DIV_BACKGROUND_COLOR = "#073642";
    public static final String NORMAL_TEXT_COLOR = "#268bd2";
    public static final String RED = "#dc322f";
    public static final String GREEN = "#859900";
    public static final String GRAY = "#586e75";
    public static final String BUTTON_CLICKED_COLOR = "#6c71c4";

    static {
        CssComponent pre = new CssComponent(HtmlSelector.PRE);
        pre.addProperty(CssProperty.COLOR, NORMAL_TEXT_COLOR);
        pre.addProperty(CssProperty.PADDING, "2px");
        pre.addProperty(CssProperty.MARGIN, "2%");
        pre.addProperty(CssProperty.BACKGROUND_COLOR, DIV_BACKGROUND_COLOR);
        theme.add(pre);

        CssComponent div = new CssComponent(HtmlSelector.DIV);
        div.addProperty(CssProperty.COLOR, NORMAL_TEXT_COLOR);
        div.addProperty(CssProperty.PADDING, "10px 20px 15px 20px");
        div.addProperty(CssProperty.MARGIN, "2%");
        div.addProperty(CssProperty.BACKGROUND_COLOR, DIV_BACKGROUND_COLOR);
        div.addProperty(CssProperty.BORDER_RADIUS, "25px");
        theme.add(div);

        CssComponent oneBut = new CssComponent(oneButton.getSelector());
        oneBut.addProperty(CssProperty.FLOAT, "left");
        oneBut.addProperty(CssProperty.MARGIN, "2px");
        theme.add(oneBut);

        CssComponent oneButInline = new CssComponent(HtmlSelector.BUTTON);
        oneButInline.addProperty(CssProperty.BACKGROUND_COLOR, DIV_BACKGROUND_COLOR);
        oneButInline.addProperty(CssProperty.COLOR, NORMAL_TEXT_COLOR);
        oneButInline.addProperty(CssProperty.BORDER_RADIUS, "25px");
        oneButInline.addProperty(CssProperty.BORDER_COLOR, BODY_BACKGROUND_COLOR);
        theme.add(oneButInline);

        CssComponent deleteComp = new CssComponent(delete.getSelector().descendant(HtmlSelector.BUTTON.getSelector()));
        deleteComp.addProperty(CssProperty.BACKGROUND_COLOR, RED);
        theme.add(deleteComp);

        CssComponent buttonHover = new CssComponent(HtmlSelector.BUTTON.getSelector().addPseudoClass(PseudoClass.HOVER));
        buttonHover.addProperty(CssProperty.BACKGROUND_COLOR, BODY_BACKGROUND_COLOR);
        buttonHover.addProperty(CssProperty.BORDER_COLOR, DIV_BACKGROUND_COLOR);
        theme.add(buttonHover);

        CssComponent buttonClicked = new CssComponent(HtmlSelector.BUTTON.getSelector().addPseudoClass(PseudoClass.ACTIVE));
        buttonClicked.addProperty(CssProperty.COLOR, BUTTON_CLICKED_COLOR);
        theme.add(buttonClicked);

        CssComponent input = new CssComponent(HtmlSelector.INPUT);
        input.addProperty(CssProperty.BACKGROUND_COLOR, DIV_BACKGROUND_COLOR);
        input.addProperty(CssProperty.COLOR, NORMAL_TEXT_COLOR);
        input.addProperty(CssProperty.BORDER_RADIUS, "25px");
        input.addProperty(CssProperty.BORDER_COLOR, BODY_BACKGROUND_COLOR);
        theme.add(input);

        CssComponent notManagedComp = new CssComponent(notManaged.getSelector());
        notManagedComp.addProperty(CssProperty.COLOR, RED);
        theme.add(notManagedComp);

        CssComponent managedComp = new CssComponent(managed.getSelector());
        managedComp.addProperty(CssProperty.COLOR, GREEN);
        theme.add(managedComp);

        CssComponent containerNameComp = new CssComponent(containerName.getSelector());
        containerNameComp.addProperty(CssProperty.FONT_WEIGHT, "bold");
        theme.add(containerNameComp);

        CssComponent labelComp = new CssComponent(label.getSelector());
        labelComp.addProperty(CssProperty.COLOR, GRAY);
        labelComp.addProperty(CssProperty.FONT_STYLE, "italic");
        theme.add(labelComp);

        CssComponent runningComp = new CssComponent(running.getSelector());
        runningComp.addProperty(CssProperty.COLOR, GREEN);
        theme.add(runningComp);

        CssComponent exitedComp = new CssComponent(exited.getSelector());
        exitedComp.addProperty(CssProperty.COLOR, RED);
        theme.add(exitedComp);

        CssComponent upButtonComp = new CssComponent(upButton.getSelector());
        upButtonComp.addProperty(CssProperty.DISPLAY, "none");
        theme.add(upButtonComp);

        CssComponent copyJsonButtonComp = new CssComponent(copyJsonButton.getSelector());
        copyJsonButtonComp.addProperty(CssProperty.MARGIN, "3%");
        theme.add(copyJsonButtonComp);

        CssComponent th = new CssComponent(HtmlSelector.TH.getSelector());
        th.addProperty(CssProperty.TEXT_DECORATION, "underline");
        th.addProperty(CssProperty.PADDING, "8px");
        theme.add(th);

        CssComponent td = new CssComponent(HtmlSelector.TD.getSelector());
        td.addProperty(CssProperty.PADDING, "8px");
        theme.add(td);

        CssComponent tdFirst = new CssComponent(HtmlSelector.TD.getSelector().addPseudoClass(PseudoClass.FIRST_CHILD));
        tdFirst.addProperty(CssProperty.BORDER_RADIUS, "25px 0px 0px 25px");
        theme.add(tdFirst);

        CssComponent tdLast = new CssComponent(HtmlSelector.TD.getSelector().addPseudoClass(PseudoClass.LAST_CHILD));
        tdLast.addProperty(CssProperty.BORDER_RADIUS, "0px 25px 25px 0px");
        theme.add(tdLast);

        CssComponent tr = new CssComponent(HtmlSelector.TR.getSelector().addPseudoClass(ParameterizedPseudoClass.NTH_CHILD, "even"));
        tr.addProperty(CssProperty.BACKGROUND_COLOR, BODY_BACKGROUND_COLOR);
        theme.add(tr);

        CssComponent body = new CssComponent(HtmlSelector.BODY);
        body.addProperty(CssProperty.BACKGROUND_COLOR, BODY_BACKGROUND_COLOR);
        theme.add(body);

        CssComponent footer = new CssComponent(HtmlSelector.FOOTER);
        footer.addProperty(CssProperty.COLOR, NORMAL_TEXT_COLOR);
        footer.addProperty(CssProperty.PADDING, "10px 20px 15px 20px");
        footer.addProperty(CssProperty.MARGIN, "2%");
        footer.addProperty(CssProperty.BACKGROUND_COLOR, DIV_BACKGROUND_COLOR);
        footer.addProperty(CssProperty.BORDER_RADIUS, "25px");
        theme.add(footer);
    }

    public static Douplet<Integer, Page> createInspect(String containerId, String containerName, Map<String, String> dataMap) {
        Page page = new Page();
        page.addTheme(theme);
        page.setTitle("Blue Queen | " + containerName);
        Div div = new Div();
        div.add(new JsonDisplay(new JSONObject(new Gson().toJson(DockerHolder.inspectContainer(containerId))), 3, page));
        page.addComponent(div);
        page.addComponent(new ScrollUpButton("Top", new ButtonPosition("10%", "10%"), page).setDatas(dataMap).addClass(upButton));
        FixedButton fixedButton = new FixedButton("/", "Cancel", new ButtonPosition("10%", "20%"), false);
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

        Form form = new Form("/" + (newTask ? "new" : DockerHolder.getContainerIdFromName(task.getId())) + "/" + EDIT_FORM, false);
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

        form.add(new Label(SHOULD_RUN, " should run: ").addClass(label));
        Select runSelect = new Select(SHOULD_RUN);
        for (Task.ShouldRunning value : Task.ShouldRunning.values()) {
            Option option = new Option(value.toString(), value.toString());
            runSelect.add(option);
            if (!newTask && value == task.getShouldRunning()) {
                option.putMeta("selected", "selected");
            }
        }
        form.add(runSelect);

        form.add(StringModifiers.BR.toString());

        form.add(new Label("ports", "ports : ").addClass(label));
        form.add(StringModifiers.BR.toString());
        form.add(new Label("ports", "host............|").addClass(label));
        form.add(new Label("ports", "|............docker").addClass(label));
        form.add(StringModifiers.BR.toString());
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

                form.add(StringModifiers.BR.toString());
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

            form.add(StringModifiers.BR.toString());
            p++;
        }

        form.add(new Label("volumes", "volumes : ").addClass(label));
        form.add(StringModifiers.BR.toString());
        form.add(new Label("volumes", "host............|").addClass(label));
        form.add(new Label("volumes", "|............docker").addClass(label));
        form.add(StringModifiers.BR.toString());
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

                form.add(StringModifiers.BR.toString());
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

            form.add(StringModifiers.BR.toString());
            v++;
        }

        Button send = new Button("Ok");
        send.setSubmit();
        form.add(send);

        div.add(form);

        div.add(new OneButtonForm("/", "Cancel", false).setDatas(dataMap).addClass(oneButton));

        page.addComponent(div);

        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> createLogs(String containerId, String containerName, Map<String, String> dataMap) {
        Page page = new Page();
        page.addTheme(theme);
        page.setTitle("Blue Queen | " + containerName);
        Div div = new Div();

        String log = DockerHolder.getContainerLog(containerId);
        page.addComponent(new CopyButton("Get logs in json", page, log).addClass(copyJsonButton));
        try {
            ContainerLog containerLog = new Gson().fromJson(log, ContainerLog.class);
            Collections.reverse(containerLog.getLogs());

            Table table = new Table();
            table.addHeadCell("Time").addHeadCell("Stream").addHeadCell("Log");
            for (ContainerLog.LogLine line : containerLog.getLogs()) {
                table.addCell(line.getTime()).addCell(line.getStream()).addCell(line.getLog()).newRow();
            }

            div.add(table);
        } catch (Exception e) {
            div.add(new P(log));
        }

        page.addComponent(div);
        page.addComponent(new ScrollUpButton("Top", new ButtonPosition("10%", "10%"), page).setDatas(dataMap).addClass(upButton));
        FixedButton fixedButton = new FixedButton("/", "Cancel", new ButtonPosition("10%", "20%"), false);
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

    public static Douplet<Integer, Page> createList(Set<Task> tasks, Map<String, String> dataMap, Date reStartDate) {
        Page page = new Page();
        page.addTheme(theme);
        page.setTitle("Blue Queen");
        for (Container container : Util.sortContainers(DockerHolder.getContainers())) {
            Div div = new Div();
            boolean isManaged = isManaged(container, tasks);

            div.add(createInformationLine(getValidName(container, tasks), container.image()
                    , "" + container.state(), container.status(), isManaged, getShouldRun(container, tasks)));
            if ("running".equals(container.state())) {
                div.add(new OneButtonForm("/" + container.id() + "/" + STOP, "Stop", false).setDatas(dataMap).addClass(oneButton));
            } else {
                div.add(new OneButtonForm("/" + container.id() + "/" + START, "Start", false).setDatas(dataMap).addClass(oneButton));
            }
            div.add(new OneButtonForm("/" + container.id() + "/" + RESTART, "Restart", false).setDatas(dataMap).addClass(oneButton));
            if (isManaged) {
                div.add(new OneButtonForm("/" + container.id() + "/" + RE_PULL, "Re pull", false).setDatas(dataMap).addClass(oneButton));
                div.add(new OneButtonForm("/" + container.id() + "/" + EDIT, "Edit", false).setDatas(dataMap).addClass(oneButton));
            }
            div.add(new OneButtonForm("/" + container.id() + "/" + LOGS, "Logs", false).setDatas(dataMap).addClass(oneButton));
            div.add(new OneButtonForm("/" + container.id() + "/" + INSPECT, "Inspect", false).setDatas(dataMap).addClass(oneButton));
            div.add(new OneButtonForm("/" + container.id() + "/" + DELETE, "Delete", false).setDatas(dataMap).addClass(oneButton));

            page.addComponent(div);
        }

        page.addComponent(new FixedButton("/new/edit", "Add new docker", new ButtonPosition("10%", "10%"), false).setDatas(dataMap));
        page.addComponent(new FixedButton("/" + SETTINGS, "Settings", new ButtonPosition("10%", "20%"), false).setDatas(dataMap));
        if (Data.INSTANCE.getMainPage() != null && StringUtils.isNotEmpty(Data.INSTANCE.getMainPage())) {
            page.addComponent(new FixedButton(Data.INSTANCE.getMainPage(), "Back", new ButtonPosition("10%", "30%"), false));
        }

        page.addComponent(new Redirect("/", 5000, page).setDatas(dataMap));
        page.addComponent((new Footer()).add("Restart date: " + reStartDate)
                .add(StringModifiers.BR + "Blue Queen version: " + Util.VERSION));

        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> createLoginPage() {
        Page page = new Login("/", "Log in", "user name :", "password :");
        page.setTitle("Blue Queen");
        page.addTheme(theme);
        return new Douplet<>(200, page);
    }

    private static HtmlComponent createInformationLine(String validName, String image, String state, String status, boolean isManaged, Task.ShouldRunning shouldRunning) {
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

        builder.append(" ").append(new Span("should run:").addClass(label).getSyntax());
        if (shouldRunning == Task.ShouldRunning.TRUE) {
            builder.append(" ").append(new Span("TRUE").addClass(managed).getSyntax());
        } else if (shouldRunning == Task.ShouldRunning.FALSE) {
            builder.append(" ").append(new Span("FALSE").addClass(notManaged).getSyntax());
        } else if (shouldRunning == Task.ShouldRunning.ONCE) {
            builder.append(" ").append(new Span("ONCE").getSyntax());
        }
        return new P(builder.toString());
    }

    public static Douplet<Integer, Page> createDelete(String containerId, String containerName, Map<String, String> dataMap) {
        Page page = new Page();
        page.addTheme(theme);
        page.setTitle("Blue Queen | " + containerName);
        Div div = new Div();
        div.add(new P("Delete " + containerName + " ?"));
        div.add(new OneButtonForm("/" + containerId + "/" + DELETE_OK, "Delete", false).setDatas(dataMap).addClass(oneButton).addClass(delete));
        div.add(new OneButtonForm("/", "Cancel", false).setDatas(dataMap).addClass(oneButton));
        page.addComponent(div);
        return new Douplet<>(200, page);
    }

    public static Douplet<Integer, Page> createSettings(Map<String, String> dataMap) {
        Page page = new Page();
        page.addTheme(theme);
        page.setTitle("Blue Queen | Settings");
        Div div = new Div();

        Form form = new Form("/" + SETTINGS_OK, false);
        form.setDatas(dataMap);

        form.add(new Label(NEXT_RESTART_DATE, " next restart date (date) : ").addClass(label));
        Input nextRestartDate = new Input("text");
        nextRestartDate.setName(NEXT_RESTART_DATE);
        nextRestartDate.setValue("" + Data.INSTANCE.getNextRestartDate());
        form.add(nextRestartDate);

        form.add(StringModifiers.BR.toString());

        form.add(new Label(TIME_TO_RESTART, " time to restart (integer) : ").addClass(label));
        Input timeToRestart = new Input("text");
        timeToRestart.setName(TIME_TO_RESTART);
        timeToRestart.setValue("" + Data.INSTANCE.getTimeToRestart());
        form.add(timeToRestart);

        form.add(new Label(CHECKING_DELAY, " checking delay (integer) : ").addClass(label));
        Input checkingDelay = new Input("text");
        checkingDelay.setName(CHECKING_DELAY);
        checkingDelay.setValue("" + Data.INSTANCE.getCheckingDelay());
        form.add(checkingDelay);

        form.add(StringModifiers.BR.toString());

        form.add(new Label(MAIN_PAGE, " main page: ").addClass(label));
        Input mainPage = new Input("text");
        mainPage.setName(MAIN_PAGE);
        mainPage.setValue("" + Data.INSTANCE.getMainPage());
        form.add(mainPage);

        form.add(StringModifiers.BR.toString());

        Button send = new Button("Ok");
        send.setSubmit();
        form.add(send);

        div.add(form);

        div.add(new OneButtonForm("/", "Cancel", false).setDatas(dataMap).addClass(oneButton));

        page.addComponent(div);

        return new Douplet<>(200, page);
    }
}
