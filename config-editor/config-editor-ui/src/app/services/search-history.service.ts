import { Injectable } from '@angular/core';
import { ParamMap, Params } from '@angular/router';
import { FILTER_DELIMITER, FILTER_PARAM_KEY, SEARCH_PARAM_KEY, ServiceSearchHistory } from '@app/model/config-model';
import { AppConfigService } from '@app/services/app-config.service';
import { isEqual } from 'lodash';

@Injectable({
  providedIn: 'root',
})
export class SearchHistoryService {
  private readonly maxSize: number;
  private readonly SEARCH_HISTORY_KEY: string;

  constructor(private appService: AppConfigService, serviceName: string) {
    this.SEARCH_HISTORY_KEY = 'siembol_search_history-' + serviceName + '-' + this.appService.environment;
    this.maxSize = this.appService.searchMaxSize;
  }

  addToSearchHistory(search: ParamMap): ServiceSearchHistory[] {
    let history = this.getSearchHistory();
    const parsedParams = this.parseParams(search);
    if (Object.keys(parsedParams).length > 0) {
      history.push(parsedParams);
      history = this.crop(this.removeOldestDuplicates(history));
      localStorage.setItem(this.SEARCH_HISTORY_KEY, JSON.stringify(history));
    }
    return history;
  }

  getSearchHistory(): ServiceSearchHistory[] {
    const history = localStorage.getItem(this.SEARCH_HISTORY_KEY);
    return history ? JSON.parse(history) : [];
  }

  private parseParams(params: ParamMap): Params {
    const result = {};
    for (const param of params.getAll(FILTER_PARAM_KEY)) {
      const [groupName, filterName] = param.split(FILTER_DELIMITER, 2);
      if (!result[groupName]) {
        result[groupName] = [];
      }
      result[groupName].push(filterName);
    }
    const search = params.get(SEARCH_PARAM_KEY);
    if (search && search !== '') {
      result[SEARCH_PARAM_KEY] = search;
    }
    return result;
  }
  
  private removeOldestDuplicates(history: ServiceSearchHistory[]): ServiceSearchHistory[] {
    return history.filter((value, index) => 
      index === history.map(obj => this.areParamsEqual(obj, value)).lastIndexOf(true)
    );
  }

  private areParamsEqual(obj1, obj2): boolean {
    if (Object.keys(obj1).length !== Object.keys(obj2).length) {
      return false;
    }
    return Object.keys(obj1).every(key =>  {
      let value1 = obj1[key];
      let value2 = obj2[key];
      if (!Array.isArray(value1)) {
        value1 = [value1];
      }
      if (!Array.isArray(value2)) {
        value2 = [value2];
      }
      return isEqual(value1, value2);
    })
  }

  private crop(history: ServiceSearchHistory[]): ServiceSearchHistory[] {
    while (history.length > this.maxSize) {
      history.shift();
    }
    return history;
  }
}