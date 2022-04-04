import { Injectable } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { AppConfigService } from '@app/services/app-config.service';
import { FILTER_PARAM_KEY, HistoryUrl, HISTORY_PARAMS, SEARCH_PARAM_KEY } from '@app/model/config-model';

@Injectable({
  providedIn: 'root',
})
export class UrlHistoryService {
  private readonly max_size: number;
  private readonly HISTORY_KEY: string;
  constructor(private router: Router, private appService: AppConfigService) {
    this.HISTORY_KEY = 'siembol_history-' + this.appService.environment;
    this.max_size = this.appService.historyMaxSize;
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        const history = this.add(event.url, this.getHistoryPreviousUrls());
        localStorage.setItem(this.HISTORY_KEY, JSON.stringify(history));
      }
    });
  }

  getHistoryPreviousUrls(): string[] {
    const history = localStorage.getItem(this.HISTORY_KEY);
    return history ? JSON.parse(history) : [];
  }

  getPreviousUrls(): HistoryUrl[] {
    const listUrls = this.getHistoryPreviousUrls();
    return listUrls.map(url => this.getHistoryUrl(url));
  }

  private getHistoryUrl(path: string): HistoryUrl {
    const url = new URL(path, location.origin);
    const paths = url.pathname.substring(1).split('/');
  
    const service = paths[0];
    const mode = paths[1] === 'admin' ? 'admin' : '';

    const newUrl = new URL(url.pathname, location.origin);
    for (const param of HISTORY_PARAMS) {
      if (url.searchParams.get(param)) {
        newUrl.searchParams.append(param, url.searchParams.get(param));
      }
    }
    const params = Object.fromEntries(newUrl.searchParams.entries());
    return {rawUrl: newUrl.pathname + newUrl.search, labels: { service, mode, ...params }};
  }

  private add(item: string, history: string[]): string[] {
    if (
      this.appService.isHomePath(item) ||
      this.appService.authenticationService.isCallbackUrl(item) ||
      this.appService.isNewConfig(item) ||
      this.isSearchQuery(item)
    ) {
      return history;
    }
    history.push(item);
    return this.crop(this.removeOldestDuplicates(history));
  }

  private removeOldestDuplicates(history: string[]): string[] {
    return history.filter((value, index) => history.lastIndexOf(value) === index);
  }

  private crop(history: string[]): string[] {
    while (history.length > this.max_size) {
      history.shift();
    }
    return history;
  }

  private isSearchQuery(path: string): boolean {
    const url = new URL(path, location.origin);
    if (url.searchParams.get(FILTER_PARAM_KEY) || url.searchParams.get(SEARCH_PARAM_KEY)) {
      return true;
    }
    return false;
  }
}
