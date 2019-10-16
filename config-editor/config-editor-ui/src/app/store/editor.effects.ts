import { Injectable } from '@angular/core';
import { EditorService } from '@app/editor.service';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Action, Store } from '@ngrx/store';
import { PopupService } from 'app/popup.service';
import * as fromStore from 'app/store';
import { forkJoin, Observable, of } from 'rxjs';
import { withLatestFrom } from 'rxjs/internal/operators/withLatestFrom';
import { exhaustMap, filter, switchMap } from 'rxjs/operators';
import { RepositoryLinks } from '../model/config-model';
import * as actions from './editor.actions';

@Injectable({
    providedIn: 'root',
  })
export class EditorEffects {

    private readonly VALIDATION_FAILED_MESSAGE = 'Failed to validate config with backend';
    private readonly BOOTSTRAP_FAILED_MESSAGE = 'Failed to load all required data at app startup';
    private readonly REPOSITORY_LOAD_FAILED_MESSAGE = 'Failed to load repositories';
    private readonly RELEASE_STATUS_FAILED_MESSAGE = 'Failed to load release status';
    private readonly RELEASE_SUCCESS_MESSAGE = 'Submitted release successfully!';
    private readonly RELEASE_FAILED_MESSAGE = 'Failed to submit release to github';
    private readonly NEW_CONFIG_SUCCESS_MESSAGE = 'Submitted new config successfully!';
    private readonly NEW_CONFIG_FAILED_MESSAGE = 'Failed to submit new config to github';
    private readonly EDIT_CONFIG_SUCCESS_MESSAGE = 'Submitted config edit successfully!';
    private readonly EDIT_CONFIG_FAILED_MESSAGE = 'Failed to submit config edit to github';


    @Effect()
    bootstrap$: Observable<Action> = this.actions$.pipe(
        ofType<actions.Bootstrap>(actions.BOOTSTRAP),
        withLatestFrom(this.store.select(fromStore.getBootstrapped)),
        filter(([action, bootStrapped]) => bootStrapped !== action.payload),
        exhaustMap(([action, isBootStrapped]) => forkJoin([
            this.editorService.getLoader(action.payload).getSchema()
                .switchMap(schema => forkJoin([
                    of(schema),
                    this.editorService.getLoader(action.payload).getConfigs(),
                    this.editorService.getLoader(action.payload).getRelease()
                        .map(result => [result.deploymentHistory, result.storedDeployment]),
                    ])),
            this.editorService.getUser(),
            this.editorService.getLoader(action.payload).getPullRequestStatus(),
            this.editorService.getSensorFields(),
            ])
            .map(([[configSchema, configs, [deploymentHistory, storedDeployment]], currentUser, pullRequestPending, sensorFields]: any) =>
                new actions.BootstrapSuccess(
                    { configs, configSchema, currentUser, pullRequestPending, storedDeployment, sensorFields, deploymentHistory }))
            .catch(error =>
                this.errorHandler(error, this.BOOTSTRAP_FAILED_MESSAGE, of(new fromStore.BootstrapFailure(error)))))
    );

    @Effect()
    loadRepositories$: Observable<Action> = this.actions$.pipe(
        ofType<actions.LoadRepositories>(actions.LOAD_REPOSITORIES),
        withLatestFrom(this.store.select(fromStore.getServiceNames)),
        exhaustMap(([action, serviceNames]) => {
            return forkJoin(serviceNames.
                map(serviceName => this.editorService.getLoader(serviceName).getRepositoryLinks())
            )
            .map((result: RepositoryLinks[]) => new actions.LoadRepositoriesSuccess(result))
                .catch(error => this.errorHandler(
                    error, this.REPOSITORY_LOAD_FAILED_MESSAGE, of(new actions.LoadRepositoriesFailure(error))
                )
            )}
        )
    );

    @Effect()
    loadPullRequestStatus$ = this.actions$.pipe(
        ofType<actions.LoadPullRequestStatus>(actions.LOAD_PULL_REQUEST_STATUS),
        withLatestFrom(this.store.select(fromStore.getServiceName)),
        switchMap(([action, serviceName]) =>
            this.editorService.getLoader(serviceName).getPullRequestStatus()
                .map((result) => new actions.LoadPullRequestStatusSuccess(result))
                .catch(error => this.errorHandler(
                    error, this.RELEASE_STATUS_FAILED_MESSAGE, of(new actions.LoadPullRequestStatusFailure(error)))))
    );

    @Effect()
    addConfig$: Observable<Action> = this.actions$.pipe(
        ofType<actions.AddConfig>(actions.ADD_CONFIG),
        withLatestFrom(this.store.select(fromStore.getServiceName), this.store.select(fromStore.getConfigs)),
        exhaustMap(([action, serviceName, configs]) =>
            of(new fromStore.Go({
                path: [serviceName, 'edit', configs.length - 1],
            })))
    );

    @Effect()
    submitRelease$ = this.actions$.pipe(
        ofType<actions.SubmitRelease>(actions.SUBMIT_RELEASE),
        withLatestFrom(this.store.select(fromStore.getServiceName)),
        switchMap(([action, serviceName]) =>
            this.editorService.getLoader(serviceName).submitRelease(action.payload)
                .map(result => {
                    this.displayNotification(this.RELEASE_SUCCESS_MESSAGE);
                    this.store.dispatch(new actions.LoadPullRequestStatus());

                    return new actions.SubmitReleaseSuccess(result);
                })
                .catch(error =>
                    this.errorHandler(error, this.RELEASE_FAILED_MESSAGE, of(new actions.SubmitReleaseFailure(error)))))
    );

    @Effect()
    submitNewConfig$  = this.actions$.pipe(
        ofType<actions.SubmitNewConfig>(actions.SUBMIT_NEW_CONFIG),
        withLatestFrom(this.store.select(fromStore.getServiceName)),
        switchMap(([action, serviceName]) =>
            this.editorService.getLoader(serviceName).submitNewConfig(action.payload)
                .map(result => {
                    this.displayNotification(this.NEW_CONFIG_SUCCESS_MESSAGE);

                    return new actions.SubmitNewConfigSuccess(
                        this.editorService.getLoader(serviceName).getConfigsFromFiles(result.attributes.files)
                    );
                })
                .catch(error => this.errorHandler(
                    error, this.NEW_CONFIG_FAILED_MESSAGE, of(new actions.SubmitNewConfigFailure(error)))))
    );

    @Effect()
    validateConfig$ = this.actions$.pipe(
        ofType<actions.ValidateConfig>(actions.VALIDATE_CONFIG),
        withLatestFrom(this.store.select(fromStore.getServiceName)),
        switchMap(([action, serviceName]) =>
            this.editorService.getLoader(serviceName).validateConfig(action.payload)
                .map(result => new actions.ValidateConfigsSuccess(result))
                .catch(error => this.errorHandler(
                    error, this.VALIDATION_FAILED_MESSAGE, of(new actions.ValidateConfigsFailure(error)))))
    );

    @Effect()
    validateConfigs$ = this.actions$.pipe(
        ofType<actions.ValidateConfigs>(actions.VALIDATE_CONFIGS),
        withLatestFrom(this.store.select(fromStore.getStoredDeployment), this.store.select(fromStore.getServiceName)),
        switchMap(([action, deployment, serviceName]) =>
            this.editorService.getLoader(serviceName).validateRelease(action.payload)
                .map(result => new actions.ValidateConfigsSuccess(result))
                .catch(error => this.errorHandler(
                    error, this.VALIDATION_FAILED_MESSAGE, of(new actions.ValidateConfigsFailure(error)))))
    );

    @Effect()
    submitRuleEdit$ = this.actions$.pipe(
        ofType<actions.SubmitConfigEdit>(actions.SUBMIT_CONFIG_EDIT),
        withLatestFrom(this.store.select(fromStore.getServiceName)),
        switchMap(([action, serviceName]) =>
            this.editorService.getLoader(serviceName).submitConfigEdit(action.payload)
                .map(result => {
                    this.displayNotification(this.EDIT_CONFIG_SUCCESS_MESSAGE);

                    return new actions.SubmitConfigEditSuccess(
                        this.editorService.getLoader(serviceName).getConfigsFromFiles(result.attributes.files)
                    )
                })
                .catch(error => this.errorHandler(
                    error, this.EDIT_CONFIG_FAILED_MESSAGE, of(new actions.SubmitConfigEditFailure(error)))))
    );

    constructor(
        private actions$: Actions,
        private snackBar: PopupService,
        private editorService: EditorService,
        private store: Store<fromStore.State>) {}

    // Opens snackbar with a summary of the error, passes the error to an error details dialog
    public errorHandler(error: any, description: string, action$: Observable<Action>): Observable<Action> {
        this.snackBar.openSnackBar(error, description);

        return action$;
    }

    public displayNotification(description: string) {
        this.snackBar.openNotification(description);
    }
}
