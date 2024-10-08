package searchengine.services.indexing;

import lombok.AllArgsConstructor;
import searchengine.services.scrabbing.PageProcessor;


import java.util.concurrent.*;


@AllArgsConstructor
public class SubmitPool implements Callable<String> {
    private ForkJoinPool pool;
    private PageProcessor task;

//    @Override
//    public void run() {
//        runPool(pool, task);
//    }

    private String runPool(ForkJoinPool pool, PageProcessor task) throws RejectedExecutionException, ExecutionException, InterruptedException {
        pool.invoke(task);
        pool.shutdown();

        return task.getUrl();
    }

    @Override
    public String call() throws Exception {
        return runPool(pool, task);
    }
}
