import { BehaviorSubject, Observable } from 'rxjs';
import { ConfigStoreState } from '../../model/store-state';
import { ConfigLoaderService } from '../config-loader.service';
import { TestCaseWrapper, TestCaseResult, TestCaseMap, isNewTestCase, TestCase } from '../../model/test-case';
import { ConfigStoreStateBuilder } from './config-store-state.builder';
import { ConfigTestResult, TestingType, Type } from '../../model/config-model';
import { cloneDeep } from 'lodash';
import { ClipboardStoreService } from '../clipboard-store.service';
import { ConfigHistoryService } from '../config-history.service';

export class TestStoreService {
  testCaseHistoryService = new ConfigHistoryService();
  constructor(
    private user: string,
    private store: BehaviorSubject<ConfigStoreState>,
    private configLoaderService: ConfigLoaderService,
    private clipboardService: ClipboardStoreService
  ) {}

  setEditedTestCaseByName(testCaseName: string) {
    this.testCaseHistoryService.clear();
    const testCase = this.getTestCaseByName(testCaseName);
    this.updateEditedTestCase(testCase);
  }

  setEditedClonedTestCaseByName(testCaseName: string) {
    this.testCaseHistoryService.clear();
    const testCase = this.getTestCaseByName(testCaseName);
    testCase.fileHistory = null;
    testCase.testCaseResult = null;
    testCase.testCase.version = 0;
    testCase.testCase.test_case_name = `${testCase.testCase.test_case_name}_clone`;
    testCase.testCase.author = this.user;
    this.updateEditedTestCase(testCase);
  }

  setEditedTestCaseNew() {
    this.testCaseHistoryService.clear();
    const currentState = this.store.getValue();
    const testCase = {
      testCase: {
        assertions: [],
        author: this.user,
        config_name: currentState.editedConfig.name,
        test_case_name: `test_${currentState.editedConfig.testCases.length + 1}`,
        test_specification: {},
        version: 0,
      },
      testCaseResult: undefined,
    } as TestCaseWrapper;
    this.updateEditedTestCase(testCase);
  }

  setEditedPastedTestCaseNew() {
    const currentState = this.store.getValue();
    const testCase = this.clipboardService.configToBePasted;
    testCase.version = 0;
    testCase.author = this.user;
    testCase.test_case_name = `test_${currentState.editedConfig.testCases.length + 1}`;
    const testCaseWrapper = {
      fileHistory: null,
      testCaseResult: null,
      testCase: testCase,
    };
    this.updateEditedTestCase(testCaseWrapper);
  }

  setEditedPastedTestCase() {
    this.clipboardService.validateConfig(Type.TESTCASE_TYPE).subscribe(() => {
      const currentState = this.store.getValue();
      const testCase = this.clipboardService.configToBePasted;
      const editedTestCase = currentState.editedTestCase;
      const pastedTestCase = cloneDeep(editedTestCase);
      pastedTestCase.testCase = Object.assign({}, cloneDeep(testCase), {
        version: editedTestCase.testCase.version,
        author: editedTestCase.testCase.author,
        config_name: editedTestCase.testCase.config_name,
        test_case_name: editedTestCase.testCase.test_case_name,
      });
      this.testCaseHistoryService.addConfig(pastedTestCase);
      this.updateEditedTestCase(pastedTestCase);
    });
  }

  updateEditedTestCase(testCase: TestCaseWrapper) {
    const newState = new ConfigStoreStateBuilder(this.store.getValue()).editedTestCase(testCase).build();
    this.store.next(newState);
  }

  cancelEditingTestCase() {
    this.updateEditedTestCase(null);
  }

  submitEditedTestCase(): Observable<boolean> {
    const state = this.store.getValue();
    if (!state.editedConfig || !state.editedTestCase) {
      throw Error('empty edited config or test case');
    }

    const testCaseWrapper = state.editedTestCase;
    const editedConfig = state.editedConfig;

    return this.configLoaderService.submitTestCase(testCaseWrapper).map((testCaseMap: TestCaseMap) => {
      if (testCaseMap) {
        const currentState = this.store.getValue();
        if (editedConfig.name !== currentState.editedConfig.name) {
          throw Error('Changed edited config during test case submission');
        }

        const editedConfigTestCases = testCaseMap[editedConfig.name];
        const submittedTestCase = editedConfigTestCases.find(
          x => x.testCase.test_case_name === testCaseWrapper.testCase.test_case_name
        );

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
      throw Error('empty edited config or test case');
    }

    const runningTest = new ConfigStoreStateBuilder(this.store.getValue())
      .editedTestCaseResult({ isRunning: true })
      .build();
    this.store.next(runningTest);

    return this.configLoaderService
      .evaluateTestCase(state.editedConfig.configData, state.editedTestCase)
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
      );
  }

  runEditedConfigTestSuite() {
    const config = this.store.getValue().editedConfig;
    if (!config) {
      throw Error('empty edited config');
    }
    const testCases = config.testCases;
    for (const testCase of testCases) {
      testCase.testCaseResult = { isRunning: true };
      this.updateEditedConfigTestCases(testCases);

      this.configLoaderService.evaluateTestCase(config.configData, testCase).subscribe(
        (testCaseResult: TestCaseResult) => {
          testCase.testCaseResult = testCaseResult;
          this.updateEditedConfigTestCases(testCases);
        },
        err => {
          testCase.testCaseResult = { isRunning: false };
          this.updateEditedConfigTestCases(testCases);
          throw err;
        }
      );
    }
  }

  updateEditedConfigTestCases(testCases: TestCaseWrapper[]) {
    const config = this.store.getValue().editedConfig;
    if (!config) {
      throw Error('empty edited config');
    }

    config.testCases = cloneDeep(testCases);
    const newState = new ConfigStoreStateBuilder(this.store.getValue()).editedConfig(config).build();
    this.store.next(newState);
  }

  validateEditedTestCase(): Observable<any> {
    const state = this.store.getValue();
    const testCase = state.editedTestCase;
    if (
      isNewTestCase(testCase) &&
      state.editedConfig.testCases.find(x => x.testCase.test_case_name === testCase.testCase.test_case_name)
    ) {
      throw Error('Testcase names must be unique in a config');
    }

    return this.configLoaderService.validateTestCase(testCase.testCase);
  }

  test(testSpecification: any, type: TestingType): Observable<ConfigTestResult> {
    if (type === TestingType.CONFIG_TESTING) {
      return this.testEditedConfig(testSpecification);
    }
    return this.testDeployment(testSpecification);
  }

  testEditedConfig(testSpecification: any): Observable<ConfigTestResult> {
    const config = this.store.getValue().editedConfig;
    if (!config) {
      throw Error('empty edited config');
    }

    return this.configLoaderService.testSingleConfig(config.configData, testSpecification);
  }

  testDeployment(testSpecification: any): Observable<ConfigTestResult> {
    const deployment = this.store.getValue().deployment;
    return this.configLoaderService.testDeploymentConfig(deployment, testSpecification);
  }

  undoTestCase() {
    let nextState = this.testCaseHistoryService.undoConfig();
    this.updateEditedTestCase(nextState.formState);
  }

  redoTestCase() {
    let nextState = this.testCaseHistoryService.redoConfig();
    this.updateEditedTestCase(nextState.formState);
  }

  addToTestCaseHistory(config: any, tabIndex: number = 0) {
    this.testCaseHistoryService.addConfig(config, tabIndex);
  }

  clearTestCaseHistory() {
    this.testCaseHistoryService.clear();
  }

  private getTestCaseByName(testCaseName: string): TestCaseWrapper {
    const currentState = this.store.getValue();
    const testCase = currentState.editedConfig.testCases.find(
      x => x.testCase.test_case_name === testCaseName
    );
    if (testCase === undefined) {
      throw Error('no test case with such name');
    }

    return cloneDeep(testCase);
  }
}
