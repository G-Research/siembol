import { Config, ConfigData } from '@app/model';
import { cloneDeep } from 'lodash';

export const mockParserConfigData: ConfigData =
{
    parser_attributes: {
        parser_type: 'generic',
    },
    parser_author: 'siembol',
    parser_extractors: [
        {
            attributes: {
                nested_separator: ':',
                remove_quotes: true,
                should_overwrite_fields: false,
                should_remove_field: false,
                skip_empty_values: false,
                thrown_exception_on_error: false,
            },
            extractor_type: 'json_extractor',
            field: 'original_string',
            name: 'json',
        },
    ],
    parser_name: 'config1',
    parser_version: 2,
    transformations: [
        {
            attributes: {
                fields_filter: {
                    including_fields: [
                        '.*',
                    ],
                },
            },
            transformation_type: 'lowercase_value',
        },
    ],
};

export const mockParserConfigFile =
{
    content: mockParserConfigData,
    file_history: [
        {
            added: 2,
            author: 'siembol',
            date: '2021-03-12T16:26:08',
            removed: 2,
        },
    ],
    file_name: 'config1.json',
};

export const mockParserConfigFiles =
{
    files: [
        mockParserConfigFile,
    ],
};

export const mockParserConfigMin: Config =
{
    author: 'siembol',
    configData: mockParserConfigData,
    description: undefined,
    isNew: false,
    name: 'config1',
    savedInBackend: true,
    tags: [
        'generic',
        'json_extractor',
    ],
    version: 2,
    versionFlag: -1,
};

export const mockParserConfig: Config =
{
    author: 'siembol',
    configData: mockParserConfigData,
    description: undefined,
    fileHistory: [
        {
            added: 2,
            author: 'siembol',
            date: '2021-03-12T16:26:08',
            removed: 2,
        },
    ],
    isReleased: false,
    isNew: false,
    name: 'config1',
    savedInBackend: true,
    tags: [
        'generic',
        'json_extractor',
    ],
    testCases: [],
    version: 2,
    versionFlag: -1,

};

const mockParserConfigDataCloned = cloneDeep(mockParserConfigData);
mockParserConfigDataCloned.parser_name += '_clone';
mockParserConfigDataCloned.parser_version = 0;

export const mockParserConfigCloned: Config =
{
      author: 'siembol',
      configData: mockParserConfigDataCloned,
      description: 'cloned from config1',
      isNew: true,
      name: 'config1_clone',
      savedInBackend: false,
      testCases: [],
      version: 0,
    };
