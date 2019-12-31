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
            this.editorService.getTestCaseSchema(),
            this.editorService.getLoader(action.payload).getTestSpecificationSchema(),
            ])
            .map(([[configSchema, configs, [deploymentHistory, storedDeployment]],
                currentUser, pullRequestPending, sensorFields, testCaseSchema, testSpecificationSchema]: any) => {
                    this.store.dispatch(new actions.LoadTestCases());

                    return new actions.BootstrapSuccess(
                        { configs, configSchema, currentUser, pullRequestPending,
                            storedDeployment, sensorFields, deploymentHistory, testCaseSchema, testSpecificationSchema })
                })
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
    loadTestCases$ = this.actions$.pipe(
        ofType<actions.LoadTestCases>(actions.LOAD_TEST_CASES),
        withLatestFrom(this.store.select(fromStore.getServiceName)),
        switchMap(([action, serviceName]) =>
            this.editorService.getLoader(serviceName).getTestCases()
                .map((result) => new actions.LoadTestCasesSuccess(result))
                .catch(error => this.errorHandler(
                    error, this.RELEASE_STATUS_FAILED_MESSAGE, of(new actions.LoadTestCasesFailure(error)))))
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
                    this.displayNotification(this.newSuccessMessage('release'));
                    this.store.dispatch(new actions.LoadPullRequestStatus());

                    return new actions.SubmitReleaseSuccess(result);
                })
                .catch(error =>
                    this.errorHandler(error, this.newFailureMessage('release'), of(new actions.SubmitReleaseFailure(error)))))
    );

    @Effect()
    submitNewConfig$  = this.actions$.pipe(
        ofType<actions.SubmitNewConfig>(actions.SUBMIT_NEW_CONFIG),
        withLatestFrom(this.store.select(fromStore.getServiceName)),
        switchMap(([action, serviceName]) =>
            this.editorService.getLoader(serviceName).submitNewConfig(action.payload)
                .map(result => {
                    this.displayNotification(this.newSuccessMessage('config'));

                    return new actions.SubmitNewConfigSuccess(
                        this.editorService.getLoader(serviceName).getConfigsFromFiles(result.attributes.files)
                    );
                })
                .catch(error => this.errorHandler(
                    error, this.newFailureMessage('config'), of(new actions.SubmitNewConfigFailure(error)))))
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
                    error, this.validationFaliedMessage('config'), of(new actions.ValidateConfigsFailure(error)))))
    );

    @Effect()
    validateTestcase$ = this.actions$.pipe(
        ofType<actions.ValidateTestcase>(actions.VALIDATE_TESTCASE),
        switchMap((action) =>
            this.editorService.validateTestCase(action.payload)
                .map(result => new actions.ValidateTestcaseSuccess(result))
                .catch(error => this.errorHandler(
                    error, this.validationFaliedMessage('testcase'), of(new actions.ValidateTestcaseFailure(error)))))
    );

    @Effect()
    submitRuleEdit$ = this.actions$.pipe(
        ofType<actions.SubmitConfigEdit>(actions.SUBMIT_CONFIG_EDIT),
        withLatestFrom(this.store.select(fromStore.getServiceName)),
        switchMap(([action, serviceName]) =>
            this.editorService.getLoader(serviceName).submitConfigEdit(action.payload)
                .map(result => {
                    this.displayNotification(this.editSuccessMessage('config'));

                    return new actions.SubmitConfigEditSuccess(
                        this.editorService.getLoader(serviceName).getConfigsFromFiles(result.attributes.files)
                    )
                })
                .catch(error => this.errorHandler(
                    error, this.editFailureMessage('config'), of(new actions.SubmitConfigEditFailure(error)))))
    );

    @Effect()
    submitNewTestCase$  = this.actions$.pipe(
        ofType<actions.SubmitNewTestCase>(actions.SUBMIT_NEW_TESTCASE),
        withLatestFrom(this.store.select(fromStore.getServiceName)),
        switchMap(([action, serviceName]) =>
            this.editorService.getLoader(serviceName).submitNewTestCase(action.payload)
                .map(result => {
                    this.displayNotification(this.newSuccessMessage('testcase'));

                    return new actions.SubmitNewTestCaseSuccess(
                        this.editorService.getLoader(serviceName).getConfigsFromFiles(result.attributes.files)
                    );
                })
                .catch(error => this.errorHandler(
                    error, this.newFailureMessage('testcase'), of(new actions.SubmitNewTestCaseFailure(error)))))
    );

    @Effect()
    submitTestCaseEdit$ = this.actions$.pipe(
        ofType<actions.SubmitTestCaseEdit>(actions.SUBMIT_TESTCASE_EDIT),
        withLatestFrom(this.store.select(fromStore.getServiceName)),
        switchMap(([action, serviceName]) =>
            this.editorService.getLoader(serviceName).submitTestCaseEdit(action.payload)
                .map(result => {
                    this.displayNotification(this.editSuccessMessage('testcase'));

                    return new actions.SubmitTestCaseEditSuccess(
                        this.editorService.getLoader(serviceName).getConfigsFromFiles(result.attributes.files)
                    )
                })
                .catch(error => this.errorHandler(
                    error, this.editFailureMessage('testcase'), of(new actions.SubmitTestCaseEditFailure(error)))))
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

    private editSuccessMessage(fileType: string): string {
        return `Submitted ${fileType} edit successfully!`;
    }

    private newSuccessMessage(fileType: string): string {
        return `Submitted new ${fileType} successfully!`;
    }

    private newFailureMessage(fileType: string): string {
        return `Failed to submit new ${fileType} to github`;
    }

    private editFailureMessage(fileType: string): string {
        return `Failed to submit ${fileType} edit to github`;
    }

    private validationFaliedMessage(fileType: string): string {
        return `Failed to validate ${fileType} with backend`;
    }
}
