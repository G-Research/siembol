import { TestBed } from '@angular/core/testing';
import { Type } from '@app/model/config-model';
import { BehaviorSubject, of } from 'rxjs';
import { mockStore } from 'testing/store';
import { ClipboardStoreService } from './clipboard-store.service';
import { ConfigLoaderService } from './config-loader.service';

describe('ClipboardService', () => {
  let service: ClipboardStoreService;
  let configLoader: ConfigLoaderService;
  beforeEach(() => {
    const store = new BehaviorSubject(mockStore);
    TestBed.configureTestingModule({
      providers: [
        {
          provide: ConfigLoaderService,
          useValue: { validateAdminConfig: () => of(undefined) },
        },
      ],
    });
    configLoader = TestBed.inject(ConfigLoaderService);
    service = new ClipboardStoreService(configLoader, store);
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should validate admin config', done => {
    spyOn(navigator.clipboard, 'readText').and.returnValue(Promise.resolve('{"name": "test"}'));
    const spy = spyOn(service, 'updatePastedConfig');
    service.validateConfig(Type.ADMIN_TYPE).subscribe(() => {
      expect(spy).toHaveBeenCalledOnceWith({ name: 'test' });
      done();
    });
  });

  it('should not be json', done => {
    spyOn(navigator.clipboard, 'readText').and.returnValue(Promise.resolve('test'));
    service.validateConfig(Type.ADMIN_TYPE).subscribe(
      () => {},
      error => {
        expect(error.message).toEqual('Clipboard is not JSON');
        done();
      }
    );
  });
});
