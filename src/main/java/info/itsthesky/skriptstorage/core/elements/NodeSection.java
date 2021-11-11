package info.itsthesky.skriptstorage.core.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
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

@Name("Node Section")
@Description({
        "Return the string list of every nodes the files have, with a specific layer of not.",
        "Imagine a second you have this file (YAML file for example, but works with other types too):",
        "",
        "First:",
        "    Second: Hello World!",
        "    Third:",
        "        Fourth: 50",
        "    Lastly: 30",
        "SecondMain: Welcome!",
        "",
        "Then, the following code will returns:",
        "node section \"First\" of \"config\" # [Second, Third.Fourth, Lastly]",
        "node section \"First\" of \"config\" as one layer # [Second, Third, Lastly]",
        "node section \"\" of \"config\" # [First.Lastly, First.Third.Fourth, First.Second, SecondMain]",
        "node section \"\" of \"config\" as one layer # [First, SecondMain]",
})
public class NodeSection extends SimpleExpression<String> {

    static {
        Skript.registerExpression(
                NodeSection.class,
                String.class,
                ExpressionType.COMBINED,
                "[storage] [the] [node] section [(with key|keyed)] %string% (of|in|from) %string% [(1¦as one (layer|keyset))]"
        );
    }

    private boolean oneLayer;
    private Expression<String> exprKey;
    private Expression<String> exprPath;
    private Node node;

    @Override
    protected String[] get(@NotNull Event e) {
        final String key = exprKey.getSingle(e);
        final String path = CreateShortcut.parse(exprPath.getSingle(e), node);
        if (path == null || key == null)
            return new String[0];
        final FlatFile data = Utils.parse(path);
        if (data == null)
            return new String[0];
        if (oneLayer) {
            return (key.isEmpty() ? data.singleLayerKeySet() : data.singleLayerKeySet(key)).toArray(new String[0]);
        } else {
            return (key.isEmpty() ? data.keySet() : data.keySet(key)).toArray(new String[0]);
        }
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "node section " + exprKey.toString(e, debug) + " from " + exprPath.toString(e, debug) + (oneLayer ? " as one layer" : "");
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        exprKey = (Expression<String>) exprs[0];
        exprPath = (Expression<String>) exprs[1];
        oneLayer = parseResult.mark == 1;
        node = SkriptLogger.getNode();
        return true;
    }
}
