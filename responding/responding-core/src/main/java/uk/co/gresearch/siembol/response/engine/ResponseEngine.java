package uk.co.gresearch.siembol.response.engine;

import uk.co.gresearch.siembol.response.common.Evaluable;
import uk.co.gresearch.siembol.response.common.RespondingResult;
/**
 * An object for evaluating response alerts using response rules
 *
 * <p>This interface extends Evaluable interface.
 * It is used for evaluating response alerts and providing metadata about the rules.
 *
 * @author  Marian Novotny
 * @see Evaluable
 */
public interface ResponseEngine extends Evaluable {
    /**
     * Gets rules metadata with rules json schema
     * @return the responding result with rules json schema
     * @see RespondingResult
     */
    RespondingResult getRulesMetadata();
}
