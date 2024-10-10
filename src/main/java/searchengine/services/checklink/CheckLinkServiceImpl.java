package searchengine.services.checklink;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.entity.CheckLinkEntity;
import searchengine.entity.SiteEntity;
import searchengine.repositories.CheckLinkRepository;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@AllArgsConstructor
@Service
public class CheckLinkServiceImpl implements CheckLinkService<CheckLinkEntity> {
    private CheckLinkRepository checkLinkRepository;

    @Override
    public void saveLink(CheckLinkEntity link) {
        checkLinkRepository.save(link);
    }

    @Override
    public void saveLinks(List<CheckLinkEntity> links) {
        checkLinkRepository.saveAll(links);
    }

    @Override
    public boolean isLinkExist(CheckLinkEntity link) {
        return checkLinkRepository.existsByPathEqualsAndSiteId(link.getPath(), link.getSite().getId()) != null;
    }

    @Override
    public void deleteAll() {
        checkLinkRepository.deleteAll();
    }

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
    public String getPath(String url) {
        try {
            return new URI(url).getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
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

    @Override
    public String nameFromUrl(String url) {
        URL pageUrl = getUrl(url);
        StringBuilder b = new StringBuilder();
        for(String string: pageUrl.getHost().split("\\.")) {
            b.append(string);
        }
        return b.toString();
    }

    @Override
    public String getHost(String url) {
        URL pageUrl = getUrl(url);
        return pageUrl.getHost();
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
