import { Directive, ElementRef, HostListener, Input, OnDestroy } from '@angular/core';
import { ChangeHistoryComponent } from '@app/components/change-history/change-history.component';
import { ConfigData, FileHistory } from '@app/model/config-model';
import { Subject, timer } from 'rxjs';
import { debounce, distinctUntilKeyChanged, takeUntil } from 'rxjs/operators';
import { ConfigWrapper } from '../model/config-model';
import { PopoverRef } from './popover-ref';
import { PopoverService } from './popover-service';

@Directive({
    // tslint:disable-next-line:directive-selector
    selector: '[hoverPopover]',
})
export class HoverPopoverDirective implements OnDestroy {
    public hoverObserver$ = new Subject<any>();
    private ngUnsubscribe$ = new Subject<any>();

    popoverRef: PopoverRef<FileHistory[]>;

    // tslint:disable-next-line:no-input-rename
    @Input('hoverPopover') config: ConfigWrapper<ConfigData>;

    constructor(private el: ElementRef, private popover: PopoverService) {
        this.hoverObserver$.pipe(
            takeUntil(this.ngUnsubscribe$),
            debounce(() => timer(200)),
            distinctUntilKeyChanged('event')
        ).subscribe(event => {
            if (event.event === 'open') {
                this.popoverRef = this.popover.open({
                    origin: this.el.nativeElement,
                    content: ChangeHistoryComponent,
                    data: this.config.fileHistory,
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
