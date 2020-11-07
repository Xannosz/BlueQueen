package hu.xannosz.blue.queen;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import hu.xannosz.microtools.Password;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static hu.xannosz.blue.queen.Constants.PERSIST_FOLDER;

public class Data {

    private final static Path DATA_PATH = Paths.get(PERSIST_FOLDER, "bluequeen/data/data.json");

    public static Data INSTANCE = new Data();

    @Getter
    private final Set<Task> tasks = new HashSet<>();

    @Getter
    private final Date nextReStartDate = new Date();

    @Getter
    private final long timeToRestart = 1000 * 60 * 5;

    @Getter
    private final int checkingDelay = 1000 * 10;

    @Getter
    private final String selfName = "bluequeen";

    @Getter
    private final Map<String, String> userPassword = new HashMap<>();

    private Data(){

    }

    public boolean addUserPassword(String user, String password) {
        try {
            userPassword.put(user, Password.getSaltedHash(password));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean authenticate(String user, String password) {
        try {
            return Password.check(password, userPassword.get(user));
        } catch (Exception e) {
            return false;
        }
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public static void readData() {
        try {
            JsonElement dataObject = JsonParser.parseString(FileUtils.readFileToString(DATA_PATH.toFile()));
            INSTANCE = new Gson().fromJson(dataObject, Data.class);
        } catch (Exception e) {
            e.printStackTrace();
            INSTANCE =  new Data();
        }
    }

    public static void writeData() {
        try {
            DATA_PATH.toFile().getParentFile().mkdirs();
            DATA_PATH.toFile().createNewFile();
            FileUtils.writeStringToFile(DATA_PATH.toFile(), new Gson().toJson(INSTANCE));
        } catch (Exception e) {
            LogHandlerImpl.INSTANCE.error(String.format("Write data to %s failed.", DATA_PATH), e);
        }
    }
}
