import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { RepositoryLinks } from '@app/model';
import * as fromStore from '@app/store';
import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  changeDetection: ChangeDetectionStrategy.Default,
  selector: 're-landing-page',
  styleUrls: ['./landing-page.component.scss'],
  templateUrl: './landing-page.component.html',
})
export class LandingPageComponent implements OnInit, OnDestroy {

  private ngUnsubscribe = new Subject();
  serviceNames$: Observable<string[]>;
  repositoryLinks: { [name: string]: RepositoryLinks } = {};

  constructor(private store: Store<fromStore.State>) { }

  ngOnInit(): void {
    this.serviceNames$ = this.store.select(fromStore.getServiceNames);
    this.store.select(fromStore.getRepositoryLinks).pipe(takeUntil(this.ngUnsubscribe)).subscribe(links => {
      if (links) {
        this.repositoryLinks = links.reduce((pre, cur) => ({ ...pre, [cur.rulesetName]: cur }), {});
      }
    });
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }
}
