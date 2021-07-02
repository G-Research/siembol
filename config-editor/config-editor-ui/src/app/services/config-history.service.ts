import { areJsonEqual } from "@app/commons/helper-functions";

interface ConfigHistory {
  past: ValidConfigState[];
  future: ValidConfigState[];
}

interface ValidConfigState {
  formState: any;
}

export class ConfigHistoryService {
  private history: ConfigHistory = { past: [], future: [] };

  clear() {
    this.history = { past: [], future: [] };
  }

  addConfig(config: any) {
    if (!this.isConfigEqualToCurrent(config)) {
      this.history.past.splice(0, 0, {
        formState: config,
      });
      this.history.future = [];
    }
  }

  getCurrentConfig(): ValidConfigState {
    if (this.history.past.length === 0) {
      return undefined;
    }
    return this.history.past[0];
  }

  undoConfig(): ValidConfigState {
    if (this.isHistoryEmpty()) {
      throw Error('Unable to undo: history is empty.');
    }
    this.history.future.splice(0, 0, this.getCurrentConfig());
    this.history.past.shift();
    return this.getCurrentConfig();
  }

  redoConfig(): ValidConfigState {
    if (this.isFutureEmpty()) {
      throw new Error('Unable to redo: future is empty.');
    }
    const nextState = this.history.future[0];
    this.history.past.splice(0, 0, nextState);
    this.history.future.shift();
    return nextState;
  }

  isFutureEmpty(): boolean {
    return this.history.future.length === 0;
  }

  isHistoryEmpty(): boolean {
    return this.history.past.length < 2;
  }

  private isConfigEqualToCurrent(config: any): boolean {
    const current = this.getCurrentConfig();
    return current && areJsonEqual(current.formState, config);
  }
}
