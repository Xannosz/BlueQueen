package hu.xannosz.blue.queen;

import hu.xannosz.microtools.pack.Douplet;
import hu.xannosz.veneos.core.HttpHandler;
import hu.xannosz.veneos.core.Page;
import hu.xannosz.veneos.core.VeneosServer;
import hu.xannosz.veneos.core.html.Button;
import hu.xannosz.veneos.core.html.Form;
import hu.xannosz.veneos.core.html.Table;

import java.util.Map;

public class GuiHolder implements HttpHandler {
    public static void main(String[] args) {
        VeneosServer server = new VeneosServer();
        server.createServer(8888);
        server.setHandler(new GuiHolder());
    }

    @Override
    public Douplet<Integer, Page> getResponse(RequestMethod requestMethod, String s, Map<String, String> map) {
        System.out.println("" + map);
        Page page = new Page();
        page.setTitle("Blue Queen");
        Form form = new Form("form");

        Button ok = new Submit("okButton");
        form.add(ok);

        Table table = new Table();

        page.addComponent(form);
        return new Douplet<>(200, page);
    }
}
