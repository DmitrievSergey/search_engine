package searchengine.services.snippet;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;
import searchengine.services.morfology.MorphologyService;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SnippetServiceImpl implements SnippetService{
    private final MorphologyService morphologyService;

    public SnippetServiceImpl(MorphologyService morphologyService) {
        this.morphologyService = morphologyService;
    }

    public String getSnippet(PageEntity page, List<LemmaEntity> lemmas) {
        Map<String, String> lemmaWordMap = new LinkedHashMap<>();
        String text = Jsoup.parse(page.getContent()).text();
        List<String> normalForms = lemmas.stream().map(LemmaEntity::getLemma).toList();


        for (String word : text.split("\\s+")) {
            String normalForm = morphologyService.getNormalForm(word);
            if (normalForms.contains(normalForm)) {
                lemmaWordMap.put(normalForm, word);
            }
        }

        return createSnippet(lemmaWordMap.values(), text);
    }

    private String createSnippet(Collection<String> words, String text) {
        StringBuilder snippets = new StringBuilder();
        int number = 0;
        int countSymbols = COUNT_TO_ADD_SYMBOLS / words.size();
        for (String w : words) {
            int startWord = text.indexOf(w, number++);
            int endWord = startWord + w.length() - 1;

            String snippet = text.substring(getStartSnippet(text, startWord, countSymbols), getEndSnippet(text, endWord, countSymbols));

            snippets.append(snippet.replace(w, "<b>".concat(w).concat("</b>")));
            snippets.append("\n");
        }

        return snippets.toString();
    }

    private int getStartSnippet(String text, int startWord, int countSymbols) {
        int startSnippet = 0;
        int start = startWord < countSymbols ? 0 : startWord - countSymbols;

        for (int i = start; i >= 0; i--) {
            String symbol = Character.toString(text.charAt(i));
            if (symbol.matches(CAPITAL_LETTERS)) {
                startSnippet = start - (start - i);
                break;
            }
        }

        return startSnippet;
    }

    private int getEndSnippet(String text, int endWord, int countSymbols) {
        int endSnippet = 0;
        int end = endWord >= text.length() - countSymbols ? endWord : endWord + countSymbols;

        if (end == endWord) {
            return endWord;
        }

        for (int i = end; i <= text.length() - 1; i++) {
            String symbol = Character.toString(text.charAt(i));
            if (symbol.matches(SYMBOLS)) {
                endSnippet = end + (i - end);
                break;
            }
        }

        return endSnippet;
    }
}
