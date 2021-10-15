package uk.co.gresearch.siembol.configeditor.sync.service;

import uk.co.gresearch.siembol.common.model.EnrichmentTableDto;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

public interface EnrichmentTablesProvider {
    ConfigEditorResult getEnrichmentTables(String serviceName);
    ConfigEditorResult addEnrichmentTable(String serviceName, EnrichmentTableDto enrichmentTable);
    ConfigEditorResult updateEnrichmentTable(String serviceName, EnrichmentTableDto enrichmentTable);
}
