import { Component, Directive, Input } from '@angular/core';

// tslint:disable-next-line:directive-selector
@Directive({ selector: '[routerLinkActive]' })
export class RouterLinkActiveStubDirective {
    @Input() routerLinkActive: any;
    @Input() routerLinkActiveOptions: any;
}

@Directive({
    // tslint:disable-next-line:directive-selector
    selector: '[routerLink]',
})
export class RouterLinkStubDirective {
    @Input() routerLink: any;
}

// tslint:disable-next-line:component-selector
@Component({ selector: 'router-outlet', template: '' })
export class RouterOutletStubComponent { }
