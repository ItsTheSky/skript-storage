package info.itsthesky.skriptstorage.core.elements;

import ch.njol.skript.Skript;
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
import de.leonhard.storage.internal.FlatFile;
import info.itsthesky.skriptstorage.api.Utils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Name("Default Node")
@Description({"It only works with getter, so you can't set that to anything. Use the 'value XX of XX' property to change them.",
        "This expression will try to get the desired node, and will if the value is not set:",
        "- If the 'set' mark is specified, set the default value provided and return it",
        "- Else, only return the default value provided without changing the file"})
@Examples({"set {_value} to get value \"Node.Path\" of \"config\" or default \"50\" # Will return '50' if the node is not set",
        "set {_value} to get value \"Node.Path\" of \"config\" or set default \"50\" # Will return AND set the node to '50' if the node is not set"})
public class NodeDefault extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(
                NodeDefault.class,
                Object.class,
                ExpressionType.COMBINED,
                "get [the] [value] %string% (of|from|in) [the] [file] %string% or [(1¦set)] [default] [to] %objects%"
        );
    }

    private Expression<String> exprKey;
    private Expression<String> exprPath;
    private Expression<Object> exprDefault;
    private boolean isSet;
    private Node node;

    @Override
    protected Object[] get(@NotNull Event e) {
        final String key = exprKey.getSingle(e);
        final String path = CreateShortcut.parse(exprPath.getSingle(e), node);
        final Object singleValue = exprDefault.isSingle() ? exprDefault.getSingle(e) : null;
        final Object[] arrayValue = exprDefault.isSingle() ? null : exprDefault.getArray(e);
        final boolean hasMultipleValue = !exprDefault.isSingle();

        if (path == null || key == null)
            return new Object[0];
        final FlatFile data = Utils.parse(path);
        if (data == null)
            return new Object[0];

        if (hasMultipleValue) {
            if (isSet) {
                return data.getOrSetDefault(key, Arrays.asList(arrayValue)).toArray(new Object[0]);
            } else {
                return data.getOrDefault(key, Arrays.asList(arrayValue)).toArray(new Object[0]);
            }
        } else {
            if (isSet) {
                return new Object[] { data.getOrSetDefault(key, singleValue) };
            } else {
                return new Object[] { data.getOrDefault(key, singleValue) };
            }
        }
    }

    @Override
    public boolean isSingle() {
        return exprDefault.isSingle();
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return exprDefault.getClass();
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "get value " + exprKey.toString(e, debug) + " of " + exprPath.toString(e, debug) + " or "+ (isSet ? "set " : "") +"default to " + exprDefault.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        exprKey = (Expression<String>) exprs[0];
        exprPath = (Expression<String>) exprs[1];
        exprDefault = (Expression<Object>) exprs[2];
        node = SkriptLogger.getNode();
        isSet = parseResult.mark == 1;
        return true;
    }
}
