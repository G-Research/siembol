interface ConfigHistory {
  past: Array<ValidConfigState>;
  future: Array<ValidConfigState>;
}

interface ValidConfigState {
  tabIndex?: number;
  formState: any;
}

export class UndoRedoService {
  private history: ConfigHistory = { past: [], future: [] };

  clear() {
    this.history = { past: [], future: [] };
  }

  addState(config: any, tabIndex: number = undefined) {
    this.history.past.splice(0, 0, {
      formState: config,
      tabIndex: tabIndex,
    });
    this.history.future = [];
  }

  getCurrent(): ValidConfigState {
    if (this.history.past.length == 0) {
      return undefined;
    }
    return this.history.past[0];
  }

  undo(): ValidConfigState {
    if (this.isHistoryEmpty()) {
      return undefined;
    }
    this.history.future.splice(0, 0, this.getCurrent());
    this.history.past.shift();
    return this.getCurrent();
  }

  redo(): ValidConfigState {
    if (this.isFutureEmpty()) {
      return undefined;
    }
    let nextState = this.history.future[0];
    this.history.past.splice(0, 0, nextState);
    this.history.future.shift();
    return nextState;
  }

  isFutureEmpty(): boolean {
    return this.history.future.length == 0;
  }

  isHistoryEmpty(): boolean {
    return this.history.past.length < 2;
  }
}
