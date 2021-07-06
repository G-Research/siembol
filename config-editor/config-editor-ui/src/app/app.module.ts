import { DragDropModule } from '@angular/cdk/drag-drop';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { LocationStrategy, PathLocationStrategy } from '@angular/common';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { APP_INITIALIZER, NgModule, ErrorHandler } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {
  ErrorDialogComponent,
  NavBarComponent,
  SideNavComponent,
  JsonViewerComponent,
  ConfigManagerComponent,
  DeployDialogComponent,
  SubmitDialogComponent,
  LandingPageComponent,
  SearchComponent,
} from '@app/components';
import { ConfigTileComponent } from '@app/components/tile/config-tile.component';
import { InputTypeComponent } from './ngx-formly/input.type.component';
import { DeploymentTileComponent } from '@app/components/tile/deployment-tile.component';
import { AppConfigService } from '@app/services/app-config.service';
import { HomeComponent, PageNotFoundComponent } from '@app/containers';
import { CredentialsInterceptor } from '@app/credentials-interceptor';
import { SharedModule } from '@app/shared';
import { FormlyModule } from '@ngx-formly/core';
import { FormlyMaterialModule } from '@ngx-formly/material';
import { NgxPopperModule } from 'ngx-popper';
import { NgxTextDiffModule } from 'ngx-text-diff';
import { environment } from 'environments/environment';
import { NgScrollbarModule } from 'ngx-scrollbar';
import { BlockUIModule } from 'ng-block-ui';
import { AppComponent } from './app.component';
import { ChangeHistoryComponent } from './components/change-history/change-history.component';
import { EditorComponent } from './components/editor/editor.component';
import { AdminComponent } from './components/admin/admin.component';
import { TestCaseHelpComponent } from './components/testing/test-case-help/test-case-help.component';
import { TestCaseEditorComponent } from './components/testing/test-case-editor/test-case-editor.component';
import { TestCentreComponent } from './components/testing/test-centre/test-centre.component';
import { TestResultsComponent } from './components/testing/test-results/test-results.component';
import { JsonTreeComponent } from './components/json-tree/json-tree.component';
import { ArrayTypeComponent } from './ngx-formly/array.type';
import { ExpansionPanelWrapperComponent } from './ngx-formly/expansion-panel-wrapper.component';
import { JsonObjectTypeComponent } from './ngx-formly/json-object.type.component';
import { UnionTypeComponent } from './ngx-formly/union.type.component';
import { NullTypeComponent } from './ngx-formly/null.type';
import { ObjectTypeComponent } from './ngx-formly/object.type.component';
import { PanelWrapperComponent } from './ngx-formly/panel-wrapper.component';
import { TabArrayTypeComponent } from './ngx-formly/tab-array.type.component';
import { TabsWrapperComponent } from './ngx-formly/tabs-wrapper.component';
import { TabsetTypeComponent } from './ngx-formly/tabset.type.component';
import { AdminTabTypeComponent } from './ngx-formly/admin-tabs.type.component';
import { TextAreaTypeComponent } from './ngx-formly/textarea.type.component';
import { HighlightVariablesPipe } from './pipes';
import { PopupService } from './services/popup.service';
import { BuildInfoDialogComponent } from './components/build-info-dialog/build-info-dialog.component';
import { ConfigTestingComponent } from './components/testing/config-testing/config-testing.component';

import { RouterModule } from '@angular/router';
import { EditorViewComponent } from './components/editor-view/editor-view.component';
import { HomeViewComponent } from './components/home-view/home-view.component';
import { AdminViewComponent } from './components/admin-view/admin-view.component';
import { AppInitGuard, AuthGuard } from './guards';
import { AppService } from './services/app.service';
import { AppInitComponent } from './components/app-init/app-init.component';
import { HttpErrorInterceptor } from './http-error-interceptor';
import { GlobalErrorHandler } from './error-handler';
import { HelpLinkWrapperComponent } from './ngx-formly/help-link.wrapper';
import { UrlHistoryService } from './services/url-history.service';
import { RawJsonDirective } from './ngx-formly/rawjson.accessor';
import { TestStatusBadgeComponent } from './components/testing/test-status-badge/test-status-badge.component';

export function configServiceFactory(config: AppConfigService) {
  return () => config.loadConfigAndMetadata();
}

export function buildInfoServiceFactory(config: AppConfigService) {
  return () => config.loadBuildInfo();
}

const PROD_PROVIDERS = [
  { deps: [AppConfigService], multi: true, provide: APP_INITIALIZER, useFactory: configServiceFactory },
  { deps: [AppConfigService], multi: true, provide: APP_INITIALIZER, useFactory: buildInfoServiceFactory },
  { multi: true, provide: HTTP_INTERCEPTORS, useClass: CredentialsInterceptor },
  { provide: ErrorHandler, useClass: GlobalErrorHandler },
  { multi: true, provide: HTTP_INTERCEPTORS, useClass: HttpErrorInterceptor },
];

const DEV_PROVIDERS = [...PROD_PROVIDERS];

@NgModule({
  bootstrap: [AppComponent],
  declarations: [
    RawJsonDirective,
    AppComponent,
    HomeComponent,
    PageNotFoundComponent,
    ErrorDialogComponent,
    EditorViewComponent,
    HomeViewComponent,
    AdminViewComponent,
    NavBarComponent,
    SideNavComponent,
    JsonViewerComponent,
    ConfigManagerComponent,
    DeployDialogComponent,
    SubmitDialogComponent,
    LandingPageComponent,
    SearchComponent,
    ConfigTileComponent,
    DeploymentTileComponent,
    EditorComponent,
    AdminComponent,
    ChangeHistoryComponent,
    ObjectTypeComponent,
    ArrayTypeComponent,
    InputTypeComponent,
    NullTypeComponent,
    PanelWrapperComponent,
    TabsWrapperComponent,
    TabsetTypeComponent,
    AdminTabTypeComponent,
    ExpansionPanelWrapperComponent,
    HelpLinkWrapperComponent,
    TextAreaTypeComponent,
    HighlightVariablesPipe,
    TestCentreComponent,
    TestCaseEditorComponent,
    ConfigTestingComponent,
    JsonObjectTypeComponent,
    TestResultsComponent,
    TestStatusBadgeComponent,
    TestCaseHelpComponent,
    JsonTreeComponent,
    UnionTypeComponent,
    TabArrayTypeComponent,
    BuildInfoDialogComponent,
  ],
  imports: [
    BrowserModule,
    BlockUIModule.forRoot(),
    RouterModule.forRoot(
      [
        {
          canActivate: [AuthGuard],
          children: [
            {
              canActivate: [AppInitGuard],
              component: AppInitComponent,
              path: '**',
            },
          ],
          path: '',
        },
      ],
      { useHash: false }
    ),
    BrowserAnimationsModule,
    HttpClientModule,
    SharedModule,
    DragDropModule,
    NgScrollbarModule,
    NgxTextDiffModule,
    ScrollingModule,
    FormlyModule.forRoot({
      extras: {
        checkExpressionOn: 'changeDetectionCheck',
        lazyRender: false,
      },
      types: [
        { component: InputTypeComponent, name: 'string', wrappers: ['form-field'] },
        { component: TextAreaTypeComponent, name: 'textarea', wrappers: ['form-field'] },
        { component: JsonObjectTypeComponent, name: 'rawobject' },
        {
          defaultOptions: {
            templateOptions: {
              type: 'number',
            },
          },
          extends: 'input',
          name: 'number',
          wrappers: ['form-field'],
        },
        {
          defaultOptions: {
            templateOptions: {
              type: 'number',
            },
          },
          extends: 'input',
          name: 'integer',
          wrappers: ['form-field'],
        },
        { extends: 'checkbox', name: 'boolean' },
        { extends: 'select', name: 'enum' },
        { component: NullTypeComponent, name: 'null', wrappers: ['form-field'] },
        { component: ArrayTypeComponent, name: 'array' },
        { component: ObjectTypeComponent, name: 'object' },
        { component: TabsetTypeComponent, name: 'tabs' },
        { component: AdminTabTypeComponent, name: 'admin-tabs' },
        { component: UnionTypeComponent, name: 'multischema' },
        { component: TabArrayTypeComponent, name: 'tab-array' },
      ],
      validationMessages: [
        { message: 'This field is required', name: 'required' },
        { message: 'should be null', name: 'null' },
        { message: 'Min length is', name: 'minlength' },
        { message: 'Max length is', name: 'maxlength' },
        { message: 'Min is', name: 'min' },
        { message: 'Max is', name: 'max' },
        { message: 'Min items required', name: 'minItems' },
        { message: 'Max items', name: 'maxItems' },
        { message: 'Json is not valid', name: 'invalidJson' },
      ],
      wrappers: [
        { component: PanelWrapperComponent, name: 'panel' },
        { component: ExpansionPanelWrapperComponent, name: 'expansion-panel' },
        { component: HelpLinkWrapperComponent, name: 'help-link' },
      ],
    }),
    ReactiveFormsModule,
    FormlyMaterialModule,
    NgxPopperModule.forRoot({}),
  ],
  providers: [
    environment.production ? PROD_PROVIDERS : DEV_PROVIDERS,
    PopupService,
    { provide: LocationStrategy, useClass: PathLocationStrategy },
    AppService,
    AppInitGuard,
    HighlightVariablesPipe,
    UrlHistoryService,
  ],
})
export class AppModule {}
