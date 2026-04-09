package helper;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Utility class for determining operating system properties and getting configuration directories.
 */
public class OsUtil {

    /**
     * System property for the operating system name, converted to lower case.
     */
    private static final String OS = System.getProperty("os.name").toLowerCase();

    /**
     * Boolean flag for the operating system type.
     */
    static public boolean isAndroid = System.getProperty("java.runtime.name").contains("Android");
    /**
     * Boolean flag for the operating system type.
     */
    static public boolean isMac = !isAndroid && OS.contains("mac");
    /**
     * Boolean flag for the operating system type.
     */
    static public boolean isWindows = !isAndroid && OS.contains("windows");
    /**
     * Boolean flag for the operating system type.
     */
    static public boolean isLinux = !isAndroid && OS.contains("linux");
    /**
     * Boolean flag for the operating system type.
     */
    static public boolean isIos = !isAndroid && (!(isWindows || isLinux || isMac)) || OS.startsWith("ios");

    /**
     * Boolean flag for the operating system type.
     */
    static public boolean isARM = System.getProperty("os.arch").startsWith("arm") || System.getProperty("os.arch").startsWith("aarch64");
    /**
     * Boolean flag for the operating system type.
     */
    static public boolean is64Bit = System.getProperty("os.arch").contains("64") || System.getProperty("os.arch").startsWith("armv8");

    public static boolean isGwt = false;

    static {
        try {
            Class.forName("com.google.gwt.core.client.GWT");
            isGwt = true;
        }
        catch(Exception ignored) { /* IGNORED */ }

        boolean isMOEiOS = "iOS".equals(System.getProperty("moe.platform.name"));
        if (isMOEiOS || (!isAndroid && !isWindows && !isLinux && !isMac)) {
            isIos = true;
            isAndroid = false;
            isWindows = false;
            isLinux = false;
            isMac = false;
            is64Bit = false;
        }
    }

    /**
     * Returns the configuration directory for the current user.
     *
     * @param applicationName The name of the application
     * @return The configuration directory as a String
     */
    public static String getUserConfigDirectory(String applicationName)
    {
        String CONFIG_HOME = null;

        if((CONFIG_HOME = System.getenv("XDG_CONFIG_HOME"))==null)
        {
            if(isLinux || isAndroid)
            {
                CONFIG_HOME = System.getProperty("user.home")+"/.config";
            }
            else if(isMac)
            {
                CONFIG_HOME = System.getProperty("user.home")+"/Library/Preferences";
            }
            else if(isIos)
            {
                CONFIG_HOME = System.getProperty("user.home")+"/Documents";
            }
            else if(isWindows)
            {
                if((CONFIG_HOME = System.getenv("APPDATA"))==null)
                {
                    CONFIG_HOME = System.getProperty("user.home")+"/Local Settings";
                }
            }
        }

        if(applicationName==null || CONFIG_HOME==null) return CONFIG_HOME;
        return CONFIG_HOME+"/"+applicationName;
    }


    /**
     * Returns the user data directory for the given application name.
     *
     * @param applicationName The name of the application
     * @return The user data directory as a String
     */
    public static String getUserDataDirectory(String applicationName)
    {
        String DATA_HOME = null;

        if((DATA_HOME = System.getenv("XDG_DATA_HOME"))==null)
        {
            if(isLinux || isAndroid)
            {
                DATA_HOME = System.getProperty("user.home")+"/.local/share";
            }
            else if(isMac)
            {
                DATA_HOME = System.getProperty("user.home")+"/Library/Application Support";
            }
            else if(isIos)
            {
                DATA_HOME = System.getProperty("user.home")+"/Documents";
            }
            else if(isWindows)
            {
                if((DATA_HOME = System.getenv("APPDATA"))==null)
                {
                    DATA_HOME = System.getProperty("user.home")+"/Local Settings/Application Data";
                }
            }
        }

        if(applicationName==null || DATA_HOME==null) return DATA_HOME;

        return DATA_HOME+"/"+applicationName;
    }

    /**
     * Returns the configuration directory for the application. If the directory doesn't exist, it will be created.
     *
     * @return The configuration directory as a File object
     */
    public static File getConfigDir()
    {
        File _f = new File(getUserConfigDirectory("tty-tetris"));
        _f.mkdirs();
        return _f;
    }

    /**
     * Returns the configuration file based on the given file name.
     *
     * @param _file The name of the configuration file
     * @return The File object representing the configuration file
     */
    public static File getConfigFile(String _file)
    {
        return new File(getConfigDir(), _file);
    }

}
