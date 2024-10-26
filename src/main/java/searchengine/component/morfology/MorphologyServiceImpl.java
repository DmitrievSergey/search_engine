package searchengine.component.morfology;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;

@Service
public class MorphologyServiceImpl implements MorphologyService{
    private final RussianLuceneMorphology russianLuceneMorphology;
    private final EnglishLuceneMorphology englishLuceneMorphology;

    public MorphologyServiceImpl(RussianLuceneMorphology russianLuceneMorphology, EnglishLuceneMorphology englishLuceneMorphology) {
        this.russianLuceneMorphology = russianLuceneMorphology;
        this.englishLuceneMorphology = englishLuceneMorphology;
    }




    @Override
    public String getNormalForm(String word) {
        String result = "";
        String wordLowerCase = word.toLowerCase();

        if (getLanguage(wordLowerCase).isEmpty() || word.length() == 1) {
            return result;
        }
        if (getLanguage(wordLowerCase).equals(RUS_LANG)) {
            result = checkWord(wordLowerCase, russianLuceneMorphology) ? ""
                    : russianLuceneMorphology.getNormalForms(wordLowerCase).get(0);
        }
        if (getLanguage(wordLowerCase).equals(EN_LANG)) {
            result = checkWord(wordLowerCase, englishLuceneMorphology) ? ""
                    : englishLuceneMorphology.getNormalForms(wordLowerCase).get(0);
        }

        return result;
    }

    private String getLanguage(String word) {
        if (word.matches(RUS_ALPHABET)) {
            return RUS_LANG;
        }
        if (word.matches(EN_ALPHABET)) {
            return EN_LANG;
        }
        return "";
    }


    private boolean checkWord(String word, LuceneMorphology luceneMorphology) {
        if (!luceneMorphology.checkString(word)) {
            return true;
        }
        String morphInfo = luceneMorphology.getMorphInfo(word).toString();
        for (String teg : EXTRA_WORDS) {
            if (morphInfo.contains(teg)) {
                return true;
            }
        }

        return false;
    }


}
