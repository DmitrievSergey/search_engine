package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "jsoup-settings")
public class JSOUPSettings {
    private int timeout;
    private List<Integer> intervals;
    private boolean followRedirect;
    private boolean ignoreHTTPErrors;
    private List<String> userAgents;
    private String referrer;

    @Cacheable(value = "timeout")
    public int getTimeout() {
        return timeout;
    }

    @Cacheable(value = "intervals")
    public List<Integer> getIntervals() {
        return intervals;
    }

    @Cacheable(value = "followRedirect")
    public boolean isFollowRedirect() {
        return followRedirect;
    }

    @Cacheable(value = "ignoreHTTPErrors")
    public boolean isIgnoreHTTPErrors() {
        return ignoreHTTPErrors;
    }
    @Cacheable(value = "userAgents")
    public List<String> getUserAgents() {
        return userAgents;
    }
    @Cacheable(value = "referrer")
    public String getReferrer() {
        return referrer;
    }
}
