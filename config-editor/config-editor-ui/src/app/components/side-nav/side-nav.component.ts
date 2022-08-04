import { Component, OnInit, ViewChild } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Observable } from 'rxjs';
import { AppService } from '../../services/app.service';
import { MatMenuTrigger } from '@angular/material/menu';
import {
    UserRole, RepositoryLinks, repoNames, ServiceInfo
} from '@app/model/config-model';
import { AppConfigService } from '@app/services/app-config.service';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-side-nav',
    styleUrls: ['./side-nav.component.scss'],
    templateUrl: './side-nav.component.html',
})
export class SideNavComponent implements OnInit {
    loading$: Observable<boolean>;
    userServices: ServiceInfo[];
    serviceAdmin = UserRole.SERVICE_ADMIN;
    serviceUser = UserRole.SERVICE_USER;
    repositoryLinks: { [name: string]: RepositoryLinks } = {};
    isMatMenuOpen = false;
    prevButtonTrigger;
    readonly adminPath = this.appConfig.adminPath;
    readonly repoNames = repoNames;
    
    @ViewChild(MatMenuTrigger) trigger: MatMenuTrigger;
    constructor(
        private appService: AppService, private appConfig: AppConfigService) {}

    ngOnInit() {
        this.userServices = this.appService.userServices;
        this.repositoryLinks = this.appService.repositoryLinks;
    }

    menuEnter() {
        this.isMatMenuOpen = true;
    } 
    
    menuLeave(trigger) {
        this.isMatMenuOpen = false;
        trigger.closeMenu();  
    } 

    // Note: hack so menu opens and previous is closed on hover 
    buttonEnter(trigger) {
        setTimeout(() => {
            if(this.prevButtonTrigger && this.prevButtonTrigger != trigger){
                this.prevButtonTrigger.closeMenu();
                this.prevButtonTrigger = trigger;
                this.isMatMenuOpen = false;
                trigger.openMenu();
            }
            else if (!this.isMatMenuOpen) {
                this.prevButtonTrigger = trigger
                trigger.openMenu();
            } else {
                this.prevButtonTrigger = trigger
            }
        })
    }

    buttonLeave(trigger) {
        setTimeout(() => {
            if (!this.isMatMenuOpen) {
                trigger.closeMenu();
            }
        }, 100)
    }
    
    getPath(service: string): string {
        let path = '/' + service;
        const roles = this.appService.getUserServiceRoles(service);
        if (roles.length < 2 && roles.includes(UserRole.SERVICE_ADMIN)) {
            path += this.adminPath;
        }
        return path;  
    }
}
