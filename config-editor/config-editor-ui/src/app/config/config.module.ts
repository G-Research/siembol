import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { AppConfigService } from './app-config.service';

@NgModule({
    imports: [HttpClientModule],
    providers: [AppConfigService],
})
export class ConfigModule {
    constructor() {}
}
