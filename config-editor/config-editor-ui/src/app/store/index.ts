import { RouterStateUrl } from '@app/app-routing';
import { ConfigWrapper } from '@app/model';
import * as fromRouter from '@ngrx/router-store';
import { Action, ActionReducer, ActionReducerMap, createSelector, MetaReducer } from '@ngrx/store';
import { environment } from 'environments/environment';
import { cloneDeep } from 'lodash';
import { storeFreeze } from 'ngrx-store-freeze';
import { Deployment } from '../model';
import { ConfigData } from '../model/config-model';
import * as fromEditor from './editor.reducer';

export * from './editor.actions'
export * from './router-actions'

export interface State {
    editor: fromEditor.State;
    routerReducer: fromRouter.RouterReducerState<RouterStateUrl>;
}

export const reducers: ActionReducerMap<State> = {
    editor: fromEditor.reducer,
    routerReducer: fromRouter.routerReducer,
};

export function logger(reducer: ActionReducer<State>): ActionReducer<State, Action> {
    return function (state: State, action: Action): State {
        console.log('state', state);
        console.log('action', action);

        const newState = reducer(state, action);
        console.log('newState', newState);

        return newState;
    };
}

export function sortByDeploymentOrder(configList: ConfigWrapper<ConfigData>[],
        deployment: ConfigWrapper<ConfigData>[]): ConfigWrapper<ConfigData>[] {
    let pos = 0;
    for (const r of deployment) {
        for (let i = pos; i < configList.length; ++i) {
            if (configList[i].name === r.name) {
                const tmp = configList[pos];
                configList[pos] = configList[i];
                configList[i] = tmp;
            }
        }
        ++pos;
    }

    return configList;
}

export const metaReducers: MetaReducer<State>[] = environment.production ? [] : [logger, storeFreeze];

export const getEditorState = (state: State) => state.editor;
export const getRouterState = (state: State) => state.routerReducer;

export const getConfigs = createSelector(getEditorState, fromEditor.getConfigs);
export const getSelectedConfig = createSelector(getEditorState, fromEditor.getSelectedConfig);
export const getSchema = createSelector(getEditorState, fromEditor.getSchema);
export const getLoaded = createSelector(getEditorState, fromEditor.getLoaded);
export const getBootstrapped = createSelector(getEditorState, fromEditor.getBootstrapped);
export const getLoading = createSelector(getEditorState, fromEditor.getLoading);
export const getSelectedView = createSelector(getEditorState, fromEditor.getSelectedView);
export const getErrorMessage = createSelector(getEditorState, fromEditor.getErrorMessage);
export const getStoredDeployment = createSelector(getEditorState, fromEditor.getStoredDeployment);
export const getCurrentUser = createSelector(getEditorState, fromEditor.getCurrentUser);
export const getPullRequestPending = createSelector(getEditorState, fromEditor.getPullRequestPending);
export const getSubmitReleaseStatus = createSelector(getEditorState, fromEditor.getSubmitReleaseStatus);
export const getConfigValidity = createSelector(getEditorState, fromEditor.getConfigValidity);
export const getServiceName = createSelector(getEditorState, fromEditor.getServiceName);
export const getRepositoryLinks = createSelector(getEditorState, fromEditor.getRepositoryLinks);
export const getSearchTerm = createSelector(getEditorState, fromEditor.getSearchTerm);
export const getConfigTestingEvent = createSelector(getEditorState, fromEditor.getConfigTestingEvent);
export const getFilterMyConfigs = createSelector(getEditorState, fromEditor.getFilterMyConfigs);
export const getFilterUndeployed = createSelector(getEditorState, fromEditor.getFilterUndeployed);
export const getFilterUpgradable = createSelector(getEditorState, fromEditor.getFilterUpgradable);
export const getServiceNames = createSelector(getEditorState, fromEditor.getServiceNames);
export const getDeploymentHistory = createSelector(getEditorState, fromEditor.getDeploymentHistory);
export const getDynamicFieldsMap = createSelector(getEditorState, fromEditor.getDynamicFieldsMap);
export const getTestCaseSchema = createSelector(getEditorState, fromEditor.getTestCaseSchema);
export const getTestSpecificationSchema = createSelector(getEditorState, fromEditor.getTestSpecificationSchema);
export const getTestCaseMap = createSelector(getEditorState, fromEditor.getTestCaseMap);

export const getDeploymentFilteredBySearchTerm = createSelector(
    [getStoredDeployment, getSearchTerm, getFilterMyConfigs, getCurrentUser, getFilterUndeployed, getFilterUpgradable],
    (deployment: Deployment<ConfigWrapper<ConfigData>>, searchTerm, isMyConfig, user, undeployed, upgradable) => {
        const dep: Deployment<ConfigWrapper<ConfigData>> = cloneDeep(deployment);

        if (undeployed) {
            return {configs: [], deploymentVersion: dep.deploymentVersion};
        }

        if (upgradable) {
            dep.configs = dep.configs.filter(d => d.versionFlag > 0);
        }

        if (isMyConfig) {
            dep.configs = deployment.configs.filter(r => r.author === user);
        }

        if (!(searchTerm === null || searchTerm === undefined || searchTerm === '')) {
            const lowerCaseSearchTerm = searchTerm.toLowerCase();
            dep.configs = dep.configs.filter(f => f.name.toLowerCase().includes(lowerCaseSearchTerm)
                        || f.author.toLowerCase().startsWith(lowerCaseSearchTerm)
                        || f.tags === undefined ? true : f.tags.join(' ').includes(lowerCaseSearchTerm));
        }

        return dep;
    }
)

export const getConfigsFilteredBySearchTerm = createSelector(
    [getConfigs, getSearchTerm, getDeploymentFilteredBySearchTerm,
        getFilterMyConfigs, getCurrentUser, getFilterUndeployed, getFilterUpgradable],

    (configSet: ConfigWrapper<ConfigData>[], searchTerm: string,
        deployment: Deployment<ConfigWrapper<ConfigData>>, isMyConfig, user, undeployed, upgradable) => {

        let configs: ConfigWrapper<ConfigData>[] = cloneDeep(configSet);

        if (undeployed) {
            configs = configs.filter(r => !r.isDeployed);
        }

        if (upgradable) {
            configs = configs.filter(r => r.versionFlag > 0);
        }

        if (isMyConfig) {
            configs = configs.filter(r => r.author === user);
        }

        if (searchTerm === null || searchTerm === undefined || searchTerm === '') {
            return sortByDeploymentOrder(configs, deployment.configs);
        }
        const lowerCaseSearchTerm = searchTerm.toLowerCase();

        // use startswith for author as we assume the user will enter the whole name here
        return sortByDeploymentOrder(configs.filter((r: ConfigWrapper<ConfigData>) =>
            r.name.toLowerCase().includes(lowerCaseSearchTerm)
                || r.author.toLowerCase().startsWith(lowerCaseSearchTerm)
                || r.tags === undefined ? true : r.tags.join(' ').includes(lowerCaseSearchTerm)), deployment.configs);
    }
)

