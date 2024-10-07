package searchengine.services.scrabbing;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public interface ScrubbingService {
    Set<String> getPageLinks(String url, SiteEntity site);
}
