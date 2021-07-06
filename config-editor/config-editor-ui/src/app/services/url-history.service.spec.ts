import { UrlHistoryService } from './url-history.service';
import { AppConfigService } from '@app/services/app-config.service';
import { TestBed, inject } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NavigationEnd, Router } from '@angular/router';

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
    expect(service.getHistoryPreviousUrls()).toContain('url');
    expect(service.getHistoryPreviousUrls()).toHaveSize(1);
  }));

  it('should ignore duplicate urls', inject([UrlHistoryService], (service: UrlHistoryService) => {
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url', 'url'));
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url', 'url'));
    expect(service.getHistoryPreviousUrls()).toContain('url');
    expect(service.getHistoryPreviousUrls()).toHaveSize(1);
  }));

  it('should crop oldest', inject([UrlHistoryService], (service: UrlHistoryService) => {
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url', 'url'));
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url2', 'url'));
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url3', 'url'));
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url4', 'url'));
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url5', 'url'));
    TestBed.get(Router).events.next(new NavigationEnd(1, 'url6', 'url'));
    expect(service.getHistoryPreviousUrls()).not.toContain('url');
    expect(service.getHistoryPreviousUrls()).toHaveSize(5);
  }));
});
