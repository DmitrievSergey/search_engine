package searchengine.services.checklink;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.entity.SiteEntity;
import searchengine.services.site.SiteService;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class CheckLinkServiceImpl implements CheckLinkService {

    @Override
    public boolean isValid(String page, String baseUrl, SiteEntity site) {
        URL pageUrl;
        URL siteUrl;
        try {
            pageUrl = getUrl(page);
            siteUrl = getUrl(site.getUrl());
            if (! checkProtocol(pageUrl.getProtocol())) {
                log.info("Кривой протокол у урла " + pageUrl);
                return false;
            }
            if (! pageUrl.getHost().equals(siteUrl.getHost())) return false;
            if (! pageUrl.toString().contains(baseUrl)) return false;
            if ( checkFileExtension(pageUrl.getPath())) return false;
        } catch (NullPointerException | IllegalArgumentException  e) {
            e.printStackTrace();
            log.info(" Кривой URI " + page);
            return false;
        }
        return true;

    }

    @Override
    public URL getUrl(String url) {
        try {
            return new URI(url).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            e.printStackTrace();
            log.info(" Кривой урл - " + url);
        }
        return null;
    }

    private boolean checkProtocol(String url) {
        String validProtocol = "http|https";
        return url.matches(validProtocol);
    }

    private boolean checkFileExtension(String url) {
        String fileExtensions = ".*\\.(js|css|jpg|pdf|jpeg|gif|zip|tar|jar|gz|svg|ppt|pptx|php|png|exe|docx)($|\\?.*)";
        Pattern pattern = Pattern.compile(fileExtensions);
        Matcher matcher = pattern.matcher(url);
        return matcher.find();
    }

}
