package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Token;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Tokens;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NamedEntityCandidate;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.*;


public class FilterNamedEntitiesByPOStagsNounPhrases extends JCasAnnotator_ImplBase {

  /** Here we are sorting phrases based by the most right-side position and then length.
   * This is based on the simplified assumption that in English grammer the head noun of the noun phrase
   * is at the end of the phrase.
   * Example: "[[[first-person {{{role-playing]]] video game}}} we will give preference to
   * "role-playing video game" although it is shorter than the other potential mention. **/
  public static Comparator<NamedEntityCandidate> lengthComparator = new Comparator<NamedEntityCandidate>(){
    @Override
    public int compare(NamedEntityCandidate o1, NamedEntityCandidate o2) {
      if (o2.getEnd() > o1.getEnd()) 
        return 1;
      else if (o2.getEnd() < o1.getEnd())
        return -1;
      else
        return ((o2.getEnd() - o2.getBegin()) - (o1.getEnd() - o1.getBegin()));
    }
  };

//  /** This is merely sorting the mentions by length **/
//  public static Comparator<NamedEntityCandidate> lengthComparator = new Comparator<NamedEntityCandidate>(){
//    @Override
//    public int compare(NamedEntityCandidate o1, NamedEntityCandidate o2) {
//      if ((o2.getEnd() - o2.getBegin()) > (o1.getEnd() - o1.getBegin()))
//        return 1;
//      else if ((o2.getEnd() - o2.getBegin()) < (o1.getEnd() - o1.getBegin()))
//        return -1;
//      else if (o2.getEnd() > o1.getEnd())
//        return 1;
//      else
//        return -1;
//    }
//  };

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    Tokens tokens = Tokens.getTokensFromJCas(jCas);
    Collection<NamedEntityCandidate> neMentionCandidatesJcas = JCasUtil.select(jCas, NamedEntityCandidate.class);
    List<NamedEntityCandidate> neMentionsSorted = new ArrayList<>(neMentionCandidatesJcas);
    Collections.sort(neMentionsSorted, lengthComparator);
    
    Map<Integer, Map<Integer, String>> tokensPOStags = new HashMap<Integer, Map<Integer,String>>();
    for (Token t: tokens) {
      tokensPOStags.putIfAbsent(t.getBeginIndex(), new HashMap<>());
      tokensPOStags.get(t.getBeginIndex()).put(t.getEndIndex(), t.getPOS());
    }

    RangeSet<Integer> added = TreeRangeSet.create();
    //ccMentionsSorted is sorted based on: firstly end position of the mention and secondly the length of the mention.
    for (NamedEntityCandidate cc : neMentionsSorted) {
      // Avoid overlaps.
      if (added.contains(cc.getBegin()) || added.contains(cc.getEnd())) {
        continue;
      }
      
      //Exists at least one noun in the phrase (POS tags).
      if (!isNounPhrase(tokensPOStags, cc.getBegin(), cc.getEnd())) {
        continue;
      }
      
      NamedEntity neMention = new NamedEntity(jCas, cc.getBegin(), cc.getEnd());
      neMention.addToIndexes();
      added.add(Range.closed(cc.getBegin(), cc.getEnd()));
    }
  }

  public static boolean isNounPhrase(Map<Integer, Map<Integer, String>> tokenPos, int begin, int end) {
    for (int s = begin; s<=end; s++) {
      for (int e = begin; e<=end; e++) {
        if (tokenPos.get(s) != null && tokenPos.get(s).get(e) != null) {
          if (tokenPos.get(s).get(e).toLowerCase().startsWith("n")) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
