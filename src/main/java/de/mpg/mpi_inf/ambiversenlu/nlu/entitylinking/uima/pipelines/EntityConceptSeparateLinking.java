package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.util.HashSet;
import java.util.Set;

public class EntityConceptSeparateLinking extends Pipeline {
    @Override
    void addSteps() {
        for (Language language : supportedLanguages()) {
            String upperCaseLanguage = language.name().toUpperCase();
            String next = upperCaseLanguage + "_TOKENIZER";
            addstep(upperCaseLanguage, next);
            addstep(next, upperCaseLanguage + "_POS");
            addstep(upperCaseLanguage + "_POS", Component.AIDA_NO_RESULTS.name());
            addstep(Component.AIDA_NO_RESULTS.name(), Component.CD_NO_RESULTS.name());
        }
    }

    @Override
    Set<Language> supportedLanguages() {
        Set<Language> supported = new HashSet<>();
            supported.add(Language.getLanguageForString("en"));//for now only en
        return supported;
    }
}
