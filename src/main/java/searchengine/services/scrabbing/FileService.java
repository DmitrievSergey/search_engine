package searchengine.services.scrabbing;

import searchengine.entity.SiteEntity;

import java.util.Set;

public interface FileService {
    Set<String> writeLinksToFile(String url, SiteEntity site);

}
