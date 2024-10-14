package lol.pyr.znpcsplus.updater;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class UpdateChecker extends BukkitRunnable {
    private final static Logger logger = Logger.getLogger("ZNPCsPlus Update Checker");
    private final static String GET_RESOURCE = "https://api.spigotmc.org/simple/0.2/index.php?action=getResource&id=109380";
    public final static String DOWNLOAD_LINK = "https://www.spigotmc.org/resources/znpcsplus.109380/";

    private final PluginDescriptionFile info;
    private Status status = Status.UNKNOWN;
    private String newestVersion = "N/A";

    public UpdateChecker(PluginDescriptionFile info) {
        this.info = info;
    }

    public void run() {
        String foundVersion = null;
        try {
            URL getResource = new URL(GET_RESOURCE);
            HttpURLConnection httpRequest = ((HttpURLConnection) getResource.openConnection());
            httpRequest.setRequestMethod("GET");
            httpRequest.setConnectTimeout(5_000);
            httpRequest.setReadTimeout(5_000);

            if (httpRequest.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (InputStreamReader reader = new InputStreamReader(httpRequest.getInputStream())) {
                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                    foundVersion = jsonObject.get("current_version").getAsString();
                }
            } else {
                logger.warning("Failed to check for updates: HTTP response code " + httpRequest.getResponseCode());
            }
        } catch (IOException e) {
            logger.warning("Failed to check for updates: " + e.getMessage());
            return;
        }

        if (foundVersion == null) return;
        newestVersion = foundVersion;

        status = compareVersions(info.getVersion(), newestVersion);
        if (status == Status.UPDATE_NEEDED) notifyConsole();
    }

    private void notifyConsole() {
        logger.warning("Version " + getLatestVersion() + " of " + info.getName() + " is available now!");
        logger.warning("Download it at " + UpdateChecker.DOWNLOAD_LINK);
    }

    private Status compareVersions(String currentVersion, String newVersion) {
        if (currentVersion.equalsIgnoreCase(newVersion)) return Status.LATEST_VERSION;
        ReleaseType currentType = parseReleaseType(currentVersion);
        ReleaseType newType = parseReleaseType(newVersion);
        if (currentType == ReleaseType.UNKNOWN || newType == ReleaseType.UNKNOWN) return Status.UNKNOWN;
        String currentVersionWithoutType = getVersionWithoutReleaseType(currentVersion);
        String newVersionWithoutType = getVersionWithoutReleaseType(newVersion);
        String[] currentParts = currentVersionWithoutType.split("\\.");
        String[] newParts = newVersionWithoutType.split("\\.");
        for (int i = 0; i < Math.min(currentParts.length, newParts.length); i++) {
            int currentPart = Integer.parseInt(currentParts[i]);
            int newPart = Integer.parseInt(newParts[i]);
            if (newPart > currentPart) return Status.UPDATE_NEEDED;
            if (newPart < currentPart) return Status.LATEST_VERSION;
        }
        if (newType.ordinal() > currentType.ordinal()) return Status.UPDATE_NEEDED;
        if (newType == currentType) {
            int currentReleaseTypeNumber = getReleaseTypeNumber(currentVersion);
            int newReleaseTypeNumber = getReleaseTypeNumber(newVersion);
            if (newReleaseTypeNumber > currentReleaseTypeNumber) return Status.UPDATE_NEEDED;
        }
        return Status.LATEST_VERSION;
    }

    private ReleaseType parseReleaseType(String version) {
        if (version.toLowerCase().contains("snapshot")) return ReleaseType.SNAPSHOT;
        if (version.toLowerCase().contains("alpha")) return ReleaseType.ALPHA;
        if (version.toLowerCase().contains("beta")) return ReleaseType.BETA;
        return version.matches("\\d+\\.\\d+\\.\\d+") ? ReleaseType.RELEASE : ReleaseType.UNKNOWN;
    }

    private String getVersionWithoutReleaseType(String version) {
        return version.contains("-") ? version.split("-")[0] : version;
    }

    private int getReleaseTypeNumber(String version) {
        if (!version.contains("-")) return 0;
        return Integer.parseInt(version.split("-")[1].split("\\.")[1]);
    }

    public Status getStatus() {
        return status;
    }

    public String getLatestVersion() {
        return newestVersion;
    }

    public enum Status {
        UNKNOWN, LATEST_VERSION, UPDATE_NEEDED
    }

    public enum ReleaseType {
        UNKNOWN, SNAPSHOT, ALPHA, BETA, RELEASE
    }
}
