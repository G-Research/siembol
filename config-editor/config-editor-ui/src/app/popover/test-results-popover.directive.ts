import { Directive, ElementRef, HostListener, Input, OnDestroy } from '@angular/core';
import { Subject, timer } from 'rxjs';
import { debounce, distinctUntilKeyChanged, takeUntil } from 'rxjs/operators';
import { TestResultsComponent } from '../components/testing/test-results/test-results.component';
import { TestCaseResult, TestCaseWrapper } from '../model/test-case';
import { PopoverRef } from './popover-ref';
import { PopoverService } from './popover-service';

@Directive({
    // tslint:disable-next-line:directive-selector
    selector: '[TestResultsPopover]',
})
export class TestResultsPopoverDirective implements OnDestroy {
    public hoverObserver$ = new Subject<any>();
    private ngUnsubscribe$ = new Subject<any>();

    popoverRef: PopoverRef<TestCaseResult>;

    // tslint:disable-next-line:no-input-rename
    @Input('TestResultsPopover') testCase: TestCaseWrapper;

    constructor(private el: ElementRef, private popover: PopoverService) {
        this.hoverObserver$.pipe(
            takeUntil(this.ngUnsubscribe$),
            debounce(() => timer(200)),
            distinctUntilKeyChanged('event')
        ).subscribe(event => {
            if (event.event === 'open') {
                this.popoverRef = this.popover.open({
                    origin: this.el.nativeElement,
                    content: TestResultsComponent,
                    data: this.testCase.testResult,
                });
            }
            if (event.event === 'close') {
                if (this.popoverRef !== undefined) {
                    this.popoverRef.close();
                }
            }
        });
    }

    @HostListener('mouseenter') onMouseEnter() {
        this.hoverObserver$.next({event: 'open'});
    }

    @HostListener('mouseleave') onMouseLeave() {
        this.hoverObserver$.next({event: 'close'});
    }

    ngOnDestroy() {
        this.ngUnsubscribe$.next();
    }
}
