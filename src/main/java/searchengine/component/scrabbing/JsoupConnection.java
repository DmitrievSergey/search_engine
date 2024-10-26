package searchengine.component.scrabbing;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.dto.exception.CustomInfoException;
import searchengine.dto.exception.CustomInterruptException;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.SocketTimeoutException;

public interface JsoupConnection {
    Logger logger = LoggerFactory.getLogger(JsoupConnection.class);
    static Document getConnect(String url) throws CustomInfoException, CustomInterruptException {
        Document document = null;

        try {
            Thread.sleep(1500);
            document = Jsoup.connect(url).userAgent(UserAgent.getUserAgent()).referrer("http://www.google.com").get();
        } catch (SocketTimeoutException | SSLHandshakeException e) {
            logger.debug("Can't get connected to the site" + url);
            throw new CustomInterruptException(CustomInterruptException.PAGE_UNREACHABLE, 500, url);
        } catch (HttpStatusException e) {
            if (e.getStatusCode() >= 400 && e.getStatusCode() < 500) {
                logger.debug("Can't get connected to the site" + url);
                throw new CustomInfoException(CustomInterruptException.PAGE_UNREACHABLE, e.getStatusCode(), url);
            } else if (e.getStatusCode() >= 500 )throw new CustomInterruptException(
                    CustomInterruptException.PAGE_UNREACHABLE, e.getStatusCode(), e.getUrl());
        }
        catch (UnsupportedMimeTypeException e) {
            logger.info(e.getUrl() + "Некорректный урл");
        } catch (IOException exception) {
            exception.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }
}
