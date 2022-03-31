import { Injectable } from '@angular/core';
import { Params } from '@angular/router';
import { AppConfigService } from '@app/services/app-config.service';
import { isEqual } from 'lodash';

@Injectable({
  providedIn: 'root',
})
export class SearchHistoryService {
  private readonly max_size: number;
  private readonly SEARCH_HISTORY_KEY: string;

  constructor(private appService: AppConfigService) {
    this.SEARCH_HISTORY_KEY = 'siembol_search_history-' + this.appService.environment;
    this.max_size = this.appService.searchMaxSize;
  }

  getServiceSearchHistory(serviceName: string) {
    return this.getSearchHistory()[serviceName];
  }

  addToSearchHistory(search: any, serviceName: string) {
    const history = this.getSearchHistory();
    let serviceHistory = history[serviceName]? history[serviceName] : [];
    const paramsWithoutEmpty = this.removeEmptyParams(search.params);
    if (Object.keys(paramsWithoutEmpty).length > 0) {
      serviceHistory.push(paramsWithoutEmpty);
      serviceHistory = this.crop(this.removeOldestDuplicates(serviceHistory));
      history[serviceName] = serviceHistory;
      localStorage.setItem(this.SEARCH_HISTORY_KEY, JSON.stringify(history));
    }
  }

  private getSearchHistory() {
    const history = localStorage.getItem(this.SEARCH_HISTORY_KEY);
    return history ? JSON.parse(history) : {};
  }

  private removeEmptyParams(params: Params) {
    return Object.fromEntries(Object.entries(params).filter(([_, v]) => v.length > 0));
  }
  
  private removeOldestDuplicates(history: string[]): string[] {
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

  private crop(history: string[]): string[] {
    while (history.length > this.max_size) {
      history.shift();
    }
    return history;
  }
}