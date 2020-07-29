package de.mpg.mpi_inf.ambiversenlu.nlu.model.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMention;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import org.apache.uima.jcas.JCas;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DocumentAnnotations {

  private List<Map.Entry<Integer, Integer>> mentionsNE = new ArrayList<>();
  private List<Map.Entry<Integer, Integer>> mentionsC = new ArrayList<>();

  public boolean addMention(int offset, int length, boolean isNamedEntity) {
    if (isNamedEntity)
      return mentionsNE.add(new AbstractMap.SimpleEntry<Integer, Integer>(offset, length));
    else
      return mentionsC.add(new AbstractMap.SimpleEntry<Integer, Integer>(offset, length));
  }

  public void addMentionsToJCas(JCas jCas) {
    for (Entry<Integer, Integer> mention : mentionsNE) {
      NamedEntity ne = new NamedEntity(jCas, mention.getKey(), mention.getKey() + mention.getValue());
      ne.setValue("manual");
      ne.addToIndexes();
    }
    for (Entry<Integer, Integer> mention : mentionsC) {
      ConceptMention conceptMention = new ConceptMention(jCas, mention.getKey(), mention.getKey() + mention.getValue());
      conceptMention.addToIndexes();
    }
  }

}
