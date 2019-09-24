package org.linguisto.learn;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Penn Treebank Tagset.
 * see https://gist.github.com/nlothian/9240750
 */
public class EnPOSTagSet {
    private static Map<String, String> map;

    static {
        map = new HashMap<String, String>();
        map.put("CC", "7");  //Coordinating conjunction
        map.put("CD", "9");  //Cardinal number
        map.put("DT", "");  //Determiner
        map.put("EX", "");  //Existential there
        map.put("FW", "");  //Foreign word
        map.put("IN", "7,12");  //Preposition or subordinating conjunction
        map.put("JJ", "5");  //Adjective
        map.put("JJR", "5");  //Adjective, comparative
        map.put("JJS", "5");  //Adjective, superlative
        map.put("LS", "");  //List item marker
        map.put("MD", "");  //Modal
        map.put("NN", "1");  //Noun, singular or mass
        map.put("NNS", "1");  //Noun, plural
        map.put("NNP", "1");  //Proper noun, singular
        map.put("NNPS", "1");  //Proper noun, plural
        map.put("PDT", "");  //Predeterminer
        map.put("POS", "");  //Possessive ending
        map.put("PRP", "10");  //Personal pronoun
        map.put("PRP$", "10");  //Possessive pronoun (prolog version PRP-S)
        map.put("RB", "6");  //Adverb
        map.put("RBR", "6");  //Adverb, comparative
        map.put("RBS", "6");  //Adverb, superlative
        map.put("RP", "13");  //Particle
        map.put("SYM", "");  //Symbol
        map.put("TO", "");  //to
        map.put("UH", "8");  //Interjection
        map.put("VB", "16");  //Verb, base form
        map.put("VBD", "16");  //Verb, past tense
        map.put("VBG", "16");  //Verb, gerund or present participle
        map.put("VBN", "16");  //Verb, past participle
        map.put("VBP", "16");  //Verb, non-3rd person singular present
        map.put("VBZ", "16");  //Verb, 3rd person singular present
        map.put("WDT", "");  //Wh-determiner
        map.put("WP", "10");  //Wh-pronoun
        map.put("WP$", "10");  //Possessive wh-pronoun (prolog version WP-S)
        map.put("WRB", "6");  //Wh-adverb
    }

    public EnPOSTagSet() {
    }

    public static String getWordType(String posTag) {
        return map.getOrDefault(posTag, "");
    }

    public static Set<String> getAllTags() {
        return map.keySet();
    }

}
