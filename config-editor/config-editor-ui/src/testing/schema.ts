export const mockConfigSchema =
{
    description: 'Parsers configuration',
    properties: {
      parsers_configurations: {
        description: 'List of parser configurations',
        items: {
          description: 'Parser specification',
          properties: {
            parser_attributes: {
              description: 'Attributes for parser settings',
              properties: {
                parser_type: {
                  description: 'The type of the parser',
                  enum: [
                    'generic',
                    'syslog',
                    'netflow',
                  ],
                  type: 'string',
                },
                syslog_config: {
                  description: 'The configuration for Syslog parser',
                  properties: {
                    merge_sd_elements: {
                      default: false,
                      description: 'Merge SD elements into one parsed object',
                      type: 'boolean',
                    },
                    syslog_version: {
                      default: 'RFC_3164, RFC_5424',
                      description: 'Expected version of syslog message',
                      enum: [
                        'RFC_3164, RFC_5424',
                        'RFC_3164',
                        'RFC_5424',
                      ],
                      type: 'string',
                    },
                    time_formats: {
                      description: 'Time formats used for time formatting. If not provided syslog default time formats are used',
                      items: {
                        description: 'Time format for timestamp parsing',
                        properties: {
                          time_format: {
                            description: 'Time format used by Java DateTimeFormatter',
                            type: 'string',
                          },
                          timezone: {
                            default: 'UTC',
                            description: 'Timezone used by the time formatter',
                            type: 'string',
                          },
                          validation_regex: {
                            description: 'validation regular expression for checking format of the timestamp',
                            type: 'string',
                          },
                        },
                        required: [
                          'time_format',
                        ],
                        title: 'time format',
                        type: 'object',
                      },
                      type: 'array',
                      widget: {
                        formlyConfig: {
                          wrappers: [
                            'expansion-panel',
                          ],
                        },
                      },
                    },
                    timezone: {
                      default: 'UTC',
                      description: 'Timezone used in syslog default time formats. Not applicable for custom time_formats.',
                      type: 'string',
                    },
                  },
                  title: 'syslog config',
                  type: 'object',
                  widget: {
                    formlyConfig: {
                      hideExpression: 'field.parent.model.parser_type !== \'syslog\'',
                      wrappers: [
                        'expansion-panel',
                      ],
                    },
                  },
                },
              },
              required: [
                'parser_type',
              ],
              title: 'parser attributes',
              type: 'object',
            },
            parser_author: {
              description: 'Author of the parser',
              type: 'string',
            },
            parser_description: {
              description: 'Description of the parser',
              type: 'string',
            },
            parser_extractors: {
              description: 'Specification of parser extractors',
              items: {
                description: 'Parser extractor specification',
                properties: {
                  attributes: {
                    description: 'The attributes of the extractor and related functions',
                    properties: {
                      column_names: {
                        description: 'Specification for selecting right column names',
                        items: {
                          description: 'Names of fields along with a filter',
                          properties: {
                            column_filter: {
                              description: 'Filter for applying the names. The size of names array is used when filter is not provided',
                              properties: {
                                index: {
                                  description: 'Index in the array started from 0',
                                  type: 'integer',
                                },
                                required_value: {
                                  description: 'Required string on the index position',
                                  type: 'string',
                                },
                              },
                              required: [
                                'index',
                                'required_value',
                              ],
                              title: 'column filter',
                              type: 'object',
                            },
                            names: {
                              description: 'Names of fields according to columns order, use a skip_character \'_\' if you do not want to include the column',
                              items: {
                                type: 'string',
                              },
                              type: 'array',
                            },
                          },
                          required: [
                            'names',
                          ],
                          title: 'column names',
                          type: 'object',
                        },
                        type: 'array',
                        widget: {
                          formlyConfig: {
                            hideExpression: 'field.parent.parent.model.extractor_type  !== \'csv_extractor\'',
                            wrappers: [
                              'expansion-panel',
                            ],
                          },
                        },
                      },
                      conversion_exclusions: {
                        default: [
                          'timestamp',
                        ],
                        description: 'List of fields excluded from string converting',
                        items: {
                          type: 'string',
                        },
                        minItems: 0,
                        type: 'array',
                        widget: {
                          formlyConfig: {
                            hideExpression: '!(field.parent.parent.model.post_processing_functions && field.parent.parent.model.post_processing_functions.includes(\'convert_to_string\'))',
                            wrappers: [
                              'expansion-panel',
                            ],
                          },
                        },
                      },
                      dot_all_regex_flag: {
                        default: true,
                        description: 'The regular expression \'.\' matches any character - including a line terminator',
                        type: 'boolean',
                        widget: {
                          formlyConfig: {
                            hideExpression: 'field.parent.parent.model.extractor_type  !== \'pattern_extractor\'',
                          },
                        },
                      },
                      escaped_character: {
                        default: '\\',
                        description: 'Escaped character for escaping quotes, delimiters, brackets',
                        type: 'string',
                        widget: {
                          formlyConfig: {
                            hideExpression: 'field.parent.parent.model.extractor_type  !== \'key_value_extractor\'',
                          },
                        },
                      },
                      escaping_handling: {
                        default: true,
                        description: 'Handling escaping during parsing, e.g., string searching',
                        type: 'boolean',
                        widget: {
                          formlyConfig: {
                            hideExpression: 'field.parent.parent.model.extractor_type  !== \'key_value_extractor\'',
                          },
                        },
                      },
                      key_value_delimiter: {
                        default: '=',
                        description: 'Key-value delimiter used for splitting key value pairs',
                        type: 'string',
                        widget: {
                          formlyConfig: {
                            hideExpression: 'field.parent.parent.model.extractor_type  !== \'key_value_extractor\'',
                          },
                        },
                      },
                      nested_separator: {
                        default: ':',
                        description: 'The separator added during unfolding nested json objects',
                        type: 'string',
                        widget: {
                          formlyConfig: {
                            hideExpression: 'field.parent.parent.model.extractor_type  !== \'json_extractor\'',
                          },
                        },
                      },
                      next_key_strategy: {
                        default: false,
                        description: 'Strategy for key value extraction: key-value delimiter is found first and then the word delimiter is searched back',
                        type: 'boolean',
                        widget: {
                          formlyConfig: {
                            hideExpression: 'field.parent.parent.model.extractor_type  !== \'key_value_extractor\'',
                          },
                        },
                      },
                      path_prefix: {
                        description: 'The prefix added to extracted field name',
                        type: 'string',
                        widget: {
                          formlyConfig: {
                            hideExpression: 'field.parent.parent.model.extractor_type  !== \'json_extractor\'',
                          },
                        },
                      },
                      quota_value_handling: {
                        default: true,
                        description: 'Handling quotes during parsing, e.g., string searching',
                        type: 'boolean',
                        widget: {
                          formlyConfig: {
                            hideExpression: 'field.parent.parent.model.extractor_type  !== \'key_value_extractor\'',
                          },
                        },
                      },
                      regex_select_config: {
                        description: 'The specification of regex_select extractor',
                        properties: {
                          default_value: {
                            description: 'Default value when no pattern matches',
                            type: 'string',
                          },
                          output_field: {
                            description: 'Output field for selected value',
                            type: 'string',
                          },
                          patterns: {
                            description: 'Search patterns for selecting value',
                            items: {
                              description: 'Regular expression select search pattern',
                              properties: {
                                name: {
                                  description: 'The value that will be added to the output_field after pattern matching',
                                  type: 'string',
                                },
                                pattern: {
                                  description: 'Regular expression pattern',
                                  type: 'string',
                                  widget: {
                                    formlyConfig: {
                                      templateOptions: {
                                        link: 'https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html',
                                      },
                                      wrappers: [
                                        'form-field',
                                        'help-link',
                                      ],
                                    },
                                  },
                                },
                              },
                              required: [
                                'pattern',
                              ],
                              title: 'search pattern',
                              type: 'object',
                              widget: {
                                formlyConfig: {
                                  wrappers: [
                                    'expansion-panel',
                                  ],
                                },
                              },
                            },
                            minItems: 0,
                            type: 'array',
                          },
                        },
                        required: [
                          'output_field',
                          'patterns',
                        ],
                        title: 'reg select',
                        type: 'object',
                        widget: {
                          formlyConfig: {
                            hideExpression: 'field.parent.parent.model.extractor_type  !== \'regex_select_extractor\'',
                            wrappers: [
                              'expansion-panel',
                            ],
                          },
                        },
                      },
                      regular_expressions: {
                        description: 'List of regular expressions',
                        items: {
                          type: 'string',
                        },
                        minItems: 0,
                        type: 'array',
                        widget: {
                          formlyConfig: {
                            hideExpression: 'field.parent.parent.model.extractor_type  !== \'pattern_extractor\'',
                            templateOptions: {
                              link: 'https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html',
                            },
                            wrappers: [
                              'help-link',
                              'panel',
                            ],
                          },
                        },
                      },
                      remove_quotes: {
                        default: true,
                        description: 'Extractor removes quotes in the extracted values',
                        type: 'boolean',
                      },
                      rename_duplicate_keys: {
                        default: true,
                        description: 'Rename duplicate keys instead of overwriting or ignoring fields with the same keys',
                        type: 'boolean',
                        widget: {
                          formlyConfig: {
                            hideExpression: 'field.parent.parent.model.extractor_type  !== \'key_value_extractor\'',
                          },
                        },
                      },
                      should_match_pattern: {
                        default: false,
                        description: 'At least one pattern should match otherwise the extractor throws an exception',
                        type: 'boolean',
                        widget: {
                          formlyConfig: {
                            hideExpression: 'field.parent.parent.model.extractor_type  !== \'pattern_extractor\'',
                          },
                        },
                      },
                      should_overwrite_fields: {
                        default: false,
                        description: 'Extractor will overwrite an existing field with the same name',
                        type: 'boolean',
                      },
                      should_remove_field: {
                        default: false,
                        description: 'Extractor will remove field after the extraction',
                        type: 'boolean',
                      },
                      skip_empty_values: {
                        default: false,
                        description: 'Skipping extracted empty strings',
                        type: 'boolean',
                      },
                      skipping_column_name: {
                        default: '_',
                        description: 'The name that can be used for skipping fields during extraction',
                        type: 'string',
                        widget: {
                          formlyConfig: {
                            hideExpression: 'field.parent.parent.model.extractor_type  !== \'csv_extractor\'',
                          },
                        },
                      },
                      string_replace_replacement: {
                        description: 'Replacement that will replace target',
                        type: 'string',
                        widget: {
                          formlyConfig: {
                            hideExpression: '!(field.parent.parent.model.pre_processing_function && (field.parent.parent.model.pre_processing_function.includes(\'string_replace\') || field.parent.parent.model.pre_processing_function.includes(\'string_replace_all\')))',
                          },
                        },
                      },
                      string_replace_target: {
                        description: 'Target that will be replaced by replacement',
                        type: 'string',
                        widget: {
                          formlyConfig: {
                            hideExpression: '!(field.parent.parent.model.pre_processing_function && (field.parent.parent.model.pre_processing_function.includes(\'string_replace\') || field.parent.parent.model.pre_processing_function.includes(\'string_replace_all\')))',
                          },
                        },
                      },
                      thrown_exception_on_error: {
                        default: false,
                        description: 'Extractor throws exception on error (recommended for testing), otherwise it skips the further processing',
                        type: 'boolean',
                      },
                      time_formats: {
                        description: 'The specification of time formats',
                        items: {
                          description: 'Time format for timestamp parsing',
                          properties: {
                            time_format: {
                              description: 'Time format used by Java DateTimeFormatter',
                              type: 'string',
                              widget: {
                                formlyConfig: {
                                  templateOptions: {
                                    link: 'https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html',
                                  },
                                  wrappers: [
                                    'form-field',
                                    'help-link',
                                  ],
                                },
                              },
                            },
                            timezone: {
                              default: 'UTC',
                              description: 'Timezone used by the time formatter',
                              type: 'string',
                            },
                            validation_regex: {
                              description: 'validation regular expression for checking format of the timestamp',
                              type: 'string',
                              widget: {
                                formlyConfig: {
                                  templateOptions: {
                                    link: 'https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html',
                                  },
                                  wrappers: [
                                    'form-field',
                                    'help-link',
                                  ],
                                },
                              },
                            },
                          },
                          required: [
                            'time_format',
                          ],
                          title: 'time format',
                          type: 'object',
                        },
                        minItems: 0,
                        type: 'array',
                        widget: {
                          formlyConfig: {
                            hideExpression: '!(field.parent.parent.model.post_processing_functions && field.parent.parent.model.post_processing_functions.includes(\'format_timestamp\'))',
                            wrappers: [
                              'expansion-panel',
                            ],
                          },
                        },
                      },
                      timestamp_field: {
                        default: 'timestamp',
                        description: 'The field used in formatting timestamp',
                        type: 'string',
                        widget: {
                          formlyConfig: {
                            hideExpression: '!(field.parent.parent.model.post_processing_functions && (field.parent.parent.model.post_processing_functions.includes(\'convert_unix_timestamp\') || field.parent.parent.model.post_processing_functions.includes(\'format_timestamp\')))',
                          },
                        },
                      },
                      word_delimiter: {
                        default: ' ',
                        description: 'Word delimiter used for splitting words',
                        type: 'string',
                        widget: {
                          formlyConfig: {
                            hideExpression: '!(field.parent.parent.model.extractor_type  === \'csv_extractor\' || field.parent.parent.model.extractor_type  === \'key_value_extractor\')',
                          },
                        },
                      },
                    },
                    title: 'extractor attributes',
                    type: 'object',
                  },
                  extractor_type: {
                    description: 'The extractor type',
                    enum: [
                      'pattern_extractor',
                      'key_value_extractor',
                      'csv_extractor',
                      'json_extractor',
                      'regex_select_extractor',
                    ],
                    type: 'string',
                  },
                  field: {
                    description: 'The field on which the extractor is applied',
                    type: 'string',
                  },
                  name: {
                    description: 'The name of the extractor',
                    type: 'string',
                  },
                  post_processing_functions: {
                    description: 'The postprocessing function applied after the extractor',
                    items: {
                      enum: [
                        'convert_unix_timestamp',
                        'format_timestamp',
                        'convert_to_string',
                      ],
                      type: 'string',
                    },
                    minItems: 0,
                    type: 'array',
                    widget: {
                      formlyConfig: {
                        wrappers: [
                          'expansion-panel',
                        ],
                      },
                    },
                  },
                  pre_processing_function: {
                    description: 'The pre-processing function applied before the extractor',
                    enum: [
                      'string_replace',
                      'string_replace_all',
                    ],
                    type: 'string',
                  },
                },
                required: [
                  'attributes',
                  'field',
                  'name',
                  'extractor_type',
                ],
                title: 'parser extractor',
                type: 'object',
                widget: {
                  formlyConfig: {
                    wrappers: [
                      'expansion-panel',
                    ],
                  },
                },
              },
              minItems: 0,
              type: 'array',
            },
            parser_name: {
              description: 'Name of the parser',
              pattern: '^[a-zA-Z0-9_\\-]+$',
              type: 'string',
            },
            parser_version: {
              description: 'Version of the parser',
              type: 'integer',
            },
            transformations: {
              description: 'Specification of parser transformations applied after parsing',
              items: {
                description: 'The specification of transformation',
                properties: {
                  attributes: {
                    description: 'The attributes of the transformation',
                    properties: {
                      case_type: {
                        default: 'lowercase',
                        description: 'The type of case',
                        enum: [
                          'lowercase',
                          'uppercase',
                        ],
                        type: 'string',
                        widget: {
                          formlyConfig: {
                            hideExpression: '!(field.parent.parent.model.transformation_type === \'field_name_change_case\')',
                          },
                        },
                      },
                      field_rename_map: {
                        description: 'Mapping for field rename transformation',
                        items: {
                          description: 'Specification for renaming fields',
                          properties: {
                            field_to_rename: {
                              description: 'Field name that should be renamed',
                              type: 'string',
                            },
                            new_name: {
                              description: 'New field name after renaming',
                              type: 'string',
                            },
                          },
                          required: [
                            'field_to_rename',
                            'new_name',
                          ],
                          title: 'field rename',
                          type: 'object',
                          widget: {
                            formlyConfig: {
                              wrappers: [
                                'panel',
                              ],
                            },
                          },
                        },
                        minItems: 0,
                        type: 'array',
                        widget: {
                          formlyConfig: {
                            hideExpression: 'field.parent.parent.model.transformation_type !== \'rename_fields\'',
                            wrappers: [
                              'expansion-panel',
                            ],
                          },
                        },
                      },
                      fields_filter: {
                        description: 'Filter for selecting the fields for the transformation',
                        properties: {
                          excluding_fields: {
                            description: 'Excluding patterns of the filter',
                            items: {
                              type: 'string',
                            },
                            minItems: 0,
                            type: 'array',
                            widget: {
                              formlyConfig: {
                                templateOptions: {
                                  link: 'https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html',
                                },
                                wrappers: [
                                  'panel',
                                  'help-link',
                                ],
                              },
                            },
                          },
                          including_fields: {
                            description: 'Including patterns of the filter',
                            items: {
                              type: 'string',
                            },
                            minItems: 0,
                            type: 'array',
                            widget: {
                              formlyConfig: {
                                templateOptions: {
                                  link: 'https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html',
                                },
                                wrappers: [
                                  'help-link',
                                  'panel',
                                ],
                              },
                            },
                          },
                        },
                        required: [
                          'including_fields',
                        ],
                        title: 'fields filter',
                        type: 'object',
                        widget: {
                          formlyConfig: {
                            hideExpression: '!(field.parent.parent.model.transformation_type === \'delete_fields\' || field.parent.parent.model.transformation_type === \'trim_value\' || field.parent.parent.model.transformation_type === \'chomp_value\' || field.parent.parent.model.transformation_type === \'lowercase_value\' || field.parent.parent.model.transformation_type === \'uppercase_value\')',
                            wrappers: [
                              'expansion-panel',
                            ],
                          },
                        },
                      },
                      message_filter: {
                        description: 'Filter for filtering the whole message',
                        properties: {
                          matchers: {
                            description: 'Matchers for the message filter',
                            items: {
                              description: 'Specification for message filter matcher',
                              properties: {
                                field_name: {
                                  description: 'The name of the field for matching',
                                  type: 'string',
                                },
                                negated: {
                                  default: false,
                                  description: 'The matcher is negated',
                                  type: 'boolean',
                                },
                                pattern: {
                                  description: 'Regular expression for matching the field value',
                                  type: 'string',
                                  widget: {
                                    formlyConfig: {
                                      templateOptions: {
                                        link: 'https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html',
                                      },
                                      wrappers: [
                                        'form-field',
                                        'help-link',
                                      ],
                                    },
                                  },
                                },
                              },
                              required: [
                                'field_name',
                                'pattern',
                              ],
                              title: 'message filter matcher',
                              type: 'object',
                              widget: {
                                formlyConfig: {
                                  wrappers: [
                                    'panel',
                                  ],
                                },
                              },
                            },
                            minItems: 0,
                            type: 'array',
                          },
                        },
                        required: [
                          'matchers',
                        ],
                        title: 'message filter',
                        type: 'object',
                        widget: {
                          formlyConfig: {
                            hideExpression: '!(field.parent.parent.model.transformation_type === \'filter_message\')',
                            wrappers: [
                              'expansion-panel',
                            ],
                          },
                        },
                      },
                      string_replace_replacement: {
                        description: 'Replacement will replace target in JAVA String replace function',
                        type: 'string',
                        widget: {
                          formlyConfig: {
                            hideExpression: '!(field.parent.parent.model.transformation_type === \'field_name_string_replace\' || field.parent.parent.model.transformation_type === \'field_name_string_replace_all\')',
                          },
                        },
                      },
                      string_replace_target: {
                        description: 'target that will be replaced in JAVA String replace function',
                        type: 'string',
                        widget: {
                          formlyConfig: {
                            hideExpression: '!(field.parent.parent.model.transformation_type === \'field_name_string_replace\' || field.parent.parent.model.transformation_type === \'field_name_string_replace_all\' || field.parent.parent.model.transformation_type === \'field_name_string_delete_all\')',
                          },
                        },
                      },
                    },
                    title: 'transformation attributes',
                    type: 'object',
                  },
                  transformation_type: {
                    description: 'The type of the transformation',
                    enum: [
                      'field_name_string_replace',
                      'field_name_string_replace_all',
                      'field_name_string_delete_all',
                      'field_name_change_case',
                      'rename_fields',
                      'delete_fields',
                      'trim_value',
                      'lowercase_value',
                      'uppercase_value',
                      'chomp_value',
                      'filter_message',
                    ],
                    type: 'string',
                  },
                },
                required: [
                  'attributes',
                  'transformation_type',
                ],
                title: 'transformation',
                type: 'object',
                widget: {
                  formlyConfig: {
                    wrappers: [
                      'expansion-panel',
                    ],
                  },
                },
              },
              minItems: 0,
              type: 'array',
            },
          },
          required: [
            'parser_author',
            'parser_attributes',
            'parser_name',
            'parser_version',
          ],
          title: 'parser config',
          type: 'object',
          widget: {
            formlyConfig: {
              type: 'tabs',
            },
          },
        },
        minItems: 0,
        type: 'array',
      },
      parsers_version: {
        description: 'Version of the parsers config',
        type: 'integer',
      },
    },
    required: [
      'parsers_configurations',
      'parsers_version',
    ],
    title: 'parsers config',
    type: 'object',
  };
