package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.co.gresearch.siembol.alerts.model.MatcherDto;
import uk.co.gresearch.siembol.alerts.model.MatcherTypeDto;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;
import uk.co.gresearch.siembol.configeditor.service.alerts.sigma.model.SigmaDetectionDto;

import java.util.*;

public class SigmaSearchTest {
    /**
     * iptables:
     *   Image|endswith: '/service'
     *   CommandLine|contains|all:
     *     - 'iptables'
     *     - 'stop'
     *     - 1
     * keywords:
     *   - entered promiscuous mode
     *   - 1
     *   - test secret word
     * keyword:
     *   - single
     */
    @Multiline
    private static String sigmaDetectionExample;

    /**
     * iptables:
     *   Image: null
     *   test: ''
     */
    @Multiline
    private static String sigmaDetectionExampleEmptyValues;

    /**
     * iptables:
     *   Image: 'abc'
     *   CommandLine|contains|all:
     *     - true
     *     - 1
     */
    @Multiline
    private static String sigmaDetectionExampleBooleanValue;

    private static final ObjectReader SIGMA_DETECTION_READER = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readerFor(SigmaDetectionDto.class);

    private SigmaDetectionDto detection;
    private Map<String, JsonNode> searchesMap;
    private SigmaSearch.Builder builder;
    private SigmaSearch search;

    @Before
    public void Setup() throws JsonProcessingException {
        detection = SIGMA_DETECTION_READER.readValue(sigmaDetectionExample);
        searchesMap = detection.getSearchesMap();
        Assert.assertNotNull(detection);
    }

    @Test
    public void buildSearchListOk() {
        builder = new SigmaSearch.Builder(SigmaSearch.SearchType.LIST, "keywords");
        builder.addList(searchesMap.get("keywords"));
        search = builder.build();

        Assert.assertEquals("keywords", search.getIdentifier());
        List<MatcherDto> matchers = search.getSiembolMatchers();
        Assert.assertEquals(1, matchers.size());
        Assert.assertEquals(SiembolMessageFields.ORIGINAL.toString(), matchers.get(0).getField());
        Assert.assertEquals(false, matchers.get(0).getNegated());
        Assert.assertEquals(MatcherTypeDto.REGEX_MATCH, matchers.get(0).getType());
        Assert.assertNotNull(matchers.get(0).getData());
    }

    @Test
    public void buildSearchListSingleOk() {
        builder = new SigmaSearch.Builder(SigmaSearch.SearchType.LIST, "keyword");
        builder.addList(searchesMap.get("keyword"));
        search = builder.build();

        Assert.assertEquals("keyword", search.getIdentifier());
        List<MatcherDto> matchers = search.getSiembolMatchers();
        Assert.assertEquals(1, matchers.size());
        Assert.assertEquals(SiembolMessageFields.ORIGINAL.toString(), matchers.get(0).getField());
        Assert.assertEquals(false, matchers.get(0).getNegated());
        Assert.assertEquals(MatcherTypeDto.REGEX_MATCH, matchers.get(0).getType());
        Assert.assertNotNull(matchers.get(0).getData());
    }

    @Test
    public void buildMapOk() {
        builder = new SigmaSearch.Builder(SigmaSearch.SearchType.MAP, "iptables");

        searchesMap.get("iptables")
                .fieldNames()
                .forEachRemaining(x -> builder.addMapEntry(x, searchesMap.get("iptables").get(x)));
        search = builder.build();

        Assert.assertEquals("iptables", search.getIdentifier());
        List<MatcherDto> matchers = search.getSiembolMatchers();
        Assert.assertEquals(2, matchers.size());

        Assert.assertEquals("Image", matchers.get(0).getField());
        Assert.assertEquals(false, matchers.get(0).getNegated());
        Assert.assertEquals(MatcherTypeDto.REGEX_MATCH, matchers.get(0).getType());
        Assert.assertNotNull(matchers.get(0).getData());

        Assert.assertEquals("CommandLine", matchers.get(1).getField());
        Assert.assertEquals(false, matchers.get(1).getNegated());
        Assert.assertEquals(MatcherTypeDto.REGEX_MATCH, matchers.get(1).getType());
        Assert.assertNotNull(matchers.get(1).getData());

    }

    @Test
    public void buildMapFieldRenamingOk() {
        builder = new SigmaSearch.Builder(SigmaSearch.SearchType.MAP, "iptables");

        Map<String, String> fieldMapping = new HashMap<>();
        fieldMapping.put("Image", "siembol_image");
        fieldMapping.put("Secret", "siembol_secret");
        builder.fieldMapping(fieldMapping);

        searchesMap.get("iptables")
                .fieldNames()
                .forEachRemaining(x -> builder.addMapEntry(x, searchesMap.get("iptables").get(x)));
        search = builder.build();

        Assert.assertEquals("iptables", search.getIdentifier());
        List<MatcherDto> matchers = search.getSiembolMatchers();
        Assert.assertEquals(2, matchers.size());

        Assert.assertEquals("siembol_image", matchers.get(0).getField());
        Assert.assertEquals(false, matchers.get(0).getNegated());
        Assert.assertEquals(MatcherTypeDto.REGEX_MATCH, matchers.get(0).getType());
        Assert.assertNotNull(matchers.get(0).getData());

        Assert.assertEquals("CommandLine", matchers.get(1).getField());
        Assert.assertEquals(false, matchers.get(1).getNegated());
        Assert.assertEquals(MatcherTypeDto.REGEX_MATCH, matchers.get(1).getType());
        Assert.assertNotNull(matchers.get(1).getData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildAddWrongList() {
        builder = new SigmaSearch.Builder(SigmaSearch.SearchType.LIST, "keywords");
        builder.addList(new ArrayList<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildEmptySearch() {
        new SigmaSearch.Builder(SigmaSearch.SearchType.MAP, "keywords").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildWrongListSearch() {
        new SigmaSearch.Builder(SigmaSearch.SearchType.LIST, "keywords")
                .addList(searchesMap.get("iptables"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildWrongMapSearch() {
        new SigmaSearch.Builder(SigmaSearch.SearchType.MAP, "iptables")
                .addMapEntry(null, searchesMap.get("iptables"));
    }

    @Test
    public void buildWithNullAndEmptyValues() throws JsonProcessingException {
        detection = SIGMA_DETECTION_READER.readValue(sigmaDetectionExampleEmptyValues);
        searchesMap = detection.getSearchesMap();
        Assert.assertNotNull(detection);

        builder = new SigmaSearch.Builder(SigmaSearch.SearchType.MAP, "iptables");
        searchesMap.get("iptables")
                .fieldNames()
                .forEachRemaining(x -> builder.addMapEntry(x, searchesMap.get("iptables").get(x)));
        search = builder.build();
        List<MatcherDto> matchers = search.getSiembolMatchers();
        Assert.assertEquals(2, matchers.size());
        Assert.assertEquals(MatcherTypeDto.REGEX_MATCH, matchers.get(0).getType());
        Assert.assertTrue(matchers.get(0).getNegated());
        Assert.assertEquals("Image", matchers.get(0).getField());
        Assert.assertEquals(".*", matchers.get(0).getData());

        Assert.assertEquals(MatcherTypeDto.REGEX_MATCH, matchers.get(1).getType());
        Assert.assertFalse(matchers.get(1).getNegated());
        Assert.assertEquals("test", matchers.get(1).getField());
        Assert.assertEquals("^$", matchers.get(1).getData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildWithBooleanValues() throws JsonProcessingException {
        detection = SIGMA_DETECTION_READER.readValue(sigmaDetectionExampleBooleanValue);
        searchesMap = detection.getSearchesMap();
        Assert.assertNotNull(detection);

        builder = new SigmaSearch.Builder(SigmaSearch.SearchType.MAP, "iptables");
        searchesMap.get("iptables")
                .fieldNames()
                .forEachRemaining(x -> builder.addMapEntry(x, searchesMap.get("iptables").get(x)));
    }

}
