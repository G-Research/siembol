import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { RepositoryLinks } from '@app/model';
import * as fromStore from '@app/store';
import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ServiceInfo } from '../../model/config-model';
import { AppConfigService } from '../../config';

@Component({
  changeDetection: ChangeDetectionStrategy.Default,
  selector: 're-landing-page',
  styleUrls: ['./landing-page.component.scss'],
  templateUrl: './landing-page.component.html',
})
export class LandingPageComponent implements OnInit, OnDestroy {

  private ngUnsubscribe = new Subject();
  userServices$: Observable<ServiceInfo[]>;
  repositoryLinks: { [name: string]: RepositoryLinks } = {};

  constructor(
    private store: Store<fromStore.State>,
    private config: AppConfigService) { }

  ngOnInit(): void {
    this.userServices$ = Observable.of(this.config.getUserServices());
    this.store.select(fromStore.getRepositoryLinks).pipe(takeUntil(this.ngUnsubscribe)).subscribe(links => {
      if (links) {
        this.repositoryLinks = links.reduce((pre, cur) => ({ ...pre, [cur.service_name]: cur }), {});
      }
    });
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }
}
