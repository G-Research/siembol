import { inject, TestBed } from '@angular/core/testing';
import { Type } from '@app/model/config-model';
import { of } from 'rxjs';
import { ClipboardStoreService } from './clipboard-store.service';
import { ConfigLoaderService } from './config-loader.service';

fdescribe('ClipboardService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ClipboardStoreService,
        {
          provide: ConfigLoaderService,
          useValue: { validateAdminConfigJson: () => of(undefined) },
        },
      ],
    });
  });

  it('should create', inject([ClipboardStoreService], (service: ClipboardStoreService) => {
    expect(service).toBeTruthy();
  }));

  it('should validate admin config', inject([ClipboardStoreService], (service: ClipboardStoreService) => {
    spyOn(navigator.clipboard, 'readText').and.returnValue(Promise.resolve('{"name": "test"}'));
    service.validateConfig(Type.ADMIN_TYPE).subscribe(() => {
      expect(service.configToBePasted).toEqual({ name: 'test' });
    });
  }));

  it('should not be json', inject([ClipboardStoreService], (service: ClipboardStoreService) => {
    spyOn(navigator.clipboard, 'readText').and.returnValue(Promise.resolve('test'));
    service.validateConfig(Type.ADMIN_TYPE).subscribe(
      () => {},
      error => {
        expect(error.message).toEqual('Clipboard is not JSON');
      }
    );
  }));
});
