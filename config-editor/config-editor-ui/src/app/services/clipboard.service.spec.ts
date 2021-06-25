import { inject, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ClipboardService } from './clipboard.service';
import { ConfigLoaderService } from './config-loader.service';

fdescribe('ClipboardService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ClipboardService,
        {
          provide: ConfigLoaderService,
          useValue: { validateAdminConfigJson: () => of(undefined) },
        },
      ],
    });
  });

  it('should create', inject([ClipboardService], (service: ClipboardService) => {
    expect(service).toBeTruthy();
  }));

  it('should validate admin config', inject([ClipboardService], (service: ClipboardService) => {
    spyOn(navigator.clipboard, 'readText').and.returnValue(Promise.resolve('{"name": "test"}'));
    service.validateAdminConfig().subscribe(() => {
      expect(service.adminConfigToBePasted).toEqual({ name: 'test' });
    });
  }));

  it('should not be json', inject([ClipboardService], (service: ClipboardService) => {
    spyOn(navigator.clipboard, 'readText').and.returnValue(Promise.resolve('test'));
    service.validateAdminConfig().subscribe(
      () => {},
      error => {
        expect(error.message).toEqual('Clipboard is not JSON');
      }
    );
  }));
});
