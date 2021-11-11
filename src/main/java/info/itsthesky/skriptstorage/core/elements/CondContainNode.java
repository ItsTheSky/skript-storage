package info.itsthesky.skriptstorage.core.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import de.leonhard.storage.internal.FlatFile;
import info.itsthesky.skriptstorage.api.Utils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Name("Contains Node")
@Description({"Check if the specific data file contains a node or not.",
"This special condition will also convert the input string as possible shortcut."})
public class CondContainNode extends Condition {

    static {
        Skript.registerCondition(
                CondContainNode.class,
                "[the] (y[a]ml|t[o]ml|json|file) %string% contain[s] [the] node %string%",
                "[the] (y[a]ml|t[o]ml|json|file) %string% (don't|doesn't) contain[s] [the] node %string%"
        );
    }

    private Expression<String> exprPath;
    private Expression<String> exprKey;
    private Node node;

    @Override
    public boolean check(@NotNull Event e) {
        final String path = CreateShortcut.parse(exprPath.getSingle(e), node);
        final String key = exprKey.getSingle(e);
        if (path == null || key == null)
            return false;
        final FlatFile data = Utils.parse(path);
        if (data == null)
            return false;
        return data.contains(key);
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "the data file " + exprPath.toString(e, debug) + " contains node " + exprKey.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        exprPath = (Expression<String>) exprs[0];
        exprKey = (Expression<String>) exprs[1];
        node = SkriptLogger.getNode();
        return true;
    }
}
