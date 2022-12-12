package uk.co.gresearch.siembol.enrichments.storm.common;

import org.apache.commons.lang3.tuple.Pair;
import java.util.ArrayList;
/**
 * A serializable object for representing list of a pair of strings
 *
 * <p>This class implements serializable interface and is used for representing list of a pair of strings.
 *
 * @author Marian Novotny
 */
public class EnrichmentPairs extends ArrayList<Pair<String, String>> {
    private static final long serialVersionUID = 1L;
}
