package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NamedEntityCandidate;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.Collection;

public class FilterNamedEntitiesByNothing extends JCasAnnotator_ImplBase {
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    Collection<NamedEntityCandidate> mentionCandidatesJcas = JCasUtil.select(aJCas, NamedEntityCandidate.class);
    for (NamedEntityCandidate cc : mentionCandidatesJcas) {
      NamedEntity mention = new NamedEntity(aJCas, cc.getBegin(), cc.getEnd());
      mention.addToIndexes();
    }
  }
}
