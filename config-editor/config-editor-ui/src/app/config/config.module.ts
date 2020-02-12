import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { AppConfigService } from './app-config.service';

export function configFactory(config: AppConfigService) {
    return config.getConfig();
}

@NgModule({
    imports: [HttpClientModule],
    providers: [AppConfigService],
})
export class ConfigModule {
    constructor() {}
}
