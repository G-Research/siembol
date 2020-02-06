import { StatusCode } from '@app/commons';
import {
    ConfigData, ConfigWrapper, Deployment, EditorResult, ExceptionInfo,
    FileHistory, PullRequestInfo, RepositoryLinks, SchemaDto, SensorFields, SubmitStatus,
} from '@app/model';
import { cloneDeep } from 'lodash';
import { TestCaseMap } from '../model/test-case';
import * as editor from './editor.actions';

export interface State {
    bootstrapped: string;
    currentUser: string;
    configs: ConfigWrapper<ConfigData>[];
    storedDeployment: Deployment<ConfigWrapper<ConfigData>>;
    configSchema: SchemaDto;
    selectedConfig: number;
    errorMessage: string;
    loaded: boolean;
    loading: boolean;
    newForm: boolean;
    requestInflight: boolean;
    selectedView: string;
    pullRequestPending: PullRequestInfo;
    submitReleaseStatus: SubmitStatus;
    submitConfigStatus: SubmitStatus;
    configValidity: EditorResult<ExceptionInfo>;
    serviceName: string;
    repositoryLinks: RepositoryLinks[];
    searchTerm: string;
    sensorFields: SensorFields[];
    dataSource: string;
    configTestingEvent: string;
    filterMyConfigs: boolean;
    filterUndeployed: boolean;
    filterUpgradable: boolean;
    serviceNames: string[];
    deploymentHistory: FileHistory[];
    dynamicFieldsMap: Map<string, string>;
    testCaseSchema: any;
    testCaseMap: TestCaseMap;
    testSpecificationSchema: any;
}

export const initialState: State = {
    bootstrapped: undefined,
    currentUser: undefined,
    dataSource: undefined,
    errorMessage: undefined,
    loaded: false,
    loading: false,
    newForm: false,
    pullRequestPending: undefined,
    requestInflight: false,
    configValidity: undefined,
    configs: [],
    configSchema: undefined,
    searchTerm: undefined,
    selectedConfig: undefined,
    selectedView: undefined,
    storedDeployment: undefined,
    submitReleaseStatus: new SubmitStatus(),
    submitConfigStatus: new SubmitStatus(),
    serviceName: undefined,
    repositoryLinks: undefined,
    sensorFields: [],
    configTestingEvent: '',
    filterMyConfigs: false,
    filterUndeployed: false,
    filterUpgradable: false,
    serviceNames: [],
    deploymentHistory: [],
    dynamicFieldsMap: new Map<string, string>(),
    testCaseSchema: {},
    testCaseMap: undefined,
    testSpecificationSchema: undefined,
}

export function reducer(state = initialState, action: editor.Actions): State {
    switch (action.type) {

        case editor.BOOTSTRAP:
            return Object.assign({}, state, {
                serviceName: action.payload,
                loading: true,
            });

        case editor.SET_SERVICE_NAMES:
            return Object.assign({}, state, {
                serviceNames: action.payload,
            });

        case editor.BOOTSTRAP_SUCCESS:
            const data = cloneDeep(action.payload);
            detectOutdatedConfigs(data.storedDeployment.configs, data.configs);

            return Object.assign({}, state, data, {
                bootstrapped: state.serviceName,
                loading: false,
            });

        case editor.UPDATE_CONFIGS:
            return Object.assign({}, state, {
                configs: action.payload,
            });

        case editor.BOOTSTRAP_FAILURE:
            return Object.assign({}, state, {
                errorMessage: action.payload,
                loading: false,
                requestInflight: false,
            })

        case editor.SELECT_CONFIG:
            return Object.assign({}, state, {
                selectedConfig: action.payload,
            });

        case editor.ADD_CONFIG:
            return Object.assign({}, state, {
                configs: [...state.configs, action.payload],
            });

        case editor.UPDATE_DEPLOYMENT:
            const configsClone = cloneDeep(state.configs);
            const depClone: Deployment<ConfigWrapper<ConfigData>> = cloneDeep(action.payload);
            detectOutdatedConfigs(depClone.configs, configsClone);

            return Object.assign({}, state, {
                configs: configsClone,
                storedDeployment: depClone,
            });

        case editor.LOAD_PULL_REQUEST_STATUS:
            return Object.assign({}, state, {
                pullRequestPending: {
                    pull_request_pending: true,
                    pull_request_url: undefined,
                },
            });

        case editor.LOAD_PULL_REQUEST_STATUS_SUCCESS:
            return Object.assign({}, state, {
                pullRequestPending: action.payload,
            });

        case editor.LOAD_REPOSITORIES_SUCCESS:
            return Object.assign({}, state, {
                repositoryLinks: action.payload,
            });

        case editor.SUBMIT_NEW_CONFIG:
        case editor.SUBMIT_CONFIG_EDIT:
        case editor.SUBMIT_NEW_TESTCASE:
        case editor.SUBMIT_TESTCASE_EDIT:
            return Object.assign({}, state, {
                loading: true,
            });

        case editor.SUBMIT_CONFIG_EDIT_SUCCESS:
        case editor.SUBMIT_NEW_CONFIG_SUCCESS:
            const configs = cloneDeep(action.payload);
            const dep: Deployment<ConfigWrapper<ConfigData>> = cloneDeep(state.storedDeployment);
            detectOutdatedConfigs(dep.configs, configs);

            return Object.assign({}, state, {
                loading: false,
                configs: configs,
                storedDeployment: dep,
            });

        case editor.SUBMIT_TESTCASE_EDIT_SUCCESS:
        case editor.SUBMIT_NEW_TESTCASE_SUCCESS:
            return Object.assign({}, state, {
                loading: false,
                testCaseMap: action.payload,
            })

        case editor.SUBMIT_NEW_TESTCASE_FAILURE:
        case editor.SUBMIT_TESTCASE_EDIT_FAILURE:
        case editor.SUBMIT_NEW_CONFIG_FAILURE:
        case editor.SUBMIT_CONFIG_EDIT_FAILURE:
            return Object.assign({}, state, {
                loading: false,
            });

        case editor.SUBMIT_RELEASE:
            return Object.assign({}, state, {
                loading: true,
                pullRequestPending: {
                    pull_request_pending: true,
                    pull_request_url: undefined,
                },
                submitReleaseStatus: new SubmitStatus(true),
            });

        case editor.SUBMIT_RELEASE_SUCCESS:
            const statusSuccess: SubmitStatus = new SubmitStatus();
            switch (action.payload.status_code) {
                case StatusCode.CREATED:
                    statusSuccess.submitSuccess = true;
                    break;
                case StatusCode.BAD_REQUEST:
                case StatusCode.ERROR:
                    statusSuccess.statusCode = action.payload.status_code;
                    statusSuccess.message = action.payload.attributes.message;
            }
            statusSuccess.submitInFlight = false;

            // set pr status to true to prevent deploy button appearing before the pr action has completed
            return Object.assign({}, state, {
                loading: false,
                pullRequestPending: {
                    pull_request_pending: false,
                    pull_request_url: undefined,
                },
                submitReleaseStatus: statusSuccess,
            });

        case editor.VALIDATE_CONFIGS_SUCCESS:
            return Object.assign({}, state, {
                configValidity: action.payload,
            });

        case editor.VALIDATE_CONFIGS_FAILURE:
            return Object.assign({}, state, {
                errorMessage: action.payload,
                configValidity: {
                    attributes: {
                        message: action.payload,
                        exception: action.payload,
                    },
                    status_code: '500',
                },
            });

        case editor.SEARCH_CONFIG:
            return Object.assign({}, state, {
                searchTerm: action.payload,
            });

        case editor.SELECT_DATA_SOURCE:
            return Object.assign({}, state, {
                dataSource: action.payload,
            });

        case editor.LOAD_CENTRIFUGE_FIELDS_SUCCESS:
            const sensors: SensorFields[] = cloneDeep(state.sensorFields);
            sensors.push({fields: action.payload, sensor_name: state.serviceName});

            return Object.assign({}, state, {
                sensorFields: sensors,
            });

        case editor.LOAD_CENTRIFUGE_FIELDS_FAILURE:
            return Object.assign({}, state, {
                errorMessage: action.payload,
            });

        case editor.STORE_CONFIG_TESTING_EVENT:
            return Object.assign({}, state, {
                configTestingEvent: action.payload,
            });

        case editor.FILTER_MY_CONFIGS:
            return Object.assign({}, state, {
                filterMyConfigs: action.payload,
            });

        case editor.FILTER_UPGRADABLE:
            return Object.assign({}, state, {
                filterUpgradable: action.payload,
            });

        case editor.FILTER_UNDEPLOYED:
            return Object.assign({}, state, {
                filterUndeployed: action.payload,
            });

        case editor.UPDATE_DYNAMIC_FIELDS_MAP:
            return Object.assign({}, state, {
                dynamicFieldsMap: action.payload,
            });

        case editor.LOAD_TEST_CASES_SUCCESS:
            return Object.assign({}, state, {
                testCaseMap: action.payload,
            });

        case editor.LOAD_TEST_CASES_FAILURE:
            return Object.assign({}, state, {
                error: action.payload,
            });

        case editor.UPDATE_TEST_CASE_STATE:
            const testCaseMap: TestCaseMap = cloneDeep(state.testCaseMap);
            const index: number = testCaseMap[action.testCase.config_name].findIndex(
                i => i.testCase.test_case_name === action.testCase.test_case_name);
            testCaseMap[action.testCase.config_name][index].testState = action.testState;
            testCaseMap[action.testCase.config_name][index].testResult = action.testCaseResult;

            return Object.assign({}, state, {
                testCaseMap: testCaseMap,
            });

        case editor.UPDATE_ALL_TEST_CASE_STATE:
            const testCaseMap2: TestCaseMap = cloneDeep(state.testCaseMap);
            for (let i = 0; i < testCaseMap2[action.configName].length; ++i) {
                testCaseMap2[action.configName][i].testState = action.testState;
                testCaseMap2[action.configName][i].testResult = action.testCaseResult;
            }

            return Object.assign({}, state, {
                testCaseMap: testCaseMap2,
            });

        default:
            return state;
    }
}

export function detectOutdatedConfigs(deployment: ConfigWrapper<ConfigData>[], store: ConfigWrapper<ConfigData>[]) {
    store.forEach(config => {
        const matchingConfig = deployment.find(r => !r.isNew && r.name === config.name);
        if (matchingConfig) {
            config.isDeployed = true;
            matchingConfig.isDeployed = true;
            if (matchingConfig.version !== config.version) {
                config.versionFlag = config.version;
                matchingConfig.versionFlag = config.version;
            } else {
                config.versionFlag = -1;
                matchingConfig.versionFlag = -1;
            }
        } else {
            config.isDeployed = false;
            config.versionFlag = -1;
        }
    });
}

export const getConfigs = (state: State) => state.configs;
export const getSelectedConfig = (state: State) => state.selectedConfig;
export const getSchema = (state: State) => state.configSchema;
export const getLoaded = (state: State) => state.loaded;
export const getBootstrapped = (state: State) => state.bootstrapped;
export const getLoading = (state: State) => state.loading;
export const getSelectedView = (state: State) => state.selectedView;
export const getErrorMessage = (state: State) => state.errorMessage;
export const getCurrentUser = (state: State) => state.currentUser;
export const getNewForm = (state: State) => state.newForm;
export const getRequestInflight = (state: State) => state.requestInflight;
export const getStoredDeployment = (state: State) => state.storedDeployment;
export const getPullRequestPending = (state: State) => state.pullRequestPending;
export const getSubmitReleaseStatus = (state: State) => state.submitReleaseStatus;
export const getConfigValidity = (state: State) => state.configValidity;
export const getServiceName = (state: State) => state.serviceName;
export const getRepositoryLinks = (state: State) => state.repositoryLinks;
export const getSearchTerm = (state: State) => state.searchTerm;
export const getSensorFields = (state: State) => state.sensorFields;
export const getDataSource = (state: State) => state.dataSource;
export const getConfigTestingEvent = (state: State) => state.configTestingEvent;
export const getFilterMyConfigs = (state: State) => state.filterMyConfigs;
export const getFilterUndeployed = (state: State) => state.filterUndeployed;
export const getFilterUpgradable = (state: State) => state.filterUpgradable;
export const getServiceNames = (state: State) => state.serviceNames;
export const getDeploymentHistory = (state: State) => state.deploymentHistory;
export const getDynamicFieldsMap = (state: State) => state.dynamicFieldsMap;
export const getTestCaseSchema = (state: State) => state.testCaseSchema;
export const getTestSpecificationSchema = (state: State) => state.testSpecificationSchema;
export const getTestCaseMap = (state: State) => state.testCaseMap;
