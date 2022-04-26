import { SearchHistoryService } from './search-history.service';
import { AppConfigService } from '@app/services/app-config.service';
import { TestBed } from '@angular/core/testing';
import { convertToParamMap } from '@angular/router';

export class MockAuth {
  // eslint-disable-next-line no-unused-vars
  isCallbackSearch(s: string) {
    return false;
  }
}

describe('SearchHistoryService', () => {
  let service: SearchHistoryService;
  beforeEach(() => {
    const store = {};
    const mockLocalStorage = {
      getItem: (key: string): string => (key in store ? store[key] : undefined),
      setItem: (key: string, value: string) => {
        store[key] = `${value}`;
      },
    };

    TestBed.configureTestingModule({
      providers: [
        SearchHistoryService,
        {
          provide: AppConfigService,
          useValue: jasmine.createSpyObj(
            'AppConfigService',
            {
              environment: 'test',
              searchMaxSize: 5,
            }
          ),
        },
      ],
    });
    spyOn(localStorage, 'getItem').and.callFake(mockLocalStorage.getItem);
    spyOn(localStorage, 'setItem').and.callFake(mockLocalStorage.setItem);
    const appService = TestBed.inject(AppConfigService);
    service = new SearchHistoryService(appService, "myalerts");
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should have one search and ignore unknown param', () => {
    const history = service.addToSearchHistory(convertToParamMap({ filter: ["group1|param1", "group1|param2"], search: "test", hi: "hi"}));
    expect(history).toContain({ filter: ["group1|param1", "group1|param2"], search: "test" });
    expect(history).toHaveSize(1);
  });

  it('should ignore empty', () => {
    service.addToSearchHistory(convertToParamMap({ filter: [], search: "", hi: "hi"}));
    service.addToSearchHistory(convertToParamMap({ filter: [], search: "test", hi: "hi"}));
    const history = service.addToSearchHistory(convertToParamMap({ filter: ["param"], search: "", hi: "hi"}));
    expect(history).toContain({ search: "test" });
    expect(history).toContain({ filter: ["param"] });
    expect(history).toHaveSize(2);
  });

  it('should ignore duplicate params', () => {
    service.addToSearchHistory(convertToParamMap({ filter: ["group1|param1"]}));
    service.addToSearchHistory(convertToParamMap({ filter: ["group1|param1"]}));
    service.addToSearchHistory(convertToParamMap({ filter: "group1|param1"}));
    expect(service.getSearchHistory()).toContain({ filter: ["group1|param1"] });
    expect(service.getSearchHistory()).toHaveSize(1);
  });
});