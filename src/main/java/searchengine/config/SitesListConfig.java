package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class SitesListConfig {
    private List<SiteConfig> sites;

    @Cacheable(value = "sites")
    public List<SiteConfig> getSites() {
        return sites;
    }

    public void setSites(List<SiteConfig> sites) {
        this.sites = sites;
    }

    public SiteConfig existInConfig(String url) {
        for (SiteConfig s : sites) {
            if (url.contains(s.getUrl())) return s;
        }
        return null;
    }
}
