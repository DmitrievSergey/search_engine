package searchengine.services.morfology;

import java.util.List;

public interface MorphologyService {
    static final List<String> EXTRA_WORDS = List.of("СОЮЗ", "МЕЖД", "МС", "ПРЕДЛ", "ВВОДН", "ЧАСТ", "CONJ", "PART");
    static final String RUS_ALPHABET = "[а-яА-Я]+";
    static final String EN_ALPHABET = "[a-zA-z]+";
    static final String RUS_LANG = "RUSSIAN";
    static final String EN_LANG = "ENGLISH";
    String getNormalForm(String word);
}
