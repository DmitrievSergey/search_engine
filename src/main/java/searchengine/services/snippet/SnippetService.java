package searchengine.services.snippet;

import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;

import java.util.List;

public interface SnippetService {
    static final int COUNT_TO_ADD_SYMBOLS = 100;
    static final String CAPITAL_LETTERS = "[АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯABCDEFGHIJKLMNOPQRSTUVWXYZ]";
    static final String SYMBOLS = "[;:!. _,'@?/\\s+]";
    String getSnippet(PageEntity page, List<LemmaEntity> lemmas);
}
