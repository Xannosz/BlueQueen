package hu.xannosz.blue.queen;

import com.amihaiemil.docker.*;

import java.io.IOException;
import java.net.URI;

public class DockerHolder {
    public static void main(String[] args) {
        final Docker docker = new TcpDocker(URI.create("http://localhost:2375"));
        final Images images = docker.images();
        for (final Image image : images) {
            // System.out.println("" + image);
        }
        try {
            final Image helloWorld = images.pull("hello-world", "latest");
            final Container running = helloWorld.run();
            System.out.println("" + running);
            running.logs();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
