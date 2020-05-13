package uk.co.gresearch.siembol.response.common;

import java.util.List;

public interface ResponsePlugin {
    List<RespondingEvaluatorFactory> getRespondingEvaluatorFactories();
}
