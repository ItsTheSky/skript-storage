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
import de.leonhard.storage.internal.settings.ReloadSettings;
import info.itsthesky.skriptstorage.api.Utils;
import info.itsthesky.skriptstorage.api.queue.Queue;
import info.itsthesky.skriptstorage.api.queue.QueueManager;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Name("Stop Queue")
@Description({"Stop or clear the queue for a specific path.",
        "Of course a queue should be started before this effect in order to work.",
        "Restarting-it (or stopping it) will make every changes happen in one time only (one time file save)",
        "You can also clear the queue, and therefore every changes will be lost."})
@Examples({"stop the queue for \"config\" # Save the changes",
        "clear queue for \"plugins/test.json\" # Cancel the changes"})
public class StopQueue extends Effect {

    static {
        Skript.registerEffect(
                StopQueue.class,
                "(restart|stop|save) [the] queue for [the] [path] %string%",
                "clear [the] queue for [the] [path] %string%"
        );
    }

    private Expression<String> exprPath;
    private boolean clear;
    private Node node;

    @Override
    protected void execute(@NotNull Event e) {
        final String path = CreateShortcut.parse(exprPath.getSingle(e), node);
        if (path == null)
            return;
        final Queue queue = QueueManager.parse(path);
        if (queue == null) {
            Skript.error("The queue was not started for path '"+path+"'!");
            return;
        }
        final FlatFile file = Utils.parse(path);
        if (file == null)
            return;
        file.setReloadSettings(ReloadSettings.MANUALLY);
        for (Consumer<FlatFile> consumer : queue.getConsumers())
            consumer.accept(file);
        file.forceReload();
        QueueManager.remove(path);
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return (clear ? "clear" : "restart") + " the queue for " + exprPath.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        exprPath = (Expression<String>) exprs[0];
        node = SkriptLogger.getNode();
        clear = matchedPattern == 1;
        return true;
    }
}
