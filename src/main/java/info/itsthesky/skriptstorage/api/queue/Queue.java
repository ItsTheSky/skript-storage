package info.itsthesky.skriptstorage.api.queue;

import de.leonhard.storage.internal.FlatFile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Queue {

    private final String path;
    private final List<Consumer<FlatFile>> consumers;

    public String getPath() {
        return path;
    }

    public List<Consumer<FlatFile>> getConsumers() {
        return consumers;
    }

    public Queue(String path) {
        this.path = path;
        this.consumers = new ArrayList<>();
    }

    public void add(Consumer<FlatFile> queue) {
        consumers.add(queue);
    }
}
