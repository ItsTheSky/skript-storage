package info.itsthesky.skriptstorage.core.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import info.itsthesky.skriptstorage.api.queue.Queue;
import info.itsthesky.skriptstorage.api.queue.QueueManager;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Name("Start Queue")
@Description({"Start a new queue for a specific file path.",
        "Every change made with that path will be queued, and not saved directly.",
        "You will either need to save the queue (make every save) or clear it (cancel every changes) to return to a normal state.",
        "This system is recommended when you are managing a huge quantity of data in a single time."})
@Examples({"start queue for \"config\"",
        "enable the queue for \"plugins/test.json\""})
@Since("1.3.0")
public class StartQueue extends Effect {

    static {
        Skript.registerEffect(
                StartQueue.class,
                "(enable|start) [the] queue for [the] [path] %string%"
        );
    }

    private Expression<String> exprPath;
    private Node node;

    @Override
    protected void execute(@NotNull Event e) {
        final String path = CreateShortcut.parse(exprPath.getSingle(e), node);
        if (path == null)
            return;
        if (QueueManager.queueExist(path)) {
            Skript.error("Unable to create a queue for '"+path+"' since there's already one started.");
            return;
        }
        final Queue queue = new Queue(path);
        QueueManager.addQueue(queue);
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "start the queue for " + exprPath.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        exprPath = (Expression<String>) exprs[0];
        node = SkriptLogger.getNode();
        return true;
    }
}
