package info.itsthesky.skriptstorage.core.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import de.leonhard.storage.util.FileUtils;
import info.itsthesky.skriptstorage.api.Utils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;

@Name("Create Data from JSON")
@Description({
        "Convert (and check) JSON string to a flat file data.",
        "This will create the desired path, and if exist, will override it.",
        "If the inputted JSON is not valid, no exception will be thrown instead of a whole stack trace.",
        "The specified variable will be clear if no error was found, else it will contains the error message."
})
@Examples({
        "create \"config\" from json \"{\"\"Example\"\": 30}\" and store the error in {_error} # Don't forget to double the \" !",
        "if {_err} is set:",
        "\tsend \"An error occured while parsing JSON: %{_err}%\" to console"
})
public class CreateJSON extends Effect {

    static {
        Skript.registerEffect(
                CreateJSON.class,
                "(create|init|make) [the] [file] %string% (with|from) [the] [json] %string% [and store (it|the (error|exception)) in %-object%]"
        );
    }

    private Expression<String> exprPath;
    private Expression<String> exprJson;
    private Variable<?> var;
    private Node node;

    @Override
    protected void execute(@NotNull Event e) {
        final String path = CreateShortcut.parse(exprPath.getSingle(e), node);
        final String json = exprJson.getSingle(e);
        if (path == null || json == null)
            return;
        final String error = Utils.checkJSON(json);
        if (error != null) {
            if (var != null)
                var.change(e, new String[] {error}, Changer.ChangeMode.SET);
            return;
        }
        var.change(e, null, Changer.ChangeMode.RESET);
        final File file = new File(path);
        Utils.checkFile(file);
        FileUtils.write(file, Collections.singletonList(json));
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "create " + exprPath.toString(e, debug) + " from json " + exprJson.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        exprPath = (Expression<String>) exprs[0];
        exprJson = (Expression<String>) exprs[1];
        final Expression<?> exprVar = exprs[2];
        if (!(exprVar instanceof Variable<?>)) {
            Skript.error("'"+exprVar.toString(null, false)+"' cannot be used as a variable.");
            return false;
        }
        var = (Variable<?>) exprVar;
        node = SkriptLogger.getNode();
        return true;
    }
}
