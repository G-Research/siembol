import { Injectable } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { AppConfigService } from '@app/services/app-config.service';
import { parseUrl } from '@app/commons/helper-functions'
import { ParsedUrl } from '@app/model/config-model';

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

  getHistoryParsedPreviousUrls(): ParsedUrl[] {
    const listUrls = this.getHistoryPreviousUrls();
    return listUrls.map(url => ({ rawUrl: url, labels: parseUrl(url)}));
  }

  private add(item: string, history: string[]): string[] {
    if (
      this.appService.isHomePath(item) ||
      this.appService.authenticationService.isCallbackUrl(item) ||
      this.appService.isNewConfig(item)
    ) {
      return history;
    }
    history.push(this.trimSearchFromPath(item));
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

  private trimSearchFromPath(path: string): string {
    const url = new URL(path, location.origin);
    const paths = url.pathname.substring(1).split('/');
    if (paths.length === 1) {
      path = path.split("?", 2)[0];
    }
    return path;
  }
}
