import { DragDropModule } from '@angular/cdk/drag-drop';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { HashLocationStrategy, LocationStrategy } from '@angular/common';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { APP_INITIALIZER, NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { GestureConfig } from '@angular/material';
import { BrowserModule, HAMMER_GESTURE_CONFIG } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppRoutingModule, CustomRouterStateSerializer } from '@app/app-routing';
import {
  ConfigManagerComponent, DeployDialogComponent, EditorViewComponent,
  ErrorDialogComponent, JsonViewerComponent, LandingPageComponent, NavBarComponent, SearchComponent, SideBarComponent,
  SubmitDialogComponent
} from '@app/components';
import { TestingDialogComponent } from '@app/components/testing/testing-dialog/testing-dialog.component';
import { ConfigTileComponent } from '@app/components/tile/config-tile.component';
import { DeploymentTileComponent } from '@app/components/tile/deployment-tile.component';
import { AppConfigService, ConfigModule } from '@app/config';
import { HomeComponent, PageNotFoundComponent } from '@app/containers';
import { CoreModule } from '@app/core';
import { CredentialsInterceptor } from '@app/credentials-interceptor';
import { RepoResolver, ViewResolver } from '@app/guards';
import { StripSuffixPipe } from '@app/pipes';
import { SharedModule } from '@app/shared';
import { metaReducers, reducers } from '@app/store';
import { EditorEffects } from '@app/store/editor.effects';
import { RouterEffects } from '@app/store/router-effects';
import { EffectsModule } from '@ngrx/effects';
import { RouterStateSerializer, StoreRouterConnectingModule } from '@ngrx/router-store';
import { StoreModule } from '@ngrx/store';
import { FormlyModule } from '@ngx-formly/core';
import {FormlyMaterialModule} from '@ngx-formly/material';
import { environment } from 'environments/environment';
import 'hammerjs';
import { NgxMatSelectSearchModule } from 'ngx-mat-select-search'
import { NgScrollbarModule } from 'ngx-scrollbar';
import { AppComponent } from './app.component';
import { ChangeHistoryComponent } from './components/change-history/change-history.component';
import { EditorComponent } from './components/editor/editor.component';
import { InitComponent } from './components/init-component/init.component';
import { SubmitTestcaseDialogComponent } from './components/testing/submit-testcase-dialog/submit-testcase-dialog.component';
import { TestCaseHelpComponent } from './components/testing/test-case-help/test-case-help.component';
import { TestCaseComponent } from './components/testing/test-case/test-case.component';
import { TestCentreComponent } from './components/testing/test-centre/test-centre.component';
import { TestResultsComponent } from './components/testing/test-results/test-results.component';
import { ConfigStoreGuard } from './guards/config-store.guard';
import { ArrayTypeComponent } from './ngx-formly/components/array.type';
import { ExpansionPanelWrapperComponent } from './ngx-formly/components/expansion-panel-wrapper.component';
import { FormFieldWrapperComponent } from './ngx-formly/components/form-field-wrapper.component';
import { InputTypeComponent } from './ngx-formly/components/input.type.component';
import { NullTypeComponent } from './ngx-formly/components/null.type';
import { ObjectTypeComponent } from './ngx-formly/components/object.type.component';
import { PanelWrapperComponent } from './ngx-formly/components/panel-wrapper.component';
import { SelectTypeComponent } from './ngx-formly/components/select.type.component';
import { TabsWrapperComponent } from './ngx-formly/components/tabs-wrapper.component';
import { TabsetTypeComponent } from './ngx-formly/components/tabset.type.component';
import { TextAreaTypeComponent } from './ngx-formly/components/textarea.type.component';
import { JsonObjectTypeComponent } from './ngx-formly/json-object.type.component';
import { FormlySelectOptionsPipe } from './ngx-formly/pipe/select-options.pipe';
import { HighlightVariablesPipe } from './pipes';
import { FileHistoryPopoverDirective } from './popover/file-history-popover.directive';
import { HoverPopoverDirective } from './popover/hover-popover.directive';
import { PopoverRendererComponent } from './popover/popover-renderer.component';
import { PopoverService } from './popover/popover-service';
import { TestResultsPopoverDirective } from './popover/test-results-popover.directive';
import { PopupService } from './popup.service';
import { NgxTextDiffModule } from './text-diff/ngx-text-diff.module';

export function configServiceFactory(config: AppConfigService) {
  return () => config.loadConfig();
}

export function uiMetadataServiceFactory(config: AppConfigService) {
  return () => config.loadUiMetadata();
}

const PROD_PROVIDERS = [
    { provide: APP_INITIALIZER, useFactory: configServiceFactory, deps: [AppConfigService], multi: true },
    { provide: APP_INITIALIZER, useFactory: uiMetadataServiceFactory, deps: [AppConfigService], multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: CredentialsInterceptor, multi: true },
];

const DEV_PROVIDERS = [...PROD_PROVIDERS];

@NgModule({
  bootstrap: [AppComponent],
  declarations: [
    AppComponent,
    HomeComponent,
    PageNotFoundComponent,
    ErrorDialogComponent,
    SideBarComponent,
    EditorViewComponent,
    NavBarComponent,
    JsonViewerComponent,
    ConfigManagerComponent,
    StripSuffixPipe,
    DeployDialogComponent,
    SubmitDialogComponent,
    LandingPageComponent,
    SearchComponent,
    TestingDialogComponent,
    ConfigTileComponent,
    DeploymentTileComponent,
    InitComponent,
    EditorComponent,
    ChangeHistoryComponent,
    PopoverRendererComponent,
    FileHistoryPopoverDirective,
    ObjectTypeComponent,
    ArrayTypeComponent,
    NullTypeComponent,
    PanelWrapperComponent,
    TabsWrapperComponent,
    TabsetTypeComponent,
    ExpansionPanelWrapperComponent,
    TextAreaTypeComponent,
    HighlightVariablesPipe,
    InputTypeComponent,
    FormFieldWrapperComponent,
    TestCentreComponent,
    TestCaseComponent,
    JsonObjectTypeComponent,
    SubmitTestcaseDialogComponent,
    TestResultsComponent,
    TestResultsPopoverDirective,
    TestingDialogComponent,
    SelectTypeComponent,
    FormlySelectOptionsPipe,
    HoverPopoverDirective,
    TestCaseHelpComponent,
  ],
  entryComponents: [
    ErrorDialogComponent,
    JsonViewerComponent,
    DeployDialogComponent,
    SubmitDialogComponent,
    TestingDialogComponent,
    LandingPageComponent,
    HomeComponent,
    ConfigManagerComponent,
    EditorViewComponent,
    PageNotFoundComponent,
    EditorComponent,
    PopoverRendererComponent,
    ChangeHistoryComponent,
    TestCaseComponent,
    SubmitTestcaseDialogComponent,
    TestResultsComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    SharedModule,
    ConfigModule,
    CoreModule,
    AppRoutingModule,
    NgxMatSelectSearchModule,
    DragDropModule,
    NgScrollbarModule,
    NgxTextDiffModule,
    ScrollingModule,
    FormlyModule.forRoot({
        validationMessages: [
          { name: 'required', message: 'This field is required' },
          { name: 'null', message: 'should be null' },
          { name: 'minlength', message: 'Min length is' },
          { name: 'maxlength', message: 'Max length is' },
          { name: 'min', message: 'Min is' },
          { name: 'max', message: 'Max is' },
          { name: 'minItems', message: 'Min items required' },
          { name: 'maxItems', message: 'Max items' },
        ],
        types: [
          { name: 'string', component: InputTypeComponent, wrappers: ['form-field'] },
          { name: 'textarea', component: TextAreaTypeComponent, wrappers: [] },
          { name: 'rawobject', component: JsonObjectTypeComponent },
          {
            name: 'number',
            component: InputTypeComponent,
            wrappers: ['form-field'],
            defaultOptions: {
              templateOptions: {
                type: 'number',
              },
            },
          },
          {
            name: 'integer',
            component: InputTypeComponent,
            wrappers: ['form-field'],
            defaultOptions: {
              templateOptions: {
                type: 'number',
              },
            },
          },
          { name: 'boolean', extends: 'checkbox' },
          { name: 'enum', component: SelectTypeComponent, wrappers: ['form-field'] },
          { name: 'null', component: NullTypeComponent, wrappers: ['form-field'] },
          { name: 'array', component: ArrayTypeComponent },
          { name: 'object', component: ObjectTypeComponent },
          { name: 'tabs', component: TabsetTypeComponent},
        ],
        wrappers: [
            { name: 'panel', component: PanelWrapperComponent },
            { name: 'expansion-panel', component: ExpansionPanelWrapperComponent },
            { name: 'form-field', component: FormFieldWrapperComponent },
        ],
        extras: { checkExpressionOn: 'modelChange', immutable: true },
      }),
    ReactiveFormsModule,
    FormlyMaterialModule,

    // ngrx
    StoreModule.forRoot(reducers, { metaReducers }),
    EffectsModule.forRoot([EditorEffects, RouterEffects]),
    StoreRouterConnectingModule.forRoot(),
  ],
  providers: [
    environment.production ? PROD_PROVIDERS : DEV_PROVIDERS,
    PopupService,
    { provide: RouterStateSerializer, useClass: CustomRouterStateSerializer },
    { provide: LocationStrategy, useClass: HashLocationStrategy },
    { provide: HAMMER_GESTURE_CONFIG, useClass: GestureConfig },
    ViewResolver,
    RepoResolver,
    ConfigStoreGuard,
    HighlightVariablesPipe,
    StripSuffixPipe,
    PopoverService,
  ],

})
export class AppModule { }
