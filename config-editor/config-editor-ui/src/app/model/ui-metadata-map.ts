export interface UiMetadataMap {
    name: string,
    version: string,
    author: string,
    description: string,
    labelsFunc: string,
    testing: TestConfig,
    enableSensorFields: boolean,
    perConfigSchemaPath: string,
    deployment: DeploymentConfig,
}

export interface TestConfig {
    perConfigTestEnabled: boolean,
    deploymentTestEnabled: boolean,
    helpMessage: string
}

export interface DeploymentConfig {
    version: string,
    config_array: string,
    extras?: string[],
}
