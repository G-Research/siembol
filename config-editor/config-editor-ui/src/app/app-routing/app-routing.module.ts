import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InitComponent } from '@app/components/init-component/init.component';
import { ViewResolver } from '@app/guards';

const appRoutes: Routes = [
  {
    component: InitComponent,
    path: '**',
    resolve: {
        message: ViewResolver,
    },
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
