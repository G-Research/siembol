import { ConnectionPositionPair, Overlay, OverlayConfig, PositionStrategy } from '@angular/cdk/overlay';
import { ComponentPortal, PortalInjector } from '@angular/cdk/portal';
import { Injectable, Injector } from '@angular/core';
import { PopoverRef } from './popover-ref';
import { PopoverRendererComponent } from './popover-renderer.component';

export interface PopoverParams<T> {
    origin: HTMLElement;
    content: any;
    data?: T;
    width?: string | number;
    height?: string | number;
}
 @Injectable()
export class PopoverService {
    constructor(private overlay: Overlay, private injector: Injector) {}

    open<T>({origin, content, data, width, height}: PopoverParams<T>): PopoverRef<T> {
        const overlayRef = this.overlay.create(this.getOverlayConfig({origin, width, height}));
        const popoverRef = new PopoverRef<T>(overlayRef, content, data);
        const injector = this.createInjector(popoverRef, this.injector);
        overlayRef.attach(new ComponentPortal(PopoverRendererComponent, null, injector));

        return popoverRef;
    }
     createInjector(popoverRef: PopoverRef, injector: Injector) {
        const tokens = new WeakMap([[PopoverRef, popoverRef]]);

        return new PortalInjector(injector, tokens);
    }
     private getOverlayConfig({origin, width, height}): OverlayConfig {
        return new OverlayConfig({
            width,
            height,
            hasBackdrop: false,
            backdropClass: 'cdk-overlay-dark-backdrop',
            positionStrategy: this.getOverlayPosition(origin),
            scrollStrategy: this.overlay.scrollStrategies.reposition(),
        });
    }
     private getOverlayPosition(origin: HTMLElement): PositionStrategy {
        const positionStrategy = this.overlay.position()
            .flexibleConnectedTo(origin)
            .withPositions(this.getPositions())
            .withPush(false);

        return positionStrategy;
    }
     private getPositions(): ConnectionPositionPair[] {
        return [
            {
                originX: 'start',
                originY: 'center',
                overlayX: 'end',
                overlayY: 'top',
            },
            {
                originX: 'start',
                originY: 'center',
                overlayX: 'end',
                overlayY: 'bottom',
            },
            {
                originX: 'end',
                originY: 'center',
                overlayX: 'start',
                overlayY: 'top',
            },
            {
                originX: 'end',
                originY: 'center',
                overlayX: 'start',
                overlayY: 'bottom',
            },
        ]
    }
}
