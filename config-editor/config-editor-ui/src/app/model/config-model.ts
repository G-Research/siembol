import { TestCase, TestCaseWrapper, TestCaseEvaluationResult, TestCaseMap } from './test-case';
import { JSONSchema7 } from 'json-schema';
import { Observable } from 'rxjs';

export const NAME_REGEX = '^[a-zA-Z0-9_\\-]+$';

export const repoNames = {
  store_directory_name: 'Config Store Folder',
  release_directory_name: 'Config Release Folder',
  testcase_store_directory_name: 'Config Testcase Folder',
  admin_config_store_directory_name: 'Admin Config Folder',
};

export enum TestingType {
  RELEASE_TESTING = 'release_testing',
  CONFIG_TESTING = 'config_testing',
}

export enum Type {
  CONFIG_TYPE = 'Config',
  TESTCASE_TYPE = 'TestCase',
  ADMIN_TYPE = 'Admin',
}

export enum UserRole {
  SERVICE_USER = 'service_user',
  SERVICE_ADMIN = 'service_admin',
}

export interface SubmitDialogData {
  name: string;
  type: string;
  validate: () => Observable<any>;
  submit: () => Observable<boolean>;
}

export interface GitFiles<T> {
  files: T[];
}

export interface GitFilesDelete<T> {
  configs_files: T[];
  test_cases_files?: T[];
}

export interface AdminConfigGitFiles<T> extends GitFiles<T> {
  config_version: number;
}

export interface ReleaseGitFiles<T> extends GitFiles<T> {
  rules_version: number;
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
  admin_config_directory_url?: string;
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
  isReleased?: boolean;
  isNew: boolean;
  configData: ConfigData;
  savedInBackend: boolean;
  name: string;
  author: string;
  version: number;
  description: string;
  tags?: string[];
  fileHistory?: FileHistory[];
  testCases?: TestCaseWrapper[];
}

export interface AdminConfig {
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
  files: Content<ConfigData>[];
  test_specification: string;
}

export type ConfigData = any;

export interface Release {
  configs: Config[];
  releaseVersion: number;
}

export interface ConfigTestResult {
  exception?: string;
  message?: string;
  test_result_output?: string;
  test_result_complete?: boolean;
  test_result_raw_output?: any;
}

export interface ReleaseWrapper {
  storedRelease: Release;
  releaseHistory: FileHistory[];
}

export interface TestCaseResultAttributes {
  exception?: string;
  message?: string;
  test_case_result?: TestCaseEvaluationResult;
}

export interface UrlInfo {
  service?: string;
  mode?: string;
  configName?: string;
  testCaseName?: string;
}

export interface ConfigAndTestCases {
  configs: Config[];
  testCases?: TestCaseMap;
}

export interface Importers {
  config_importers: Importer[];
}

export interface Importer {
  importer_name: string;
  importer_attributes_schema: JSONSchema7;
}

export interface ConfigToImport {
  importer_name: string;
  importer_attributes: JSONSchema7;
  config_to_import: string;
}

export interface ImportedConfig {
  imported_configuration: any;
}

export interface applications {
  topologies: Application[];
}

export interface Application {
  topology_name: string,
  topology_id: string,
  attributes: string[],
  image: string,
  service_name: string
}

export const applicationManagerColumns = [
  {
    columnDef: 'service_name',
    header: 'Service Name',
    cell: (t: Application) => `${t.service_name}`,
  },
  {
    columnDef: 'application_name',
    header: 'Application Name',
    cell: (t: Application) => `${t.topology_name}`,
  },
  {
    columnDef: 'id',
    header: 'ID',
    cell: (t: Application) => `${t.topology_id}`,
  },
  {
    columnDef: 'image',
    header: 'Image',
    cell: (t: Application) => `${t.image}`,
  },
];

export const displayedApplicationManagerColumns = ["service_name", "application_name", "id", "image", "attributes", "restart"];

export interface ConfigAndTestsToClone {
  config: Config,
  test_cases: TestCaseWrapper[]
}

export class ExistingConfigError extends Error {
  constructor(message) {
    super(message);
    this.name = this.constructor.name;
  }
}

export interface ConfigManagerRow {
  config_name: string,
  author: string,
  version: number,
  releasedVersion: number,
  configHistory: FileHistory[],
  labels: string[],
  testCasesCount: number,
  status: ConfigStatus,
  isFiltered: boolean,
}

export enum ConfigStatus {
  UP_TO_DATE = "up-to-date",
  UPGRADABLE = "upgradable",
  UNRELEASED = "unreleased",
}

export interface CheckboxEvent {
  checked: boolean,
  groupName: string,
  checkboxName: string,
}

export const FILTER_DELIMITER = '|';

export interface SearchHistory {
  [type: string]: ServiceSearchHistory[];
}

export interface ServiceSearchHistory {
  [type: string]: string[] | string;
}