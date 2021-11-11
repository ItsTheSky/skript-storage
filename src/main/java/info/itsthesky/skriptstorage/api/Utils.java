package info.itsthesky.skriptstorage.api;

import de.leonhard.storage.Json;
import de.leonhard.storage.LightningBuilder;
import de.leonhard.storage.Toml;
import de.leonhard.storage.Yaml;
import de.leonhard.storage.internal.FlatFile;
import de.leonhard.storage.internal.settings.ConfigSettings;
import info.itsthesky.skriptstorage.SkriptStorage;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public final class Utils {

    public static FlatFile parse(String path) {
        final File file = new File(path);
        checkFile(file);
        final String extension = getExt(path).toLowerCase(Locale.ROOT);
        FlatFile data;
        switch (extension) {
            case ".json":
                data = new Json(new File(path));
                break;
            case ".yml":
                data = LightningBuilder
                        .fromFile(new File(path))
                        .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                        .createYaml();
                break;
            case ".toml":
                data = new Toml(new File(path));
                break;
            default:
                data = null;
        }
        if (data == null) {
            SkriptStorage.getServerLogger().severe("Unable to create a data file as " + path + "!");
            SkriptStorage.getServerLogger().severe("The file should end either by .json, .yml or .toml but got: " + extension);
        }
        return data;
    }

    private static void checkFile(File file) {
        if (file.exists())
            return;
        if (file.getParentFile() != null)
            file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            SkriptStorage.getServerLogger().severe("Unable to create file: " + file.getPath());
        }
    }

    public static String getExt(String file) {
        int lastIndexOf = file.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ".yml";
        }
        return file.substring(lastIndexOf);
    }
}
