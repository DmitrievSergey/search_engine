package searchengine.services.checklink;

import searchengine.entity.SiteEntity;

import java.net.URL;
import java.util.List;

public interface CheckLinkService<CheckLinkEntity> {

    void saveLink(CheckLinkEntity link);

    void saveLinks(List<CheckLinkEntity> links);

    boolean isLinkExist(CheckLinkEntity link);

    void deleteAll();

    boolean isValid(String url, String baseUrl, SiteEntity site);

    String getPath(String url);

    URL getUrl(String url);

    String getHost(String url);

    String nameFromUrl(String url);
}
