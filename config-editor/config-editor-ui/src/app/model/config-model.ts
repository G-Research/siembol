import { TestCase, TestCaseWrapper, TestCaseEvaluationResult } from './test-case';
import { JSONSchema7 } from 'json-schema';
import { Observable } from 'rxjs';


export enum Type {
    CONFIG_TYPE = 'Config',
    TESTCASE_TYPE = 'TestCase',
    ADMIN_TYPE = 'Admin'
}

export enum UserRole {
    SERVICE_USER = 'service_user',
    SERVICE_ADMIN = 'service_admin'
}

export interface SubmitDialogData {
    name: string,
    type: string,
    validate: () => Observable<any>;
    submit: () => Observable<boolean>;
}

export interface GitFiles<T> {
    files: T[];
}

export interface TestCaseEvaluation {
    files: Content<TestCase>[];
    test_result_raw_output: string;
}

export interface GeneralRule {
    file_name?: string;
}

export interface Content<T> extends GeneralRule {
    content: T;
}

export interface ServiceInfo {
    name: string;
    type: string;
    user_roles: UserRole[];
}

export interface UserInfo {
    user_name: string;
    services: ServiceInfo[];

}

export interface RepositoryLinksWrapper {
    rules_repositories: RepositoryLinks;
}

export interface RepositoryLinks {
    rule_store_directory_url: string;
    rules_release_directory_url: string;
    test_case_store_directory_url: string;
    admin_config_store_directory_url: string;
    service_name: string;
}

export interface SchemaInfo {
    rules_schema: JSONSchema7; 
}

export interface AdminSchemaInfo {
    admin_config_schema: JSONSchema7;
}

export interface TestSchemaInfo {
    test_schema: JSONSchema7;
}

export interface PullRequestInfo {
    pull_request_pending: boolean;
    pull_request_url: string;
}

export interface Config {
    versionFlag?: number;
    isDeployed?: boolean;
    isNew: boolean;
    configData: ConfigData;
    savedInBackend: boolean;
    name: string;
    author: string;
    version: number;
    description: string;
    tags?: string[];
    fileHistory?: FileHistory[];
    testCases: TestCaseWrapper[];
}

export interface AdminConfig{
    configData: ConfigData;
    version: number;
    fileHistory?: FileHistory[];
}

export interface FileHistory {
    author: string;
    date: string;
    removed: number;
    added: number;
}

export interface ConfigTestDto {
    files: Content<ConfigData>[],
    test_specification: string,
}

export type ConfigData = any;

export interface Deployment {
    configs: Config[];
    deploymentVersion: number;
}

export interface ConfigTestResult {
    exception?: string;
    message?: string;
    test_result_output?: string;
    test_result_complete?: boolean;
    test_result_raw_output?: object;
}

export interface DeploymentWrapper {
    storedDeployment: Deployment;
    deploymentHistory: FileHistory[];
}

export interface TestCaseResultAttributes {
    exception?: string;
    message?: string;
    test_case_result?: TestCaseEvaluationResult;
}

