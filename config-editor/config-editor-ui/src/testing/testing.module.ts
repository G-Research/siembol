import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { MaterialStubModule } from './material-stub';

@NgModule({
    exports: [
        NoopAnimationsModule,
        FormsModule,
        ReactiveFormsModule,
        MaterialStubModule,
    ],
})
export class TestingModule { }
