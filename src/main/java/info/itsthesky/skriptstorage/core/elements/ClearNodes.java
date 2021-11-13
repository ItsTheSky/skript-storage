package info.itsthesky.skriptstorage.core.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import de.leonhard.storage.internal.FlatFile;
import info.itsthesky.skriptstorage.api.Utils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@Name("Clear Nodes")
@Description({"Reset the specified file by deleting every comments (if YAML case) and possible nodes values.",
"It will basically reset the file to an emtpy one."})
@Examples({"clear nodes of \"config\"",
        "remove all nodes of \"plugins/file.json\""})
public class ClearNodes extends Effect {

    static {
        Skript.registerEffect(
                ClearNodes.class,
                "(remove|delete|reset|clear) [all] node[s] (of|in|from) [the] [file] %string%"
        );
    }

    private Expression<String> exprPath;
    private Node node;

    @Override
    protected void execute(@NotNull Event e) {
        final String path = CreateShortcut.parse(exprPath.getSingle(e), node);
        if (path == null)
            return;
        final FlatFile data = Utils.parse(path);
        if (data == null)
            return;
        data.clear();
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "clear all nodes from " + exprPath.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        exprPath = (Expression<String>) exprs[0];
        node = SkriptLogger.getNode();
        return true;
    }
}
