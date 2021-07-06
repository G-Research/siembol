import { ConfigHistoryService } from './config-history.service';

const state1 = { test: 'test1' };
const state2 = { test: 'test2' };
const state3 = { test: 'test3' };
const state4 = { test: 'test4' };

describe('ConfigHistoryService', () => {
  let service: ConfigHistoryService;

  beforeEach(() => {
    service = new ConfigHistoryService();
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should add to history', () => {
    service.addConfig(state1);
    expect(service.getCurrentConfig()).toEqual({ formState: state1 });
  });

  it('should undo twice then redo twice then undo thrice', () => {
    service.addConfig(state1);
    service.addConfig(state2);
    service.addConfig(state3);
    service.addConfig(state4);
    expect(service.getCurrentConfig()).toEqual({ formState: state4 });
    expect(service.undoConfig()).toEqual({ formState: state3 });
    expect(service.undoConfig()).toEqual({ formState: state2 });
    expect(service.redoConfig()).toEqual({ formState: state3 });
    expect(service.redoConfig()).toEqual({ formState: state4 });
    expect(service.isFutureEmpty()).toBeTrue();
    service.undoConfig();
    service.undoConfig();
    service.undoConfig();
    expect(service.getCurrentConfig()).toEqual({ formState: state1 });
    expect(service.isHistoryEmpty()).toBeTrue();
  });

  it('should throw error', () => {
    service.addConfig(state1);
    expect(() => service.undoConfig()).toThrow(new Error('Unable to undo: history is empty.'));
  });

  it('should not add same config twice', () => {
    service.addConfig(state1);
    service.addConfig(state1);
    service.addConfig(state1);
    expect(service.isHistoryEmpty()).toBeTrue();
  });
});
