package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.util.HashSet;
import java.util.Set;

public class EntityConceptSpottingJointDisambiguation extends Pipeline {
    @Override
    void addSteps() {
        for (Language language : supportedLanguages()) {
            String upperCaseLanguage = language.name().toUpperCase();
            String next = upperCaseLanguage + "_TOKENIZER";
            addstep(upperCaseLanguage, next);
            addstep(next, upperCaseLanguage + "_POS");
            addstep(upperCaseLanguage + "_POS", Component.ENTITY_SPOTTER_EXACT.name());
            addstep(Component.ENTITY_SPOTTER_EXACT.name(), Component.FILTER_ENTITIES_BY_POS_NOUNPHRASES.name());
            addstep(Component.FILTER_ENTITIES_BY_POS_NOUNPHRASES.name(), Component.CONCEPT_SPOTTER_EXACT.name());
            addstep(Component.CONCEPT_SPOTTER_EXACT.name(), Component.FILTER_CONCEPTS_BY_POS_NOUNPHRASES.name());
            addstep(Component.FILTER_CONCEPTS_BY_POS_NOUNPHRASES.name(), Component.JD.name());
        }
    }

    @Override
    Set<Language> supportedLanguages() {
        Set<Language> supported = new HashSet<>();
        supported.add(Language.getLanguageForString("en"));//for now only en
        return supported;    }
}
