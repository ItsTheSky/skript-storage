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
import info.itsthesky.skriptstorage.api.MultiplyPropertyExpression;
import info.itsthesky.skriptstorage.api.Utils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Name("YAML Comments")
@Description({"Represent the comment linked to a node in an YAML file.",
        "This property can only work with YAML files, and will simply do nothing if used with JSON or TOML",
        "The comment # are already included, means you should not add them yourself!"
})
public class DataComments extends SimpleExpression<String> {

    static {
        Skript.registerExpression(
                DataComments.class,
                String.class,
                ExpressionType.COMBINED,
                "[storage] [the] comment[s] of [the] (key|node) %string% of [the] [(yaml|file)] %string%",
                "[storage] [the] [(yaml|file)] %string%'[s] comment[s] of [the] (key|node) %string%"
        );
    }

    private Expression<String> exprKey;
    private Expression<String> exprPath;
    private Node node;

    @Override
    protected String[] get(@NotNull Event e) {
        final String path = CreateShortcut.parse(exprPath.getSingle(e), node);
        final String key = exprKey.getSingle(e);
        if (path == null || key == null)
            return new String[0];
        final FlatFile data = Utils.parse(path);
        if (data == null)
            return new String[0];
        if (!(data instanceof Yaml))
            return new String[0];
        return ((Yaml) data).getParser().assignCommentsToKey().get(key).toArray(new String[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<?>[] acceptChange(Changer.ChangeMode mode) {
        if (mode == Changer.ChangeMode.SET) {
            return new Class[]{String.class, String[].class};
        }
        return new Class[0];
    }

    @Override
    public void change(@NotNull Event e, Object[] delta, Changer.@NotNull ChangeMode mode) {
        final String path = CreateShortcut.parse(exprPath.getSingle(e), node);
        final String key = exprKey.getSingle(e);
        if (delta == null || delta.length == 0 || path == null || key == null)
            return;
        final List<String> comments = Arrays.stream(delta).map(Object::toString).collect(Collectors.toList());
        final FlatFile data = Utils.parse(path);
        if (data == null)
            return;
        if (!(data instanceof Yaml))
            return;
        final Yaml yaml = ((Yaml) data);
        yaml.getParser().assignCommentsToKey().put(key, comments);
        yaml.forceReload();
    }

    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "comments of node " + exprKey.toString(e, debug) + " of " + exprPath.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        exprKey = (Expression<String>) exprs[0];
        exprPath = (Expression<String>) exprs[1];
        node = SkriptLogger.getNode();
        return true;
    }
}
