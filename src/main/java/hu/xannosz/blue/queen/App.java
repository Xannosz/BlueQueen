package hu.xannosz.blue.queen;

import com.amihaiemil.docker.Docker;
import com.amihaiemil.docker.TcpDocker;

import java.net.URI;

public class App {
    public static void main(String[] args) {
        final Docker docker = new TcpDocker(URI.create("http://localhost:2375"));
        Data data = new Data();
        docker.containers().all().forEachRemaining((data::addContainer));
        GuiHolder guiHolder = new GuiHolder(data);
    }
}
