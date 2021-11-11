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

@Name("File Exist")
@Description({"Check if the specific file exist or not.",
"This special condition will also convert the input string as possible shortcut."})
public class CondFileExist extends Condition {

    static {
        Skript.registerCondition(
                CondFileExist.class,
                "[the] (y[a]ml|t[o]ml|json|file) %string% exist[s]",
                "[the] (y[a]ml|t[o]ml|json|file) %string% (don't|doesn't) exist[s]"
        );
    }

    private Expression<String> exprPath;
    private Node node;

    @Override
    public boolean check(@NotNull Event e) {
        final String path = CreateShortcut.parse(exprPath.getSingle(e), node);
        return isNegated() != (path != null && new File(path).exists());
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "the data file " + exprPath.toString(e, debug) + " exist";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        exprPath = (Expression<String>) exprs[0];
        node = SkriptLogger.getNode();
        return true;
    }
}
