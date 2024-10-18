package searchengine.services.statistic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.entity.Status;
import searchengine.services.lemma.LemmaService;
import searchengine.services.page.PageService;
import searchengine.services.site.SiteService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final PageService<PageEntity> pageService;
    private final SiteService<SiteEntity> siteService;
    private final LemmaService lemmaService;

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = TotalStatistics.builder()
                .sites(siteService.getSitesCount())
                .pages(pageService.getCount())
                .lemmas(lemmaService.getCount())
                .indexing(false)
                .build();

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<SiteEntity> sitesList = siteService.getAllSites();
        for (SiteEntity site : sitesList) {

            if (site.getStatus().equals(Status.INDEXING)) {
                total.setIndexing(true);
            }

            DetailedStatisticsItem item = DetailedStatisticsItem.builder()
                    .url(site.getUrl())
                    .name(site.getName())
                    .status(site.getStatus().toString())
                    .statusTime(System.currentTimeMillis())
                    .error(site.getLastError() == null ? "" : site.getLastError())
                    .pages(pageService.getCountBySite(site))
                    .lemmas(lemmaService.getCountBySite(site))
                    .build();
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
