package info.itsthesky.skriptstorage.api;

import org.jetbrains.annotations.Nullable;

public class Shortcut {

    private final String name;
    private final String originalPath;
    private final @Nullable String scriptName;

    public Shortcut(String name, String originalPath, @Nullable String scriptName) {
        this.name = name;
        this.originalPath = originalPath;
        this.scriptName = scriptName;
    }

    public Shortcut(String name, String originalPath) {
        this(name, originalPath, null);
    }

    public String getName() {
        return name;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public boolean isPrivate() {
        return getScriptName() != null;
    }

    public @Nullable String getScriptName() {
        return scriptName;
    }
}
