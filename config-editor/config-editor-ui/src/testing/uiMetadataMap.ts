import { UiMetadataMap, UiMetadata } from '@app/model/ui-metadata-map';

export const mockUiMetadataParser: UiMetadata = {
    author: 'parser_author',
    release: {
        config_array: 'parsers_configurations',
        version: 'parsers_version',
    },
    description: 'parser_description',
    labelsFunc: 'const ret = []; if (model.parser_attributes) {ret.push(model.parser_attributes.parser_type);} if (model.parser_extractors && model.parser_extractors.length > 0){for(const p of model.parser_extractors) {ret.push(p.extractor_type)} } return ret;',
    name: 'parser_name',
    perConfigSchemaPath: 'properties.parsers_configurations.items',
    testing: {
        releaseTestEnabled: true,
        perConfigTestEnabled: true,
        testCaseEnabled: true,
    },
    version: 'parser_version',
};

export const mockUiMetadataAlert: UiMetadata =
{
    author: 'rule_author',
    release: {
        config_array: 'rules',
        extras: [
            'tags',
            'rules_protection',
        ],
        version: 'rules_version',
    },
    description: 'rule_description',
    labelsFunc: 'const ret = [\'SourceType:\' + model.source_type]; if (model.tags !== undefined) { ret.push(...model.tags.map(t => t.tag_name + \':\' + t.tag_value));} return ret;',
    name: 'rule_name',
    perConfigSchemaPath: 'properties.rules.items',
    testing: {
        releaseTestEnabled: true,
        perConfigTestEnabled: true,
        testCaseEnabled: true,
    },
    version: 'rule_version',
};

export const mockUiMetadataMap: UiMetadataMap =
{alert: mockUiMetadataAlert, parserconfig: mockUiMetadataParser};
