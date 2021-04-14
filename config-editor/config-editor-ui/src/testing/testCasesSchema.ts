export const mockTestCasesSchema =
{
    description: 'Test case for testing configurations',
    properties: {
      assertions: {
        description: 'Test case assertions',
        items: {
          description: 'Test assertion used in test case',
          properties: {
            active: {
              default: true,
              description: 'The pattern is active and included in test case evaluation',
              type: 'boolean',
            },
            assertion_type: {
              description: 'The type of assertion',
              enum: [
                'path_and_value_matches',
                'only_if_path_exists',
              ],
              type: 'string',
            },
            description: {
              description: 'The description of the assertion',
              type: 'string',
              widget: {
                formlyConfig: {
                  type: 'textarea',
                },
              },
            },
            expected_pattern: {
              description: 'Regular expression pattern of expectedPattern value',
              type: 'string',
              widget: {
                formlyConfig: {
                  type: 'textarea',
                },
              },
            },
            json_path: {
              description: 'Json path for obtaing an actual value for assertion evaluation',
              type: 'string',
              widget: {
                formlyConfig: {
                  type: 'textarea',
                },
              },
            },
            negated_pattern: {
              default: false,
              description: 'The pattern is negatedPattern',
              type: 'boolean',
            },
          },
          required: [
            'assertion_type',
            'expected_pattern',
            'json_path',
          ],
          title: 'test assertion',
          type: 'object',
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
      author: {
        description: 'Author of the test case',
        type: 'string',
        widget: {
          formlyConfig: {
            hideExpression: true,
          },
        },
      },
      config_name: {
        description: 'Version of the test case',
        type: 'string',
        widget: {
          formlyConfig: {
            hideExpression: true,
          },
        },
      },
      description: {
        description: 'Description of the test case',
        type: 'string',
        widget: {
          formlyConfig: {
            type: 'textarea',
          },
        },
      },
      test_case_name: {
        description: 'The name of the test case',
        pattern: '^[a-zA-Z0-9_\\-]+$',
        type: 'string',
        widget: {
          formlyConfig: {
            hideExpression: 'model.version !== 0',
          },
        },
      },
      test_specification: {
        description: 'Test specification',
        title: 'json raw string',
        type: 'object',
      },
      version: {
        description: 'Version of the test case',
        type: 'integer',
        widget: {
          formlyConfig: {
            hideExpression: true,
          },
        },
      },
    },
    required: [
      'assertions',
      'author',
      'config_name',
      'test_case_name',
      'test_specification',
      'version',
    ],
    title: 'test case',
    type: 'object',
  };
