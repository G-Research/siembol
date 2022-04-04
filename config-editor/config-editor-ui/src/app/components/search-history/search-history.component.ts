import { Component, EventEmitter, Input, Output } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { FILTER_DELIMITER, FILTER_PARAM_KEY, SEARCH_PARAM_KEY, ServiceSearchHistory } from "@app/model/config-model";

@Component({
  selector: "re-search-history",
  template: `
  <mat-expansion-panel *ngIf="searchHistory">
    <mat-expansion-panel-header class="title-header">Saved Searches</mat-expansion-panel-header>
    <mat-accordion multi="true">
      <ng-container
        class="expansion-panel-container"
        *ngFor="let search of searchHistory.slice().reverse()">
        <mat-expansion-panel [expanded]="false" (click)="routeTo(search)" [hideToggle]="true">
          <mat-expansion-panel-header #panelH (click)="panelH._toggle()">
            <div class="labels">
              <div *ngFor="let param of parseSearchHistory(search) | keyvalue">
                <div *ngIf="param.value" class="tag-chip">
                        <div class="tag-text">
                        {{param.key | titlecase}}: {{param.value}}
                        </div>   
                </div>
              </div>
            </div>
            <button mat-button matSuffix mat-icon-button aria-label="Delete" (click)="onDeleteSavedSearch(search)">
              <mat-icon style="font-size:20px;">close</mat-icon>
            </button>
          </mat-expansion-panel-header>
        </mat-expansion-panel>
      </ng-container>
        <p *ngIf="!searchHistory">
            No history to show
        </p>
    </mat-accordion>
  </mat-expansion-panel>
  `,
  styles: [`
    .labels {
      display:flex
    }
    .tag-chip {
      display: inline-block;
      margin: 0 auto;
      padding: 2px 5px;
      min-width: 40px;
      text-align: center;
      border-radius: 9999px;
      color: #eee;
      background: rgba(255, 255, 255, 0.035);
      font-family: monospace;
      margin-left: 10px;
  }
  
  .tag-text {
      padding-top: 2px;
      font-size: 10pt;
      text-overflow: ellipsis;
      overflow: hidden;
  }
  .title-header {
    font-weight: 600;
  }
  .mat-expansion-panel {
    margin-bottom: 10px;
  }
  ::ng-deep .mat-content {
    align-items: center;
    justify-content: space-between;
    display: flex;
  }
  `],
})
export class SearchHistoryComponent {
  @Input() searchHistory: ServiceSearchHistory[];
  @Output() readonly searchDeletion: EventEmitter<ServiceSearchHistory> = new EventEmitter<ServiceSearchHistory>();

  constructor(
    private router: Router,
    private route: ActivatedRoute
  ) {}
  
  routeTo(params: any) {
    this.router.navigate(
      [], 
      {
        relativeTo: this.route,
        queryParams: params,
      });
  }

  parseSearchHistory(searchHistory: ServiceSearchHistory): ServiceSearchHistory {
    const result = { [SEARCH_PARAM_KEY]: searchHistory[SEARCH_PARAM_KEY]};
    if (searchHistory[FILTER_PARAM_KEY]){
      for (const param of searchHistory[FILTER_PARAM_KEY]) {
        const [groupName, filterName] = param.split(FILTER_DELIMITER, 2);
        if (!result[groupName]) {
          result[groupName] = [];
        }
        result[groupName].push(filterName);
      }
    }
    return result;
  }

  onDeleteSavedSearch(search: ServiceSearchHistory) {
    this.searchDeletion.emit(search);
  }
}