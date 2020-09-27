package hu.xannosz.blue.queen;

import com.amihaiemil.docker.Container;
import hu.xannosz.microtools.pack.Douplet;
import hu.xannosz.veneos.core.HttpHandler;
import hu.xannosz.veneos.core.Page;
import hu.xannosz.veneos.core.VeneosServer;
import hu.xannosz.veneos.core.html.Button;
import hu.xannosz.veneos.core.html.Div;
import hu.xannosz.veneos.core.html.Form;
import hu.xannosz.veneos.core.html.P;

import java.io.IOException;
import java.util.Map;

public class GuiHolder implements HttpHandler {

    private final Data data;

    public GuiHolder(Data data) {
        this.data = data;
        VeneosServer server = new VeneosServer();
        server.createServer(8888);
        server.setHandler(this);
    }

    @Override
    public Douplet<Integer, Page> getResponse(RequestMethod requestMethod, String s, Map<String, String> map) {
        System.out.println("" + map);
        Page page = new Page();
        page.setTitle("Blue Queen");
        Form form = new Form("form");

        Button ok = new Submit("okButton");
        form.add(ok);

        for (Container container : data.getContainers()) {
            Div div = new Div();
            try {
                div.add(new P("" + container.inspect()));
                div.add(new P("" + container.inspect().getString("Name")));
                div.add(new Submit("Stop"));
                div.add(new Submit("Restart"));
                div.add(new Submit("Edit"));
                div.add(new Submit("Delete"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            page.addComponent(div);
        }

        page.addComponent(form);
        return new Douplet<>(200, page);
    }
}
