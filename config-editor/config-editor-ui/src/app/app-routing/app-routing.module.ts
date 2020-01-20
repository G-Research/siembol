import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InitComponent } from '@app/components/init-component/init.component';
import { TestCaseHelpComponent } from '@app/components/testing/test-case-help/test-case-help.component';
import { ViewResolver } from '@app/guards';

const appRoutes: Routes = [
    {
        component: TestCaseHelpComponent,
        path: 'help/testcase',
    },
    {
        component: InitComponent,
        path: '**',
    },
];

@NgModule({
  exports: [RouterModule],
  imports: [RouterModule.forRoot(appRoutes, { useHash: true })],
  providers: [ViewResolver],
})
export class AppRoutingModule {
    constructor() {}
}
