import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { ServiceInfo, RepositoryLinks } from '../../model/config-model';
import { AppService } from '../../services/app.service';
import { UserRole } from '@app/model/config-model';


@Component({
  changeDetection: ChangeDetectionStrategy.Default,
  selector: 're-landing-page',
  styleUrls: ['./landing-page.component.scss'],
  templateUrl: './landing-page.component.html',
})
export class LandingPageComponent implements OnInit {

  userServices: ServiceInfo[];
  repositoryLinks: { [name: string]: RepositoryLinks } = {};
  serviceAdmin = UserRole.SERVICE_ADMIN;
  serviceUser = UserRole.SERVICE_USER;

  constructor(
    private appService: AppService) { }

  ngOnInit(): void {
    this.userServices = this.appService.userServices;
    Observable.forkJoin(this.userServices.map(x => this.appService.getRepositoryLinks(x.name)))
      .subscribe((links: RepositoryLinks[]) => {
        if (links) {
          this.repositoryLinks = links.reduce((pre, cur) => ({ ...pre, [cur.service_name]: cur }), {});
        }
      });
  }
}
