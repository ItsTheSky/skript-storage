package info.itsthesky.skriptstorage.api.queue;

import java.util.HashMap;

public class QueueManager {

    private static final HashMap<String, Queue> QUEUES = new HashMap<>();

    public static void addQueue(Queue queue) {
        QUEUES.put(queue.getPath(), queue);
    }

    public static boolean queueExist(String path) {
        return QUEUES.containsKey(path);
    }

    public static Queue parse(String path) {
        return QUEUES.getOrDefault(path, null);
    }

    public static void remove(String path) {
        QUEUES.remove(path);
    }

}
