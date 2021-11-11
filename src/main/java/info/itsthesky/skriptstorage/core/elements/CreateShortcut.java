package info.itsthesky.skriptstorage.core.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import info.itsthesky.skriptstorage.api.Shortcut;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

@Name("Create File Shortcut")
@Description({"Shortcut are completely optional and here to help you gain time while coding.",
        "Instead of specifying the whole file path every time, simple load a shortcut key, which will be replaced by the desired file.",
        "If you create a shortcut for an already existing one, it will be overed.",
        "The private mark will make the shortcut only available in the current script only, to avoid conflict with other potential scripts."
})
@Examples({"create shortcut for path \"plugins/PluginName/config.yml\" as \"config\"",
        "# Therefore, you can only do 'value \"test.hello\" from \"config\"' now",
        "remove shortcut \"config\""
})
public class CreateShortcut extends Effect {

    /**
     * Database for every shortcuts registered, clear when the server stop.
     */
    public static final HashMap<String, Shortcut> shortcuts = new HashMap<>();

    /**
     * Convert, if present, the input into the path registered.
     * <br> Could return the same as before, but can return null if the input is null.
     * @param input The original text
     */
    public static @Nullable String parse(String input, Node item) {
        final String scriptName = item.getConfig().getFileName();
        final Shortcut shortcut = shortcuts.getOrDefault(input, null);
        if (input == null)
            return null;
        if (shortcut == null)
            return input;
        if (shortcut.isPrivate() && !shortcut.getScriptName().equalsIgnoreCase(scriptName))
            return input;
        return shortcut.getOriginalPath();
    }

    static {
        Skript.registerEffect(
                CreateShortcut.class,
                "(make|create|register) [a] [new] [(1¦private)] shortcut for [the] [path] %string% as [the] [name] %string%",
                "(delete|remove) [the] shortcut [(with name|named)] %string%"
        );
    }

    private Expression<String> exprPath;
    private Expression<String> exprKey;

    private boolean remove;
    private boolean isPrivate;
    private Config script;

    @Override
    protected void execute(@NotNull Event e) {
        final String path = remove ? null : exprPath.getSingle(e);
        final String key = exprKey.getSingle(e);
        if (key == null || (!remove && path == null))
            return;
        if (remove) {
            shortcuts.remove(key);
        } else {
            final Shortcut shortcut = new Shortcut(key, path, (isPrivate ? script.getFileName() : null));
            shortcuts.put(key, shortcut);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "create shortcut for path " + exprPath.toString(e, debug) + " as " + exprKey.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        remove = matchedPattern == 1;
        if (remove) {
            exprKey = (Expression<String>) exprs[0];
        } else {
            exprPath = (Expression<String>) exprs[0];
            exprKey = (Expression<String>) exprs[1];
            isPrivate = parseResult.mark == 1;
        }
        script = SkriptLogger.getNode().getConfig(); // Should never throw an exception
        return true;
    }
}
