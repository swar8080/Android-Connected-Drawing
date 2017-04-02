package swar8080.collaborativedrawing.util;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;

/**
 *
 * Serialize background tasks
 * https://developer.android.com/reference/java/util/concurrent/Executor.html
 */

public class SerialExecutor implements Executor {
    final Queue<Runnable> tasks = new ArrayDeque<>();
    Runnable active;


    public synchronized void execute(final Runnable r) {
        tasks.add(new Runnable() {
            public void run() {
                try {
                    r.run();
                } finally {
                    scheduleNext();
                }
            }
        });
        if (active == null) {
            scheduleNext();
        }
    }

    protected synchronized void scheduleNext() {
        if ((active = tasks.poll()) != null) {
            new Thread(active).start();
        }
    }
}