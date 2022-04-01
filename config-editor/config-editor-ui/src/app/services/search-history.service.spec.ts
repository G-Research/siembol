import { SearchHistoryService } from './search-history.service';
import { AppConfigService } from '@app/services/app-config.service';
import { TestBed, inject } from '@angular/core/testing';

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
    service = TestBed.inject(SearchHistoryService);
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should have one search', () => {
    service.addToSearchHistory({params: { test: ["param1", "param2"]}}, "myalerts");
    expect(service.getServiceSearchHistory("myalerts")).toContain({ test: [ 'param1', 'param2' ] });
    expect(service.getServiceSearchHistory("myalerts")).toHaveSize(1);
  });

  it('should ignore duplicate params', () => {
    service.addToSearchHistory({params: { test: ["param1"]}}, "myalerts");
    service.addToSearchHistory({params: { test: ["param1"]}}, "myalerts");
    service.addToSearchHistory({params: { test: "param1"}}, "myalerts");
    expect(service.getServiceSearchHistory("myalerts")).toContain({ test: 'param1' });
    expect(service.getServiceSearchHistory("myalerts")).toHaveSize(1);
  });

  // it('should crop oldest', () => {
  //   service.addToSearchHistory({params: { test: ["param2"]}}, "myalerts");
  //   service.addToSearchHistory({params: { test: ["param1"]}}, "myalerts");
  //   service.addToSearchHistory({params: { test4: "param3"}}, "myalerts");
  //   service.addToSearchHistory({params: { test: ["param1"], test2: ["param1", "param2"]}}, "myalerts");
  //   service.addToSearchHistory({params: { test: ["param1"], test2: ["param3", "param4"]}}, "myalerts");
  //   service.addToSearchHistory({params: { test3: "param1"}}, "myalerts");
  //   service.addToSearchHistory({params: { test3: "param2"}}, "myalerts");
  //   console.log((service as any).maxSize())
  //   // expect(service.getServiceSearchHistory("myalerts")).toContain({ test: 'param1' });
  //   expect(service.getServiceSearchHistory("myalerts")).toHaveSize(5);
  // });
});