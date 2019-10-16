import { BootstrapData, ConfigData, ConfigWrapper, Deployment, EditorResult,
    ExceptionInfo, PullRequestInfo, RepositoryLinks } from '@app/model';
import { Field } from '@app/model/sensor-fields';
import { State } from '@app/store';
import { RouterAction } from '@ngrx/router-store';
import { Action } from '@ngrx/store';

export const SET_SERVICE_NAMES = '[Configs] Set Service Names';
export const UPDATE_DEPLOYMENT = '[Configs] Update Deployment';
export const UPDATE_DEPLOYMENT_SUCCESS = '[Configs] Update Deployment Success';
export const UPDATE_DEPLOYMENT_FAILURE = '[Configs] Update Deployment Success';
export const UPDATE_CONFIGS = '[Configs] Update Configs';
export const SELECT_CONFIG = '[Configs] Select Config';
export const ADD_CONFIG = '[Configs] Add Config';
export const LOAD_REPOSITORIES = '[Configs] Load Repositories';
export const LOAD_REPOSITORIES_SUCCESS = '[Configs] Load Repositories Success';
export const LOAD_REPOSITORIES_FAILURE = '[Configs] Load Repositories Failure';
export const LOAD_PULL_REQUEST_STATUS = '[Configs] Load Pull Request Status'
export const LOAD_PULL_REQUEST_STATUS_SUCCESS = '[Configs] Load Pull Request Status Success';
export const LOAD_PULL_REQUEST_STATUS_FAILURE = '[Configs] Load Pull Request Status Failure';
export const VALIDATE_CONFIG = '[Configs] Validate Config';
export const VALIDATE_CONFIGS = '[Configs] Validate Configs';
export const VALIDATE_CONFIGS_SUCCESS = '[Configs] Validate Configs Success';
export const VALIDATE_CONFIGS_FAILURE = '[Configs] Validate Configs Failure';
export const SUBMIT_RELEASE = '[Configs] Submit Release';
export const SUBMIT_RELEASE_SUCCESS = '[Configs] Submit Release Success';
export const SUBMIT_RELEASE_FAILURE = '[Configs] Submit Release Failure';
export const SUBMIT_NEW_CONFIG = '[Configs] Submit Config';
export const SUBMIT_NEW_CONFIG_SUCCESS = '[Configs] Submit Config Success';
export const SUBMIT_NEW_CONFIG_FAILURE = '[Configs] Submit Config Failure';
export const SUBMIT_CONFIG_EDIT = '[Configs] Submit Config Edit';
export const SUBMIT_CONFIG_EDIT_SUCCESS = '[Configs] Submit Config Edit Success';
export const SUBMIT_CONFIG_EDIT_FAILURE = '[Configs] Submit Config Edit Failure';
export const BOOTSTRAP = '[Configs] Bootstrap';
export const BOOTSTRAP_SUCCESS = '[Configs] Bootstrap Success';
export const BOOTSTRAP_FAILURE = '[Configs] Bootstrap Failure';
export const SEARCH_CONFIG = '[Configs] Search Config';
export const SELECT_DATA_SOURCE = '[Configs] Select Data Source';
export const LOAD_CENTRIFUGE_FIELDS = '[Configs] Load Centrifuge Fields';
export const LOAD_CENTRIFUGE_FIELDS_SUCCESS = '[Configs] Load Centrifuge Fields Success';
export const LOAD_CENTRIFUGE_FIELDS_FAILURE = '[Configs] Load Centrifuge Fields Failure';
export const STORE_CONFIG_TESTING_EVENT = '[Config Testing] Store Config Testing Event';
export const FILTER_MY_CONFIGS = '[Search] Filter My Configs';
export const FILTER_UNDEPLOYED = '[Search] Filter Undeployed';
export const FILTER_UPGRADABLE = '[Search] Filter Upgradable';
export const UPDATE_DYNAMIC_FIELDS_MAP = '[schema] Update Dynamic Fields Map';

export class SetServiceNames implements Action {
    readonly type = SET_SERVICE_NAMES;
    constructor(public payload: string[]) { }
}

export class Bootstrap implements Action {
    readonly type = BOOTSTRAP;
    constructor(public payload: string) { }
}

export class BootstrapSuccess implements Action {
    readonly type = BOOTSTRAP_SUCCESS;
    constructor(public payload: BootstrapData) { }
}

export class BootstrapFailure implements Action {
    readonly type = BOOTSTRAP_FAILURE;
    constructor(public payload: any) { }
}

export class ValidateConfig implements Action {
    readonly type = VALIDATE_CONFIG;
    constructor(public payload: ConfigWrapper<ConfigData>) { }
}

export class ValidateConfigs implements Action {
    readonly type = VALIDATE_CONFIGS;
    constructor(public payload: Deployment<ConfigWrapper<ConfigData>>) { }
}

export class ValidateConfigsSuccess implements Action {
    readonly type = VALIDATE_CONFIGS_SUCCESS;
    constructor(public payload: EditorResult<ExceptionInfo>) { }
}

export class ValidateConfigsFailure implements Action {
    readonly type = VALIDATE_CONFIGS_FAILURE;
    constructor(public payload: any) { }
}

export class LoadRepositories implements Action {
    readonly type = LOAD_REPOSITORIES;
}

export class LoadRepositoriesSuccess implements Action {
    readonly type = LOAD_REPOSITORIES_SUCCESS;
    constructor(public payload: RepositoryLinks[]) { }
}

export class LoadRepositoriesFailure implements Action {
    readonly type = LOAD_REPOSITORIES_FAILURE;
    constructor(public payload: any) { }
}

export class LoadCentrifugeFields implements Action {
    readonly type = LOAD_CENTRIFUGE_FIELDS;
}

export class LoadCentrifugeFieldsSuccess implements Action {
    readonly type = LOAD_CENTRIFUGE_FIELDS_SUCCESS;
    constructor(public payload: Field[]) { }
}

export class LoadCentrifugeFieldsFailure implements Action {
    readonly type = LOAD_CENTRIFUGE_FIELDS_FAILURE;
    constructor(public payload: any) { }
}

export class LoadPullRequestStatus implements Action {
    readonly type = LOAD_PULL_REQUEST_STATUS;
}

export class LoadPullRequestStatusSuccess implements Action {
    readonly type = LOAD_PULL_REQUEST_STATUS_SUCCESS;
    constructor(public payload: PullRequestInfo) { }
}

export class LoadPullRequestStatusFailure implements Action {
    readonly type = LOAD_PULL_REQUEST_STATUS_FAILURE;
    constructor(public payload: any) { }
}

export class SubmitRelease implements Action {
    readonly type = SUBMIT_RELEASE;
    constructor(public payload: Deployment<ConfigWrapper<ConfigData>>) { }
}

export class SubmitReleaseSuccess implements Action {
    readonly type = SUBMIT_RELEASE_SUCCESS;
    constructor(public payload: EditorResult<ExceptionInfo>) { }
}

export class SubmitReleaseFailure implements Action {
    readonly type = SUBMIT_RELEASE_FAILURE;
    constructor(public payload: any) { }
}

export class SubmitNewConfig implements Action {
    readonly type = SUBMIT_NEW_CONFIG;
    constructor(public payload: ConfigWrapper<ConfigData>) { }
}

export class SubmitNewConfigSuccess implements Action {
    readonly type = SUBMIT_NEW_CONFIG_SUCCESS;
    constructor(public payload: ConfigWrapper<ConfigData>) { }
}

export class SubmitNewConfigFailure implements Action {
    readonly type = SUBMIT_NEW_CONFIG_FAILURE;
    constructor(public payload: any) { }
}

export class SubmitConfigEdit implements Action {
    readonly type = SUBMIT_CONFIG_EDIT;
    constructor(public payload: ConfigWrapper<ConfigData>) { }
}

export class SubmitConfigEditSuccess implements Action {
    readonly type = SUBMIT_CONFIG_EDIT_SUCCESS;
    constructor(public payload: EditorResult<ConfigData>) { }
}

export class SubmitConfigEditFailure implements Action {
    readonly type = SUBMIT_CONFIG_EDIT_FAILURE;
    constructor(public payload: any) { }
}

export class UpdateConfigs implements Action {
    readonly type = UPDATE_CONFIGS;
    constructor(public payload: ConfigWrapper<ConfigData>[]) { }
}

export class UpdateDeployment implements Action {
    readonly type = UPDATE_DEPLOYMENT;
    constructor(public payload: Deployment<ConfigWrapper<ConfigData>>) { }
}

export class SelectConfig implements Action {
    readonly type = SELECT_CONFIG;
    constructor(public payload: number) { }
}

export class AddConfig implements Action {
    readonly type = ADD_CONFIG;
    constructor(public payload: ConfigWrapper<ConfigData>) { }
}

export class SearchConfig implements Action {
    readonly type = SEARCH_CONFIG;
    constructor(public payload: string) { }
}

export class SelectDataSource implements Action {
    readonly type = SELECT_DATA_SOURCE;
    constructor(public payload: string) { }
}

export class StoreConfigTestingEvent implements Action {
    readonly type = STORE_CONFIG_TESTING_EVENT;
    constructor(public payload: string) { }
}

export class FilterMyConfigs implements Action {
    readonly type = FILTER_MY_CONFIGS;
    constructor(public payload: boolean) { }
}

export class FilterUndeployed implements Action {
    readonly type = FILTER_UNDEPLOYED;
    constructor(public payload: boolean) { }
}

export class FilterUpgradable implements Action {
    readonly type = FILTER_UPGRADABLE;
    constructor(public payload: boolean) { }
}

export class UpdateDynamicFieldsMap implements Action {
    readonly type = UPDATE_DYNAMIC_FIELDS_MAP;
    constructor(public payload: object) { }
}

export type Actions
    = RouterAction<State>
    | UpdateConfigs
    | SubmitRelease
    | SubmitReleaseSuccess
    | SubmitReleaseFailure
    | SubmitNewConfig
    | SubmitNewConfigSuccess
    | SubmitNewConfigFailure
    | SubmitConfigEdit
    | SubmitConfigEditSuccess
    | SubmitConfigEditFailure
    | SelectConfig
    | UpdateDeployment
    | AddConfig
    | LoadRepositories
    | LoadRepositoriesSuccess
    | LoadRepositoriesFailure
    | LoadPullRequestStatus
    | LoadPullRequestStatusSuccess
    | LoadPullRequestStatusFailure
    | LoadCentrifugeFields
    | LoadCentrifugeFieldsSuccess
    | LoadCentrifugeFieldsFailure
    | ValidateConfig
    | ValidateConfigs
    | ValidateConfigsSuccess
    | ValidateConfigsFailure
    | Bootstrap
    | BootstrapSuccess
    | BootstrapFailure
    | SearchConfig
    | SelectDataSource
    | StoreConfigTestingEvent
    | FilterMyConfigs
    | FilterUpgradable
    | FilterUndeployed
    | UpdateDynamicFieldsMap
    | SetServiceNames;
