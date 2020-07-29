export interface UiMetadataMap {
    name: string,
    version: string,
    author: string,
    description: string,
    labelsFunc: string,
    testing: TestConfig,
    perConfigSchemaPath: string,
    deployment: DeploymentConfig,
    unionType?: UnionType,
}

export interface TestConfig {
    perConfigTestEnabled: boolean,
    deploymentTestEnabled: boolean,
    helpMessage: string,
    testCaseEnabled: boolean,
    eventName: string,
    eventHelper: string,
}

export interface DeploymentConfig {
    version: string,
    config_array: string,
    extras?: string[],
}

export interface UnionType {
    unionPath: string;
    unionSelectorName: string;
}
