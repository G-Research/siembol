package uk.co.gresearch.siembol.parsers.extractors;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

@SuppressWarnings("unnassigned")
public class RegexSelectExtractorTest {
    private String name = "test_name";
    private String field = "test_field";
    private EnumSet<ParserExtractor.ParserExtractorFlags> extractorFlags;
    private List<Pair<String, String>> patterns = new ArrayList<>();

    /**
     * node=nqptick1 type=EOE msg=audit(1526397806.509:3436363485):
     **/
    @Multiline
    private String auditdMessage;
    private String auditdRegex = "^node=";

    /**
     * <13>Jan 14 13:26:58 prod-1.k8s.abc fluentd:    stream:stdout   docker:{"container_id"=>"88751a072197197da7fa50987c485c04fdd7325a98831a533291ac113b558278"}        kubernetes:{"container_name"=>"dummy", "namespace_name"=>"dev", "pod_name"=>"dummy", "container_image"=>"unknown.net/service:cwh10r-gb4ys-km514euza-3azyc-niutqq", "container_image_id"=>"docker-pullable://docker.artifactory.net", "pod_id"=>"416af93b-15c3-11e9-add7-48df3701a2c4", "labels"=>{"app"=>"abc", "master"=>"false", "pod-template-hash"=>"1710645932"}, "host"=>"abc", "master_url"=>"https://1.2.3.4:443/api", "namespace_id"=>"3993c1be-b01f-11e8-bee0-30e1716064fc", "namespace_labels"=>{"istio-injection"=>"disabled", "opa-validating-webhook"=>"enabled", "spooning"=>"dev"}}
     **/
    @Multiline
    private String k8sMessage;
    private String k8sRegex = "^<\\d+>\\w+\\s+\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\s+[\\w-\\.]+\\.k8s";

    /**
     * <85>1 2019-02-18T17:37:47 10.18.9.141 CP-GW - Log [Fields@1.3.6.1.4.1.2620 Action="accept" UUid="{0x5c6aed6a,0x67,0x8d09120a,0xc0000002}" rule="313" rule_uid="{DB44DDE8-CD96-4B37-8A14-3E978D5BC322}" rule_name="RZ Splunk Fwd and Dep" src="192.168.55.11" dst="192.168.41.43" proto="6" product="VPN-1 & FireWall-1" service="9997" s_port="50172" product_family="Network"]
     **/
    @Multiline
    private String checkpointMessage;
    private String checkpointRegex = "^<\\d+>\\d+\\s+\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(.\\d{6})?([+-]\\d{2}:\\d{2})?\\s+[^\\s]+\\sCP-GW";

    /**
     * CEF:0|2019-02-18T17:37:47Z|GSPARK3|Cyber-Ark|Vault|9.99.0000|295|Retrieve password|5|act="Retrieve password" duser="dummmy" fname="Root\abc.net" src="172.22.1.1" cs1Label="Affected User Name" cs1="" cs2Label="Safe Name" cs2="AD" cs3Label="Location" cs3="" cs4Label="Property Name" cs4="" cs5Label="Target User Name" cs5="" cs6Label="Gateway Address" cs6="" cn1Label="Request Id" cn1="" msg="[AppID: Switches] Getting account password for Switches", , Retrieve password
     **/
    @Multiline
    private String cyberarkMessage;
    private String cyberarkRegex = "(?i)(\\w+\\s+\\d{1,2}\\s+\\d{2}:\\d{2}:\\d{2}|.+?\\|.+?\\|)\\s*...ark\\d";

    /**
     * this is a simple message
     **/
    @Multiline
    String simpleMessage;


    @Before
    public void setUp() {
        patterns.add(Pair.of("auditd", auditdRegex));

        patterns.add(Pair.of("k8s", k8sRegex));

        patterns.add(Pair.of("checkpoint", checkpointRegex));

        patterns.add(Pair.of("cyberark", cyberarkRegex));

        patterns.add(Pair.of("nomatch", "^asdfasdf"));
    }


    @Test
    public void testAuditDNoDefault() {
        RegexSelectExtractor extractor = RegexSelectExtractor.builder()
                .patterns(patterns)
                .outputField("logical_source_type")
                .defaultValue(null)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(auditdMessage.trim());
        Assert.assertEquals("auditd", out.get("logical_source_type"));
    }

    @Test
    public void testK8sNoDefault() {
        RegexSelectExtractor extractor = RegexSelectExtractor.builder()
                .patterns(patterns)
                .outputField("logical_source_type")
                .defaultValue(null)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();
        Map<String, Object> out = extractor.extract(k8sMessage.trim());
        Assert.assertEquals("k8s", out.get("logical_source_type"));
    }

    @Test
    public void testCheckpointNoDefault() {
        RegexSelectExtractor extractor = RegexSelectExtractor.builder()
                .patterns(patterns)
                .outputField("logical_source_type")
                .defaultValue(null)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();
        Map<String, Object> out = extractor.extract(checkpointMessage.trim());
        Assert.assertEquals("checkpoint", out.get("logical_source_type"));
    }

    @Test
    public void testCyberarkNoDefault() {
        RegexSelectExtractor extractor = RegexSelectExtractor.builder()
                .patterns(patterns)
                .outputField("logical_source_type")
                .defaultValue(null)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();
        Map<String, Object> out = extractor.extract(cyberarkMessage.trim());
        Assert.assertEquals("cyberark", out.get("logical_source_type"));
    }

    @Test
    public void testNothingNoDefault() {
        RegexSelectExtractor extractor = RegexSelectExtractor.builder()
                .patterns(patterns)
                .outputField("logical_source_type")
                .defaultValue(null)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();
        Map<String, Object> out = extractor.extract(simpleMessage.trim());
        Assert.assertNull(out.get("logical_source_type"));
    }

    @Test
    public void testNothingWithDefault() {
        RegexSelectExtractor extractor = RegexSelectExtractor.builder()
                .patterns(patterns)
                .outputField("logical_source_type")
                .defaultValue("default_topic")
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();
        Map<String, Object> out = extractor.extract(simpleMessage.trim());
        Assert.assertEquals("default_topic", out.get("logical_source_type"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoOutputField() {
        RegexSelectExtractor extractor = RegexSelectExtractor.builder()
                .patterns(patterns)
                .outputField(null)
                .defaultValue("default_topic")
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();
        Map<String, Object> out = extractor.extract(simpleMessage.trim());
        Assert.assertNull(out);
    }
}
