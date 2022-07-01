package uk.co.gresearch.siembol.configeditor.service.enrichments;

import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.configeditor.common.ConfigTesterBase;
import uk.co.gresearch.siembol.configeditor.common.ConfigTesterFlag;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.enrichments.compiler.EnrichmentCompiler;

import java.util.EnumSet;

import static uk.co.gresearch.siembol.configeditor.service.enrichments.EnrichmentSchemaService.fromEnrichmentResult;

public class EnrichmentConfigTester extends ConfigTesterBase<EnrichmentCompiler> {
    public EnrichmentConfigTester(SiembolJsonSchemaValidator testValidator,
                                  String testSchema,
                                  EnrichmentCompiler compiler) {
        super(testValidator, testSchema, compiler);
    }

    @Override
    public ConfigEditorResult testConfiguration(String configuration, String testSpecification) {
        return fromEnrichmentResult(testProvider.testConfiguration(configuration, testSpecification));
    }

    @Override
    public ConfigEditorResult testConfigurations(String configurations, String testSpecification) {
        return fromEnrichmentResult(testProvider.testConfigurations(configurations, testSpecification));
    }

    @Override
    public EnumSet<ConfigTesterFlag> getFlags() {
        return EnumSet.of(ConfigTesterFlag.CONFIG_TESTING,
                ConfigTesterFlag.TEST_CASE_TESTING);
    }
}
