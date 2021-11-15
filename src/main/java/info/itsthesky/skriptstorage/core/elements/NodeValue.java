package info.itsthesky.skriptstorage.core.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import de.leonhard.storage.Yaml;
import de.leonhard.storage.internal.FlatFile;
import info.itsthesky.skriptstorage.api.Utils;
import info.itsthesky.skriptstorage.api.queue.Queue;
import info.itsthesky.skriptstorage.api.queue.QueueManager;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

@Name("Node Value")
@Description({"Represent a node value.",
        "You can get the value from its key, shortcut file are allowed here.",
        "You can set, remove and remove all keys on that property.",
        "In order to keep the comments, for Yaml files, only header & first-layer nodes comments are allowed. Every other will be deleted!",
        "",
        "The default mark is only used in set changer (when you set a value).",
        "If the specified node is already set to something, then it'll not override it and do nothing.",
        "Useful when generating default tree for configuration files, and / or updating it while keeping old values."
})
@Examples({
        "send value \"Node.Path\" from \"config\" to console # Shortcut usage",
        "broadcast value \"Node.Path.1\" from \"plugins/PluginName/data.json\"",
        "set value \"Node.Path\" from \"config\" to \"Hello World!\" # Simple string",
        "",
        "set value \"Team.Red.Color\" from \"config\" to red # Custom type",
        "set {_color} to value \"Team.Red.Color\" from \"config\" parsed as color",
        "# Now you can use {_color} as you like, for sheep, banner, etc..."
})
public class NodeValue extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(
                NodeValue.class,
                Object.class,
                ExpressionType.COMBINED,
                "[storage] [the] [(1¦default)] value[s] %string% (of|from|in) [the] [(file|shortcut)] %string% [(2¦as (array|list))]",
                "[storage] [the] [(file|shortcut)] %string%'[s] [(1¦default)] value[s] %string% [(2¦as (array|list))]"
        );
    }

    private Expression<String> exprKey;
    private Expression<String> exprFile;
    private boolean changeDefault;
    private boolean forceList;
    private Node node;

    @Nullable
    @Override
    protected Object[] get(@NotNull Event e) {
        final String key = exprKey.getSingle(e);
        final String path = CreateShortcut.parse(exprFile.getSingle(e), node);
        if (path == null || key == null)
            return new Object[0];
        final FlatFile data = Utils.parse(path);
        if (data == null)
            return new Object[0];
        return new Object[] {data.get(key)};
    }

    @Override
    public Class<?>[] acceptChange(Changer.ChangeMode mode) {
        switch (mode) {
            case SET:
            case REMOVE:
            case REMOVE_ALL:
                return new Class[] {Object[].class, Object.class};
            default:
                return new Class[0];
        }
    }

    @Override
    public void change(@NotNull Event e, @Nullable Object[] values, Changer.@NotNull ChangeMode mode) {
        final String key = exprKey.getSingle(e);
        final String path = CreateShortcut.parse(exprFile.getSingle(e), node);
        if (path == null || key == null || values == null || values.length == 0)
            return;
        final FlatFile flatFile = Utils.parse(path);
        final @Nullable Queue queue = QueueManager.parse(path);
        Consumer<FlatFile> consumer = null;
        if (flatFile == null)
            return;
        final boolean single = values.length == 1;
        switch (mode) {
            case SET:
                if (single) {
                    if (changeDefault) {
                        consumer = data -> data.setDefault(key, (forceList ? Collections.singletonList(values[0]) : values[0]));
                    } else {
                        consumer = data -> data.set(key, (forceList ? Collections.singletonList(values[0]) : values[0]));
                    }
                } else {
                    if (changeDefault) {
                        consumer = data -> data.setDefault(key, Arrays.asList(values));
                    } else {
                        consumer = data -> data.set(key, Arrays.asList(values));
                    }
                }
                break;
            case REMOVE:
                consumer = data -> data.remove(key);
                break;
            case REMOVE_ALL:
                consumer = FlatFile::clear;
                break;
        }
        if (consumer == null)
            throw new UnsupportedOperationException();
        if (queue == null) {
            consumer.accept(flatFile);
        } else {
            queue.add(consumer);
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "value " + exprKey.toString(e, debug) + " from " + exprFile.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        exprKey = (Expression<String>) exprs[0];
        exprFile = (Expression<String>) exprs[1];
        changeDefault = (parseResult.mark & 1) != 0;
        forceList = (parseResult.mark & 2) != 0;
        node = SkriptLogger.getNode();
        return true;
    }

}
