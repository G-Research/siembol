import { NgModule } from '@angular/core';

import * as router from './router-stubs';

const COMPONENTS = [
    router.RouterLinkActiveStubDirective,
    router.RouterLinkStubDirective,
    router.RouterOutletStubComponent,
];

@NgModule({
    declarations: COMPONENTS,
    exports: COMPONENTS,
})
export class RouterStubModule { }
