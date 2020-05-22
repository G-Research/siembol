package uk.co.gresearch.siembol.response.engine;

import uk.co.gresearch.siembol.response.common.Evaluable;
import uk.co.gresearch.siembol.response.common.RespondingResult;

public interface ResponseEngine extends Evaluable {
    RespondingResult getRulesMetadata();
}
