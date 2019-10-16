import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-search',
  styleUrls: ['./search.component.scss'],
  templateUrl: './search.component.html',
})

export class SearchComponent implements OnInit {
  @ViewChild('searchBox', { static: true }) searchBox;
  @Input() searchTerm: string;
  @Input() filterMyConfigs: boolean;
  @Input() filterUndeployed: boolean;
  @Input() filterUpgradable: boolean;
  @Output() searchTermChange: EventEmitter<string> = new EventEmitter<string>();
  @Output() myConfigsChange: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() undeployedConfigsChange: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() updatedConfigsChange: EventEmitter<boolean> = new EventEmitter<boolean>();

  myConfigs = true;

  ngOnInit(): void {
    this.searchBox.nativeElement.focus();
  }

  public onSearch(searchTerm: string) {
    this.searchTermChange.emit(searchTerm);
  }

  public onClearSearch() {
    this.onSearch('');
    this.searchTerm = '';
  }

  public clickMyConfigs($event: boolean) {
      this.myConfigsChange.emit($event);
  }

  public clickNotDeployed($event: boolean) {
      this.undeployedConfigsChange.emit($event);
  }

  public clickUpdatedConfigs($event: boolean) {
      this.updatedConfigsChange.emit($event);
  }
}
