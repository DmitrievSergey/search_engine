package searchengine.services.indexing;

import lombok.AllArgsConstructor;
import searchengine.services.scrabbing.PageProcessor;


import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RejectedExecutionException;


@AllArgsConstructor
public class SubmitPool implements Runnable {
    private ForkJoinPool pool = new ForkJoinPool();
    private ForkJoinTask<?> task;

    @Override
    public void run() {
        runPool(pool, task);
    }

    private void runPool(ForkJoinPool pool, ForkJoinTask<?> task) throws RejectedExecutionException {
        pool.invoke(task);
        pool.shutdown();

    }
}
