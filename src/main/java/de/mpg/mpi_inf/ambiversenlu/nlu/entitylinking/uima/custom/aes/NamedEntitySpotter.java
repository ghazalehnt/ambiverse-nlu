package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;


import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMentionCandidate;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NamedEntityCandidate;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.StopWord;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.trie.Spot;
import de.mpg.mpi_inf.ambiversenlu.nlu.trie.TextSpotter;
import de.mpg.mpi_inf.ambiversenlu.nlu.trie.TrieBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.trie.Utils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.lucene.util.fst.FST;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * This UIMA component is intended for spotting named entity mention in an input text.
 * It is implemented via finite state transducers. The component is language dependant.
 *
 */
public class NamedEntitySpotter extends JCasAnnotator_ImplBase {

    private static FST[] trie = new FST[Language.activeLanguages().size()];
    private Logger logger = LoggerFactory.getLogger(NamedEntitySpotter.class);

//  /**
//   * The language.
//   */
//  public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
//
//  @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true) private String language;

    /**
     * The size of the cursor queue.
     */
    public static final String PARAM_QUEUE_SIZE = "queue_size";
    @ConfigurationParameter(name = PARAM_QUEUE_SIZE, mandatory = false)
    private int QUEUE_SIZE = 1000;

    /**
     * Minimun matching ration
     */
    public static final String PARAM_MATCHING_RATIO = "matching_ratio";
    @ConfigurationParameter(name = PARAM_MATCHING_RATIO, mandatory = false)
    private double MATCHING_RATIO = 0.9;



    @Override public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        Set<String> orderedMentions = null;
        try {
            logger.info("Initializing NamedEntitySpotter.");

            for(Language language : Language.activeLanguages()) {
                long start = System.currentTimeMillis();

                orderedMentions = new TreeSet<>(DataAccess.getMentionsforLanguage(language, true));
                trie[language.getID()] = TrieBuilder.buildTrie(orderedMentions);

                long dur = System.currentTimeMillis() - start;
                logger.info("Initialized NamedEntitySpotter '" + language + "' in " + dur/1_000 + "s.");
            }

        } catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }


    @Override public void process(JCas jCas) throws AnalysisEngineProcessException {
        Integer runningId = RunningTimer.recordStartTime("NamedEntitySpotter");
        Language language = Language.getLanguageForString(jCas.getDocumentLanguage());
        Collection<Token> tokens = JCasUtil.select(jCas, Token.class);
        Set<Integer> tokenEnd = new HashSet<>();
        Set<Integer> tokenBegin = new HashSet<>();
        for (Token token : tokens) {
          try {
            if (!StopWord.isStopwordOrSymbol(token.getCoveredText(), language)) {//TODO: what would happen if the stop word is in the middle of the mention??
                tokenEnd.add(token.getEnd());
                tokenBegin.add(token.getBegin());
            }
          } catch (EntityLinkingDataAccessException e) {
            throw new AnalysisEngineProcessException(
                    "No stop words defined for language " + language + ". Entity Spotting will not work!",
                    null,
                    e);
          }
        }
        String text = jCas.getDocumentText();

        try {
            Set<Spot> spots = TextSpotter.spotTrieEntriesInTextIgnoreCase(trie[language.getID()], text,tokenBegin, tokenEnd, MATCHING_RATIO, true);
            for (Spot spot : spots) {
                String mentionSpotted = Utils.getStringbyKey(spot.getMatch(), trie[language.getID()]);
                String mention = text.substring(spot.getBegin(), spot.getEnd());
//        System.out.println("Entity candidate: " + mention + " - " + mentionSpotted);

                // For the exact match: since even if matching ration is 1.0, very small differences are passed in the TextSpotter.
                if(MATCHING_RATIO == 1.0 && !mentionSpotted.equalsIgnoreCase(mention)) {
                    continue;
                }
                NamedEntityCandidate candid = new NamedEntityCandidate(jCas, spot.getBegin(), spot.getEnd());
                candid.addToIndexes();
            }
        } catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        RunningTimer.recordEndTime("NamedEntitySpotter", runningId);
    }
}
