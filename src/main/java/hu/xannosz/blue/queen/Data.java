package hu.xannosz.blue.queen;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class Data {

    private final static Path DATA_PATH = Paths.get("data/data.json");

    @Getter
    private final Set<Task> tasks = new HashSet<>();

    public void addTask(Task task) {
        tasks.add(task);
    }

    public static Data readData() {
        try {
            JsonElement dataObject = JsonParser.parseString(FileUtils.readFileToString(DATA_PATH.toFile()));
            return new Gson().fromJson(dataObject, Data.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new Data();
        }
    }

    public void writeData() {
        try {
            DATA_PATH.toFile().getParentFile().mkdirs();
            DATA_PATH.toFile().createNewFile();
            FileUtils.writeStringToFile(DATA_PATH.toFile(), new Gson().toJson(this));
        } catch (Exception e) {
            LogHandlerImpl.INSTANCE.error(String.format("Write data to %s failed.", DATA_PATH), e);
        }
    }
}
