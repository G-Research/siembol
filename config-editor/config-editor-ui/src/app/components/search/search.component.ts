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
  @Output() readonly searchTermChange: EventEmitter<string> = new EventEmitter<string>();
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
}
