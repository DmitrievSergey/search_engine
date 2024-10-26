package searchengine.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.config.SitesListConfig;
import searchengine.services.indexing.IndexingService;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

public class MonitoringIndexingStatus implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(MonitoringIndexingStatus.class);
    private final List<Future<?>> siteRunnableList;
    private int completeTasks = 0;
    private int tasksCount;

    public MonitoringIndexingStatus(List<Future<?>> siteRunnableList, int tasksCount) {
        this.tasksCount = tasksCount;
        this.siteRunnableList = siteRunnableList;
    }

    void getIndexingStatus(List<Future<?>> siteRunnableList) {
        while (completeTasks != tasksCount) {
            for (Iterator<Future<?>> iterator = siteRunnableList.iterator(); iterator.hasNext(); ) {
                Future<?> future = iterator.next();
                if (future.isDone()) {
                    completeTasks++;
                    iterator.remove();
                    logger.info("Size of siteRunnableList {} ", siteRunnableList.size());

                }
            }
        }
        IndexingService.isIndexingRunning.set(false);
    }

    @Override
    public void run() {
        getIndexingStatus(siteRunnableList);
    }
}
