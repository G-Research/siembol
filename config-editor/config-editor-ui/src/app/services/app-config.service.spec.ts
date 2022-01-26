import { HttpTestingController, HttpClientTestingModule } from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { AppConfigService } from "./app-config.service";

describe('AppConfigService', () => {
  // let httpTestingController: HttpTestingController;
  let service: AppConfigService;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AppConfigService],
    });
    // httpTestingController = TestBed.inject(HttpTestingController);
    service = TestBed.inject(AppConfigService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('isHomePath', () => {
    it('should be home path', () => {
      expect(service.isHomePath('/')).toBeTrue();
      expect(service.isHomePath('/home')).toBeTrue();
      expect(service.isHomePath('/home/management')).toBeTrue();
    })

    it('should not be home path', () => {
      expect(service.isHomePath('/test')).toBeFalse();
      expect(service.isHomePath('/homee')).toBeFalse();
      expect(service.isHomePath('/test/management')).toBeFalse();
    })
  })
});