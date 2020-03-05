package uk.co.gresearch.nortem.parsers.syslog;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.nortem.parsers.common.NortemParser;
import uk.co.gresearch.nortem.parsers.factory.ParserFactory;
import uk.co.gresearch.nortem.parsers.factory.ParserFactoryImpl;

import java.util.List;
import java.util.Map;


public class BasicSyslogNortemParserTest {
    private NortemParser syslogParser;
    private NortemParser syslogBsdParser;
    private ParserFactory factory;

    /**
     * {
     *   "parser_attributes": {
     *     "parser_type": "syslog",
     *     "syslog_config": {
     *     "syslog_version": "RFC_5424",
     *     "timezone": "UTC"
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String syslogConfigRfc_5424;

    /**
     * {
     *   "parser_attributes": {
     *     "parser_type": "syslog",
     *     "syslog_config": {
     *     "syslog_version": "RFC_5424",
     *     "timezone": "UTC",
     *     "merge_sd_elements" : true
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String syslogConfigRfc_5424MergeSdElements;

    /**
     * {
     *   "parser_attributes": {
     *     "parser_type": "syslog",
     *     "syslog_config": {
     *     "syslog_version": "RFC_3164",
     *     "timezone": "UTC"
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String syslogConfigBsd;

    /**
     * {
     *   "parser_attributes": {
     *     "parser_type": "syslog",
     *     "syslog_config": {
     *     "syslog_version": "RFC_3164",
     *     "timezone": "Europe/London"
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String syslogConfigBsdLondonTimezone;

    /**
     * {
     *   "parser_attributes": {
     *     "parser_type": "syslog",
     *     "syslog_config": {
     *       "syslog_version": "RFC_5424",
     *       "time_formats": [
     *       {
     *         "timezone": "UTC",
     *         "time_format": "yyyy-MM-dd'T'HH:mm:ss'Z'"
     *       }]
     *     }
     *   },
     *   "parser_extractors": [
     *   {
     *     "extractor_type": "pattern_extractor",
     *     "name": "dummy",
     *     "field": "syslog_msg",
     *     "attributes": {
     *       "regular_expressions": [
     *         "^SALscanner INFO TEST:\\s(?<info_msg>.*)$"
     *         ],
     *       "should_remove_field": false
     *     }
     *   }
     *   ],
     *   "transformations" : [
     *   {
     *      "transformation_type": "field_name_string_replace_all",
     *      "attributes": {
     *        "string_replace_target": "syslog",
     *        "string_replace_replacement": "dummy"
     *      }
     *  }]
     * }
     **/
    @Multiline
    public static String syslogConfigCustomTimpestamp;

    /**
     * <85>1 2018-05-22T17:07:41+01:00 172.16.18.101 CP-GW - Log [Fields@1.3.6.1.4.1.2620 Action="accept" UUid="{0x5b04404c,0x10004,0x651210ac,0xc0000000}" rule="14" rule_uid="{28F2CB68-9017-442B-8C64-6BD43B8082CD}" rule_name="DNS" src="172.16.16.20" dst="172.16.37.100" proto="17" product="VPN-1 & FireWall-1" service="53" s_port="60349" product_family="Network"]
     **/
    @Multiline
    public static String goodSyslogCheckpoint1;

    /**
     * <81>1 2018-05-22T03:05:37 172.19.34.31 CP-GW - Alert [Fields@1.3.6.1.4.1.2620 Action=" " UUid="{0x0,0x0,0x0,0x0}" Protection Name="Packet Sanity" Severity="2" Confidence Level="5" protection_id="PacketSanity" SmartDefense Profile="Perimeter_Protection" Performance Impact="1" Industry Reference="CAN-2002-1071" Protection Type="anomaly" Attack Info="Invalid TCP flag combination" attack="Malformed Packet" Total logs="24" Suppressed logs="23" proto="6" dst="10.254.101.253" src="10.254.101.12" product="SmartDefense" FollowUp="Not Followed" product_family="Network"]
     **/
    @Multiline
    public static String goodSyslogCheckpoint2;


    /**
     * <81>1 2018-05-22T03:05:37 172.19.34.31 CP-GW - Alert [Fields@1.3.6.1.4.1.2620 Action=" " UUid="{0x0,0x0,0x0,0x0}" Protection Name="Packet\" \] Sanity"]
     **/
    @Multiline
    public static String goodSyslogEscapedChars;

    /**
     * <81>1 2018-05-22T03:05:37 172.19.34.31 CP-GW - Alert [Fields@1.3.6.1.4.1.2620 Action=" " Protection Name="Packet" \] Sanity"][Fields@1.3.6.1.4.1.2620] BOMabcabc
     **/
    @Multiline
    public static String syslogEscapedChars2;

    /**
     * <81>1 2018-05-22T03:05:37 172.19.34.31 CP-GW - Alert - BOMabcabc
     **/
    @Multiline
    public static String goodNilSD;

    /**
     * <34>Oct 11 22:14:15 mymachine su: 'su root' failed for dummy on /dev/pts/8
     **/
    @Multiline
    public static String goodBSD;

    /**
     * <85>1 2018-08-01T09:00:24+01:00 10.254.112.76 CP-GW - Log [Fields@1.3.6.1.4.1.2620 Action=" " UUid="{0x0,0x0,0x0,0x0}" default_device_message="<133>xpand[17978]: admin localhost t +installer:packages:Check_Point_R77_30_JUMBO_HF_1_Bundle_T286_FULL.tgz:has_metadata 0 (+)" facility="local use 0" syslog_severity="Notice" product="Syslog" product_family="Network"]
     **/
    @Multiline
    public static String strangeCheckpoint;


    /**
     * <190>1 2019-01-15T12:36:05Z mime1-eqld.uberit.net sal - - - SALscanner INFO TEST: [manlistEmail] applianceupdate.clearswift.com ... [688]
     */
    @Multiline
    public static String customTimeformat;

    /**
     * <85>1 2018-05-22T17:07:41+01:00 172.16.18.101 CP-GW - Log [Fields@1.3.6.1.4.1.2620 Action1="accept"][Fields@1.3.6.1.4.1.2620 Action2="deny"]
     **/
    @Multiline
    public static String multipleSdElementsDummyCheckpoint1;

    /**
     * <85>1 2018-05-22T17:07:41Z 172.16.18.101 CP-GW - Log [Fields@1.3.6.1.4.1.2620 syslog1="accept"][Fields@1.3.6.1.4.1.2620 syslog2="deny"]
     **/
    @Multiline
    public static String multipleSdElementsDummyCheckpoint2;


    @Before
    public void setUp() throws Exception {
        factory = ParserFactoryImpl.createParserFactory();
        syslogParser = factory.create(syslogConfigRfc_5424).getAttributes().getNortemParser();
        syslogBsdParser = factory.create(syslogConfigBsd).getAttributes().getNortemParser();
    }

    @Test
    public void goodSyslogCheckpoint1() {

        Map<String, Object> out = syslogParser.parse(goodSyslogCheckpoint1.trim().getBytes()).get(0);

        Assert.assertEquals(1, out.get("syslog_version"));
        Assert.assertEquals(5, out.get("syslog_severity"));
        Assert.assertEquals(10, out.get("syslog_facility"));
        Assert.assertEquals(85, out.get("syslog_priority"));
        Assert.assertEquals("172.16.18.101", out.get("syslog_hostname"));
        Assert.assertEquals("CP-GW", out.get("syslog_appname"));
        Assert.assertEquals("Log", out.get("syslog_msg_id"));

        Assert.assertEquals("172.16.16.20", out.get("src"));
        Assert.assertEquals("60349", out.get("s_port"));
        Assert.assertEquals(1527005261000L, out.get("timestamp"));
        Assert.assertEquals("Fields@1.3.6.1.4.1.2620", out.get("syslog_sd_id"));
    }

    @Test
    public void invalidTimestampSyslogCheckpoint() {

        Map<String, Object> out = syslogParser.parse(goodSyslogCheckpoint1.trim().
                replace("2018-05-22T17:07:41+01:00", "INVALID")
                .getBytes())
                .get(0);

        Assert.assertEquals(1, out.get("syslog_version"));
        Assert.assertEquals(5, out.get("syslog_severity"));
        Assert.assertEquals(10, out.get("syslog_facility"));
        Assert.assertEquals(85, out.get("syslog_priority"));
        Assert.assertEquals("172.16.18.101", out.get("syslog_hostname"));
        Assert.assertEquals("CP-GW", out.get("syslog_appname"));
        Assert.assertEquals("Log", out.get("syslog_msg_id"));

        Assert.assertEquals("172.16.16.20", out.get("src"));
        Assert.assertEquals("60349", out.get("s_port"));
        Assert.assertEquals("INVALID", out.get("syslog_timestamp"));
        Assert.assertNotNull(out.get("timestamp"));
        Assert.assertEquals("Fields@1.3.6.1.4.1.2620", out.get("syslog_sd_id"));
    }

    @Test
    public void nonstandardConfigStandardTimestampCheckpoint() {
        Map<String, Object> out = syslogParser.parse(goodSyslogCheckpoint1.trim().getBytes()).get(0);

        Assert.assertEquals(1527005261000L, out.get("timestamp"));
        Assert.assertNull(out.get("syslog_timestamp"));
    }

    @Test
    public void goodSyslogCheckpoint2() {
        Map<String, Object> out = syslogParser.parse(goodSyslogCheckpoint2.trim().getBytes()).get(0);

        Assert.assertEquals(1, out.get("syslog_version"));
        Assert.assertEquals(1, out.get("syslog_severity"));
        Assert.assertEquals(10, out.get("syslog_facility"));
        Assert.assertEquals(81, out.get("syslog_priority"));
        Assert.assertEquals("172.19.34.31", out.get("syslog_hostname"));
        Assert.assertEquals("CP-GW", out.get("syslog_appname"));
        Assert.assertEquals("Alert", out.get("syslog_msg_id"));
        Assert.assertEquals("5", out.get("Confidence Level"));
        Assert.assertEquals(1526958337000L, out.get("timestamp"));
        Assert.assertEquals("Fields@1.3.6.1.4.1.2620", out.get("syslog_sd_id"));
    }

    @Test
    public void goodEscapedChars() {
        Map<String, Object> out = syslogParser.parse(goodSyslogEscapedChars.trim().getBytes()).get(0);

        Assert.assertEquals(1, out.get("syslog_version"));
        Assert.assertEquals(1, out.get("syslog_severity"));
        Assert.assertEquals(10, out.get("syslog_facility"));
        Assert.assertEquals(81, out.get("syslog_priority"));
        Assert.assertEquals("172.19.34.31", out.get("syslog_hostname"));
        Assert.assertEquals("CP-GW", out.get("syslog_appname"));
        Assert.assertEquals("Alert", out.get("syslog_msg_id"));
        Assert.assertEquals("Packet\\\" \\] Sanity", out.get("Protection Name"));
        Assert.assertEquals(1526958337000L, out.get("timestamp"));
        Assert.assertEquals("Fields@1.3.6.1.4.1.2620", out.get("syslog_sd_id"));
    }

    @Test(expected = java.lang.IllegalStateException.class)
    public void EscapedChars2() {
        Map<String, Object> out = syslogParser.parse(syslogEscapedChars2.trim().getBytes()).get(0);

        Assert.assertNull(out);
    }

    @Test
    public void goodNilSd() {
        Map<String, Object> out = syslogParser.parse(goodNilSD.trim().getBytes()).get(0);

        Assert.assertEquals(1, out.get("syslog_version"));
        Assert.assertEquals(1, out.get("syslog_severity"));
        Assert.assertEquals(10, out.get("syslog_facility"));
        Assert.assertEquals(81, out.get("syslog_priority"));
        Assert.assertEquals("172.19.34.31", out.get("syslog_hostname"));
        Assert.assertEquals("CP-GW", out.get("syslog_appname"));
        Assert.assertEquals("Alert", out.get("syslog_msg_id"));
        Assert.assertEquals("abcabc", out.get("syslog_msg"));
    }

    @Test
    public void goodBSD() throws Exception {
        Map<String, Object> out = syslogBsdParser.parse(goodBSD.trim().getBytes()).get(0);

        Assert.assertEquals(0, out.get("syslog_version"));
        Assert.assertEquals(2, out.get("syslog_severity"));
        Assert.assertEquals(4, out.get("syslog_facility"));
        Assert.assertEquals(34, out.get("syslog_priority"));
        Assert.assertEquals("mymachine", out.get("syslog_hostname"));
        Assert.assertEquals("su: 'su root' failed for dummy on /dev/pts/8", out.get("syslog_msg"));

    }

    @Test
    public void goodBSDWithBSDTZ() throws Exception {
        syslogBsdParser = factory.create(syslogConfigBsdLondonTimezone.trim()).getAttributes().getNortemParser();
        Map<String, Object> out = syslogBsdParser.parse(goodBSD.trim().getBytes()).get(0);

        Assert.assertEquals(0, out.get("syslog_version"));
        Assert.assertEquals(2, out.get("syslog_severity"));
        Assert.assertEquals(4, out.get("syslog_facility"));
        Assert.assertEquals(34, out.get("syslog_priority"));
        Assert.assertEquals("mymachine", out.get("syslog_hostname"));
        Assert.assertEquals("su: 'su root' failed for dummy on /dev/pts/8", out.get("syslog_msg"));
    }

    @Test
    public void goodBSDWithUTCTZ() throws Exception {
        Map<String, Object> out = syslogBsdParser.parse(goodBSD.trim().getBytes()).get(0);

        Assert.assertEquals(0, out.get("syslog_version"));
        Assert.assertEquals(2, out.get("syslog_severity"));
        Assert.assertEquals(4, out.get("syslog_facility"));
        Assert.assertEquals(34, out.get("syslog_priority"));
        Assert.assertEquals("mymachine", out.get("syslog_hostname"));
        Assert.assertEquals("su: 'su root' failed for dummy on /dev/pts/8", out.get("syslog_msg"));
    }

    @Test
    public void unescapedBracketCheckpoint() {
        Map<String, Object> out = syslogParser.parse(strangeCheckpoint.trim().getBytes()).get(0);

        Assert.assertEquals(1, out.get("syslog_version"));
        Assert.assertEquals("<133>xpand[17978]: admin localhost t +installer:packages:Check_Point_R77_30_JUMBO_HF_1_Bundle_T286_FULL.tgz:has_metadata 0 (+)", out.get("default_device_message"));
        Assert.assertEquals("10.254.112.76", out.get("syslog_hostname"));
        Assert.assertEquals("{0x0,0x0,0x0,0x0}", out.get("UUid"));

    }

    @Test
    public void customTimestamp() throws Exception {
        syslogParser = factory.create(syslogConfigCustomTimpestamp).getAttributes().getNortemParser();
        Map<String, Object> out = syslogParser.parse(customTimeformat.trim().getBytes()).get(0);

        Assert.assertEquals(1547555765000L, out.get("timestamp"));
        Assert.assertEquals("mime1-eqld.uberit.net", out.get("dummy_hostname"));
        Assert.assertEquals("sal", out.get("dummy_appname"));
        Assert.assertEquals(1, out.get("dummy_version"));
        Assert.assertEquals(23, out.get("dummy_facility"));
        Assert.assertEquals(6, out.get("dummy_severity"));
        Assert.assertEquals("SALscanner INFO TEST: [manlistEmail] applianceupdate.clearswift.com ... [688]", out.get("dummy_msg"));
        Assert.assertEquals("[manlistEmail] applianceupdate.clearswift.com ... [688]", out.get("info_msg"));
    }

    @Test
    public void customTimestampInvalid() throws Exception {
        syslogParser = factory.create(syslogConfigCustomTimpestamp).getAttributes().getNortemParser();
        Map<String, Object> out = syslogParser.parse(customTimeformat.trim()
                .replace("2019-01-15T12:36:05Z", "INVALID")
                .getBytes())
                .get(0);

        Assert.assertEquals("INVALID", out.get("dummy_timestamp"));
        Assert.assertNotNull(out.get("timestamp"));
        Assert.assertEquals("mime1-eqld.uberit.net", out.get("dummy_hostname"));
        Assert.assertEquals("sal", out.get("dummy_appname"));
        Assert.assertEquals(1, out.get("dummy_version"));
        Assert.assertEquals(23, out.get("dummy_facility"));
        Assert.assertEquals(6, out.get("dummy_severity"));
        Assert.assertEquals("SALscanner INFO TEST: [manlistEmail] applianceupdate.clearswift.com ... [688]", out.get("dummy_msg"));
        Assert.assertEquals("[manlistEmail] applianceupdate.clearswift.com ... [688]", out.get("info_msg"));
    }

    @Test
    public void mergingSdparameters(){
        syslogParser = factory.create(syslogConfigRfc_5424MergeSdElements).getAttributes().getNortemParser();
        List<Map<String, Object>> out = syslogParser.parse(multipleSdElementsDummyCheckpoint1.trim().getBytes());
        Assert.assertEquals(1, out.size());
        Map<String, Object> current = out.get(0);
        Assert.assertEquals("accept", current.get("Action1"));
        Assert.assertEquals("deny", current.get("Action2"));
        Assert.assertEquals("Fields@1.3.6.1.4.1.2620", current.get("syslog_sd_id_0"));
        Assert.assertEquals("Fields@1.3.6.1.4.1.2620", current.get("syslog_sd_id_1"));
    }

    @Test
    public void noMergeSdparameters(){
        List<Map<String, Object>> out = syslogParser.parse(multipleSdElementsDummyCheckpoint1.trim().getBytes());
        Assert.assertEquals(2, out.size());
        Map<String, Object> current1 = out.get(0);
        Map<String, Object> current2 = out.get(1);
        Assert.assertEquals(current2.size(), current1.size());
        Assert.assertEquals("accept", current1.get("Action1"));
        Assert.assertEquals("deny", current2.get("Action2"));
        Assert.assertEquals("Fields@1.3.6.1.4.1.2620", current1.get("syslog_sd_id"));
        Assert.assertEquals("Fields@1.3.6.1.4.1.2620", current2.get("syslog_sd_id"));
    }

    @Test
    public void noMergeSdparametersExtractAndTransform(){
        syslogParser = factory.create(syslogConfigCustomTimpestamp).getAttributes().getNortemParser();
        List<Map<String, Object>> out = syslogParser.parse(multipleSdElementsDummyCheckpoint2.trim().getBytes());
        Assert.assertEquals(2, out.size());
        Map<String, Object> current1 = out.get(0);
        Map<String, Object> current2 = out.get(1);
        Assert.assertEquals(current2.size(), current1.size());
        Assert.assertEquals("accept", current1.get("dummy1"));
        Assert.assertEquals("deny", current2.get("dummy2"));
        Assert.assertEquals("Fields@1.3.6.1.4.1.2620", current1.get("dummy_sd_id"));
        Assert.assertEquals("Fields@1.3.6.1.4.1.2620", current2.get("dummy_sd_id"));
    }
}

