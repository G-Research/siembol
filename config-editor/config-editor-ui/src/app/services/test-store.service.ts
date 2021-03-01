import { BehaviorSubject, Observable } from "rxjs";
import { ConfigStoreState } from "../model/store-state";
import { ConfigLoaderService } from "./config-loader.service";
import { TestCaseWrapper, TestCaseResult, TestCaseMap, isNewTestCase } from "../model/test-case";
import { ConfigStoreStateBuilder } from "./config-store-state.builder";
import { ConfigTestResult, TestingType } from "../model/config-model";
import { cloneDeep } from 'lodash';


export class TestStoreService {
    constructor(
        private user: string,
        private store: BehaviorSubject<ConfigStoreState>,
        private configLoaderService: ConfigLoaderService) {
    }

    private getTestCaseByName(testCaseName: string): TestCaseWrapper {
        const currentState = this.store.getValue();
        const testCase = currentState.editedConfig.testCases.find(x => x.testCase.test_case_name === testCaseName);
        if (testCase === undefined) {
            throw Error("no test case with such name");
        }

        return cloneDeep(testCase);
    }

    setEditedTestCaseByName(testCaseName: string) {
        const testCase = this.getTestCaseByName(testCaseName);
        this.updateEditedTestCase(testCase);
    }

    setEditedClonedTestCaseByName(testCaseName: string) {
        const testCase = this.getTestCaseByName(testCaseName);
        testCase.fileHistory = null;
        testCase.testCaseResult = null;
        testCase.testCase.version = 0;
        testCase.testCase.test_case_name = `${testCase.testCase.test_case_name}_clone`;
        testCase.testCase.author = this.user;
        this.updateEditedTestCase(testCase);
    }

    setEditedTestCaseNew() {
        const currentState = this.store.getValue();
        const testCase = {
            testCase: {
                version: 0,
                author: this.user,
                config_name: currentState.editedConfig.name,
                test_case_name: `test_${currentState.editedConfig.testCases.length + 1}`,
                test_specification: {},
                assertions: [],
            },
            testCaseResult: undefined,
        } as TestCaseWrapper;
        this.updateEditedTestCase(testCase);
    }

    updateEditedTestCase(testCase: TestCaseWrapper) {
        const newState = new ConfigStoreStateBuilder(this.store.getValue())
            .editedTestCase(testCase)
            .build();
        this.store.next(newState);

    }

    cancelEditingTestCase() {
        this.updateEditedTestCase(null);
    }

    submitEditedTestCase(): Observable<boolean> {
        const state = this.store.getValue();
        if (!state.editedConfig || !state.editedTestCase) {
            throw Error("empty edited config or test case");
        }

        const testCaseWrapper = state.editedTestCase;
        const editedConfig = state.editedConfig;

        return this.configLoaderService.submitTestCase(testCaseWrapper)
            .map((testCaseMap: TestCaseMap) => {
            if (testCaseMap) {
                const currentState = this.store.getValue();
                if (editedConfig.name !== currentState.editedConfig.name) {
                    throw Error("Changed edited config during test case submission");
                }

                const editedConfigTestCases = testCaseMap[editedConfig.name];
                const submittedTestCase = editedConfigTestCases
                    .find(x => x.testCase.test_case_name === testCaseWrapper.testCase.test_case_name);

                const newState = new ConfigStoreStateBuilder(currentState)
                    .testCaseMap(testCaseMap)
                    .updateTestCasesInConfigs()
                    .detectOutdatedConfigs()
                    .reorderConfigsByDeployment()
                    .computeFiltered(this.user)
                    .editedTestCase(submittedTestCase)
                    .editedTestCaseResult(testCaseWrapper.testCaseResult)
                    .editedConfigTestCases(editedConfigTestCases)
                    .build();
                this.store.next(newState);

                return true;
            }
        });
    }

    runEditedTestCase() {
        const state = this.store.getValue();
        if (!state.editedConfig || !state.editedTestCase) {
            throw Error("empty edited config or test case");
        }

        const runningTest = new ConfigStoreStateBuilder(this.store.getValue())
            .editedTestCaseResult({ isRunning: true })
            .build();
        this.store.next(runningTest);

        return this.configLoaderService.evaluateTestCase(state.editedConfig.configData, state.editedTestCase)
            .subscribe(
                (testCaseResult: TestCaseResult) => {
                    const newState = new ConfigStoreStateBuilder(this.store.getValue())
                        .editedTestCaseResult(testCaseResult)
                        .build();
                    this.store.next(newState);
                    },
                    err => {
                        const newState = new ConfigStoreStateBuilder(this.store.getValue())
                        .editedTestCaseResult({ isRunning: false })
                        .build();
                        this.store.next(newState);
                        throw err;
                    }
                )
    }

    runEditedConfigTestSuite() {
        const config = this.store.getValue().editedConfig;
        if (!config) {
            throw Error("empty edited config")
        }
        const testCases = config.testCases;
        for (let i = 0; i < testCases.length; i++) {
            testCases[i].testCaseResult = { isRunning: true };
            this.updateEditedConfigTestCases(testCases);

            this.configLoaderService.evaluateTestCase(config.configData, testCases[i])
                .subscribe((testCaseResult: TestCaseResult) => {
                    testCases[i].testCaseResult = testCaseResult;
                    this.updateEditedConfigTestCases(testCases);
                }, err => { 
                    testCases[i].testCaseResult = { isRunning: false };
                    this.updateEditedConfigTestCases(testCases);
                    throw err;
                })
        }
    }

    updateEditedConfigTestCases(testCases: TestCaseWrapper[]) {
        const config = this.store.getValue().editedConfig;
        if (!config) {
            throw Error("empty edited config")
        }

        config.testCases = cloneDeep(testCases);
        const newState = new ConfigStoreStateBuilder(this.store.getValue())
            .editedConfig(config)
            .build();
        this.store.next(newState);
    }

    validateEditedTestCase(): Observable<any> {
        const state = this.store.getValue();
        const testCase = state.editedTestCase;
        if (isNewTestCase(testCase) && state.editedConfig.testCases
            .find(x => x.testCase.test_case_name == testCase.testCase.test_case_name)) {
            throw Error('Testcase names must be unique in a config');
        }

        return this.configLoaderService.validateTestCase(testCase.testCase);
    }

    test(testSpecification: any, type: TestingType): Observable<ConfigTestResult> {
        if (type == TestingType.CONFIG_TESTING) {
            return this.testEditedConfig(testSpecification);
        }
        return this.testDeployment(testSpecification);
    }

    testEditedConfig(testSpecification: any): Observable<ConfigTestResult> {
        const config = this.store.getValue().editedConfig;
        if (!config) {
            throw Error("empty edited config")
        }

        return this.configLoaderService.testSingleConfig(config.configData, testSpecification);
    }

    testDeployment(testSpecification: any): Observable<ConfigTestResult> {
        const deployment = this.store.getValue().deployment;
        return this.configLoaderService.testDeploymentConfig(deployment, testSpecification);
    }
}