package hu.xannosz.blue.queen;

import hu.xannosz.veneos.core.html.Button;
import hu.xannosz.veneos.core.html.HtmlComponent;

public class Submit extends Button {
    public Submit(HtmlComponent element) {
        super(element);
        this.meta.put("type", "submit");
    }

    public Submit(String element) {
        super(element);
        this.meta.put("type", "submit");
    }
}
