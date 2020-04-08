import { NgModule, Optional, SkipSelf } from '@angular/core';
import { EditorService } from '@services/editor.service';

@NgModule({
  providers: [
    EditorService,
  ],
})
export class CoreModule {
  constructor( @Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error(
        'CoreModule is already loaded. Import it in the AppModule only ');
    }
  }
}
