package searchengine.services.checklink;

import searchengine.entity.SiteEntity;

import java.net.URL;
import java.util.List;
import java.util.Set;

public interface CheckLinkService {

    URL getUrl(String url);

    boolean isValid(String url, String baseUrl, SiteEntity site);

}
