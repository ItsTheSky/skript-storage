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
import de.leonhard.storage.util.FileUtils;
import info.itsthesky.skriptstorage.api.MultiplyPropertyExpression;
import info.itsthesky.skriptstorage.api.Utils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        return new Class[]{String.class, String[].class};
    }

    @Override
    public void change(@NotNull Event e, Object[] delta, Changer.@NotNull ChangeMode mode) {
        final String path = CreateShortcut.parse(exprPath.getSingle(e), node);
        final String key = exprKey.getSingle(e);
        if (delta == null || delta.length == 0 || path == null || key == null)
            return;
        final List<String> comments = Arrays.stream(delta).map(obj -> "#" + obj.toString()).collect(Collectors.toList());
        final FlatFile data = Utils.parse(path);
        if (data == null)
            return;
        if (!(data instanceof Yaml))
            return;

        /* Constants */

        final Yaml yaml = ((Yaml) data);

        final List<String> current = getComments(key, yaml);
        switch (mode) {
            case SET:
                setComment(key, yaml, comments);
                break;
            case ADD:
                current.addAll(comments);
                setComment(key, yaml, current);
                break;
            case REMOVE:
                current.removeAll(comments);
                setComment(key, yaml, current);
                break;
            case DELETE:
            case REMOVE_ALL:
            case RESET:
                setComment(key, yaml, null);
                break;
        }
    }

    public static List<String> getComments(String key, Yaml yaml) {
        return yaml.getParser().assignCommentsToKey().get(key);
    }

    public static void setComment(String key, Yaml yaml, @Nullable List<String> comments) {
        final boolean clearComments = comments == null;

        final List<String> currentLines = new ArrayList<>();
        final List<String> invertedCurrentLines = FileUtils.readAllLines(yaml.getFile());
        final String keyToSearch = key.contains(".") ? key.split("\\.")[0] : key;
        final Pattern pattern = Pattern.compile(keyToSearch + ":");
        final List<String> content = new ArrayList<>();

        Collections.reverse(invertedCurrentLines);

        boolean inTargetNode = false;
        for (String line : invertedCurrentLines) {
            if (inTargetNode && line.startsWith("#"))
                continue;
            final Matcher matcher = pattern.matcher(line);
            inTargetNode = matcher.find();
            currentLines.add(line);
        }

        Collections.reverse(currentLines);

        for (String line : currentLines) {
            final Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                if (!clearComments)
                    content.addAll(comments);
            }
            content.add(line);
        }

        FileUtils.write(yaml.getFile(), content);
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
