package searchengine.services.indexing;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.services.scrabbing.LinkProcessor;


import java.util.concurrent.*;

@Slf4j
@AllArgsConstructor
public class SubmitPool implements Callable<LinkProcessor> {
    private ForkJoinPool pool;
    private LinkProcessor task;


    private LinkProcessor runPool(ForkJoinPool pool, LinkProcessor task) throws RejectedExecutionException {
        pool.execute(task);
        pool.shutdown();
        log.info("Task get url " + task.getUrl());
        return task;
    }

    @Override
    public LinkProcessor call() throws Exception {
        return runPool(pool, task);
    }
}
