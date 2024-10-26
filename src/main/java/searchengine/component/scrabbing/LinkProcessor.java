package searchengine.component.scrabbing;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.config.SiteConfig;
import searchengine.dto.exception.CustomInfoException;
import searchengine.dto.exception.CustomInterruptException;
import searchengine.dto.exception.CustomStopIndexingException;
import searchengine.dto.statistics.PageStatistic;
import searchengine.services.indexing.IndexingService;
import searchengine.component.MonitoringSiteIndexing;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Getter
@Setter

public class LinkProcessor  extends RecursiveTask<List<PageStatistic>> implements Comparable<LinkProcessor> {
    private static Logger logger = LoggerFactory.getLogger(MonitoringSiteIndexing.class);
    ReentrantLock lock = new ReentrantLock();

    private final String address;
    private final List<String> addressList;
    private final List<PageStatistic> pageStatisticList;
    private final SiteConfig siteConfig;

    @SneakyThrows
    public LinkProcessor(String address, List<PageStatistic> pageStatisticList, List<String> addressList, SiteConfig siteConfig) throws InterruptedException{

            this.address = address;
            this.pageStatisticList = pageStatisticList;
            this.addressList = addressList;
            this.siteConfig = siteConfig;


    }



    @SneakyThrows
    @Override
    protected List<PageStatistic> compute()  {
        if(IndexingService.isIndexingStopped.get()) {
            throw new CustomStopIndexingException("Индексация остановлена пользователем.", getException());
        }
        try {
            Thread.sleep(150);
            Document document = JsoupConnection.getConnect(address);
            String html = document.outerHtml();
            Connection.Response response = document.connection().response();
            int statusCode = response.statusCode();
            PageStatistic pageStatistic = new PageStatistic(address, html, statusCode);
            pageStatisticList.add(pageStatistic);
            Elements elements = document.select("body").select("a");
            List<LinkProcessor> taskList = new ArrayList<>();
            for (Element el : elements) {
                String link = el.attr("abs:href");

                if (link.startsWith(el.baseUri()) && !link.equals(el.baseUri()) && !link.contains("#") &&
                        !link.contains("?") && !link.contains("img") &&
                        !link.contains(".pdf") && !link.contains(".jpg") && !link.contains(".JPG") &&
                        !link.contains(".png") && !addressList.contains(link)) {
                    addressList.add(link);

                    LinkProcessor task = new LinkProcessor(link, pageStatisticList, addressList, siteConfig);
                    logger.debug("Начали обрабатывать страницу {}, сайта {}", link, siteConfig.getName());
                    task.fork();
                    taskList.add(task);
                }
            }
            taskList.forEach(ForkJoinTask::join);
        } catch (CustomInterruptException e){
            pageStatisticList.clear();
            throw new CustomInterruptException(e.getMessage(), 500, e.getUrl());
        }
        catch (CustomInfoException e) {
            log.error("Parsing error - InfoException по урлу {}" + "\n" + " детали : {} ", e.getUrl(), e.getMessage());
            PageStatistic pageStatistic = new PageStatistic(address,e.getMessage(), e.getStatusCode());
            pageStatisticList.add(pageStatistic);
        }
        catch (CustomStopIndexingException e) {
            throw new CustomStopIndexingException(e.getMessage(), e);
        }
        catch (NullPointerException e) {
            logger.info("On url {}, error {}", address, e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("Size pageStatisticList = {}", pageStatisticList.size());
        return pageStatisticList;

    }


    @Override
    public int compareTo(LinkProcessor o) {
        return this.address.compareTo(o.getAddress());
    }
}
