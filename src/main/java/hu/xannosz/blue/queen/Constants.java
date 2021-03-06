package hu.xannosz.blue.queen;

public class Constants {
    public static final String INSPECT = "inspect";
    public static final String STOP = "stop";
    public static final String START = "start";
    public static final String RESTART = "restart";
    public static final String RE_PULL = "rePull";
    public static final String EDIT = "edit";
    public static final String EDIT_FORM = "editForm";
    public static final String DELETE = "delete";
    public static final String DELETE_OK = "deleteOK";
    public static final String LOGS = "logs";
    public static final String SETTINGS = "settings";
    public static final String SETTINGS_OK = "settingsOK";

    public static final String PERSIST_FOLDER;

    static {
        if (OsUtils.isWindows()) {
            PERSIST_FOLDER = "C:/persist";
        } else {
            PERSIST_FOLDER = "/persist";
        }
    }

    public static final String ID = "id";
    public static final String IMAGE = "image";
    public static final String SHOULD_RUN = "shouldRun";
    public static final String PORT = "port";
    public static final String VOLUME = "volume";
    public static final String TOKEN = "token";
    public static final String USER = "userName";
    public static final String PASSWD = "password";

    public static final String NEXT_RESTART_DATE = "nextRestartDate";
    public static final String TIME_TO_RESTART = "timeToRestart";
    public static final String CHECKING_DELAY = "checkingDelay";
    public static final String MAIN_PAGE = "mainPage";
}
