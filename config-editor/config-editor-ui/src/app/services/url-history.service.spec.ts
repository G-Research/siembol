import { UrlHistoryService } from './url-history.service';
import { AppConfigService } from '@app/services/app-config.service';
import { TestBed, inject } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NavigationEnd, Router } from '@angular/router';
import { HistoryUrl } from '@app/model/config-model';

export class MockAuth {
  // eslint-disable-next-line no-unused-vars
  isCallbackUrl(s: string) {
    return false;
  }
}

describe('UrlHistoryService', () => {
  beforeEach(() => {
    const store = {};
    const mockLocalStorage = {
      getItem: (key: string): string => (key in store ? store[key] : undefined),
      setItem: (key: string, value: string) => {
        store[key] = `${value}`;
      },
    };

    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [
        UrlHistoryService,
        {
          provide: AppConfigService,
          useValue: jasmine.createSpyObj(
            'AppConfigService',
            { isHomePath: false, isNewConfig: false },
            {
              authenticationService: new MockAuth(),
              environment: 'test',
              historyMaxSize: 5,
            }
          ),
        },
      ],
    });
    spyOn(localStorage, 'getItem').and.callFake(mockLocalStorage.getItem);
    spyOn(localStorage, 'setItem').and.callFake(mockLocalStorage.setItem);
  });

  it('should create', inject([UrlHistoryService], (service: UrlHistoryService) => {
    expect(service).toBeTruthy();
  }));

  it('should have one url', inject([UrlHistoryService], (service: UrlHistoryService) => {
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url', 'url'));
    expect(service.getHistoryOfUrls()).toContain('url');
    expect(service.getHistoryOfUrls()).toHaveSize(1);
  }));

  it('should ignore duplicate urls', inject([UrlHistoryService], (service: UrlHistoryService) => {
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url', 'url'));
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url', 'url'));
    expect(service.getHistoryOfUrls()).toContain('url');
    expect(service.getHistoryOfUrls()).toHaveSize(1);
  }));

  it('should crop oldest', inject([UrlHistoryService], (service: UrlHistoryService) => {
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url', 'url'));
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url2', 'url'));
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url3', 'url'));
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url4', 'url'));
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url5', 'url'));
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url6', 'url'));
    expect(service.getHistoryOfUrls()).not.toContain('url');
    expect(service.getHistoryOfUrls()).toHaveSize(5);
  }));

  it('should parse history', inject([UrlHistoryService], (service: UrlHistoryService) => {
    TestBed.get(Router).events.next(new NavigationEnd(1, 'test-service?configName=test&testCaseName=testCase&random=test', 'url'));
    const parsedUrl: HistoryUrl = { 
      rawUrl: '/test-service?configName=test&testCaseName=testCase', 
      labels: { configName: "test", testCaseName: "testCase", service: "test-service", mode: ''},
    };
    expect(service.getParsedPreviousUrls()[0]).toEqual(parsedUrl);
    expect(service.getParsedPreviousUrls()).toHaveSize(1);
  }));
});

