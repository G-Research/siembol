import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

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
  @Input() filterUnreleased: boolean;
  @Input() filterUpgradable: boolean;
  @Output() readonly searchTermChange: EventEmitter<string> = new EventEmitter<string>();
  @Output() readonly myConfigsChange: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() readonly unreleasedConfigsChange: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() readonly updatedConfigsChange: EventEmitter<boolean> = new EventEmitter<boolean>();
  debouncer: Subject<string> = new Subject<string>();

  myConfigs = true;

  ngOnInit(): void {
    this.searchBox.nativeElement.focus();
    this.debouncer
        .pipe(debounceTime(300), distinctUntilChanged())
        .subscribe(
          (searchTerm: string) => this.searchTermChange.emit(searchTerm)
        );
  }

  onSearch(searchTerm: string) {
    this.debouncer.next(searchTerm);
  }

  onClearSearch() {
    this.onSearch('');
    this.searchTerm = '';
  }

  clickMyConfigs($event: boolean) {
      this.myConfigsChange.emit($event);
  }

  clickNotReleased($event: boolean) {
      this.unreleasedConfigsChange.emit($event);
  }

  clickUpdatedConfigs($event: boolean) {
      this.updatedConfigsChange.emit($event);
  }
}
