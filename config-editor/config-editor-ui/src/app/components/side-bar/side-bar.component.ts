import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ConfigData, ConfigWrapper } from '@app/model';
import { Store } from '@ngrx/store';
import * as fromStore from 'app/store';
import { Observable } from 'rxjs';
import { take } from 'rxjs/operators';
import { AppConfigService } from '../../config';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-side-bar',
  styleUrls: ['./side-bar.component.scss', '../config-manager/config-manager.component.scss'],
  templateUrl: './side-bar.component.html',
})
export class SideBarComponent {
  public configs$: Observable<ConfigWrapper<ConfigData>[]>;
  public selectedConfig$: Observable<number>;
  private serviceName: string;

  constructor(public config: AppConfigService, private store: Store<fromStore.State>) {
    this.configs$ = this.store.select(fromStore.getConfigs);
    this.selectedConfig$ = this.store.select(fromStore.getSelectedConfig);
    this.store.select(fromStore.getServiceName).pipe(take(1)).subscribe(name => this.serviceName = name);
  }

  public onReturn() {
    this.store.dispatch(new fromStore.Go({
      path: [this.serviceName],
    }));
  }

  public onSelect(index: number) {
    this.store.dispatch(new fromStore.Go({
      path: [this.serviceName, 'edit', index],
    }));
  }
}
