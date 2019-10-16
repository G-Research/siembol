import { Component, Directive, EventEmitter, forwardRef, Input, Output } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-icon', template: '' })
export class MatIconStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-form-field', template: '' })
export class MatFormFieldStubComponent {
    @Input() color: any;
}

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-nav-list', template: '' })
export class MatNavListStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-card', template: '' })
export class MatCardStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-card-title', template: '' })
export class MatCardTitleStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-chip', template: '' })
export class MatChipStubComponent {
    @Input() color: any;
}

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-chip-list', template: '' })
export class MatChipListStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-progress-bar', template: '' })
export class MatProgressBarStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-card-content', template: '' })
export class MatCardContentStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-card-subtitle', template: '' })
export class MatCardSubtitleStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-spinner', template: '' })
export class MatSpinnerStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-option', template: '' })
export class MatOptionStubComponent {
    @Input() value: any;
}

@Component({
    providers: [
        {
            multi: true,
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => MatSelectStubComponent),
        },
    ],
    // tslint:disable-next-line:component-selector
    selector: 'mat-select',
    template: '',
})
export class MatSelectStubComponent implements ControlValueAccessor {
    writeValue(): void { }
    registerOnChange(): void { }
    registerOnTouched(): void { }
}

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-select-trigger', template: '' })
export class MatSelectTriggerStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-sidenav-container', template: '' })
export class MatSidenavContainerStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-sidenav', template: '' })
export class MatSidenavStubComponent {
    @Input() opened: any;
    @Output() onCloseStart = new EventEmitter<any>();
}

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-autocomplete', template: '', exportAs: 'matAutocomplete' })
export class MatAutocompleteStubComponent {
    @Input() displayWith: (any) => (any) => any;
}

// tslint:disable-next-line:directive-selector
@Directive({ selector: 'input[matAutocomplete]' })
export class MatAutocompleteStubDirective {
    @Input() matAutocomplete: any;
}

// tslint:disable-next-line:directive-selector
@Directive({ selector: 'button[matMenuTriggerFor]' })
export class MatMenuTriggerForStubDirective {
    @Input() matMenuTriggerFor: any;
}

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-menu', template: '', exportAs: 'matMenu' })
export class MatMenuStubComponent {
    @Input() overlapTrigger: any;
}

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-divider', template: '' })
export class MatDividerStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-error', template: '' })
export class MatErrorStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-list', template: '' })
export class MatListStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-list-item', template: '' })
export class MatListItemStubComponent { }

@Component({
    providers: [
        {
            multi: true,
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => MatCheckBoxStubComponent),
        },
    ],
    // tslint:disable-next-line:component-selector
    selector: 'mat-checkbox',
    template: '',
})
export class MatCheckBoxStubComponent implements ControlValueAccessor {
    @Input() disabled: any;

    writeValue(): void { }
    registerOnChange(): void { }
    registerOnTouched(): void { }
}

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-toolbar', template: '' })
export class MatToolbarStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-dialog-actions', template: '' })
export class MatDialogActionsStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-dialog-content', template: '' })
export class MatDialogContentStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-tab', template: '' })
export class MatTabStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-tab-group', template: '' })
export class MatTabGroupStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-table', template: '' })
export class MatTableStubComponent {
    @Input() dataSource: any;
}

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-header-cell', template: '' })
export class MatHeaderCellStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-cell', template: '' })
export class MatCellStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-header-row', template: '' })
export class MatHeaderRowStubComponent {
    @Input() cdkHeaderRowDef: any;
}

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-row', template: '' })
export class MatRowStubComponent {
    @Input() cdkRowDefColumns: any;
    @Input() cdkRowDef: any;
}

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-card-actions', template: '' })
export class MatCardActionsStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-button-toggle', template: '' })
export class MatButtonToggleStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-button-toggle-group', template: '', exportAs: 'matButtonToggleGroup' })
export class MatButtonToggleGroupStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-hint', template: ''  })
export class MatHintStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-list-option', template: ''  })
export class MatListOptionStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'mat-selection-list', template: ''  })
export class MatSelectionListStubComponent { }

// tslint:disable-next-line:component-selector
@Component({ selector: 'ngx-datatable', template: '' })
export class NgxDatatableStubComponent {
    @Input() rows: any[];
    @Input() columnMode: string;
    @Input() headerHeight: number;
    @Input() footerHeight: number;
    @Input() rowHeight: number;
    @Input() scrollbarV: boolean;
    @Input() scrollbarH: boolean;
    @Input() selectionType: string;
}

// tslint:disable-next-line:component-selector
@Component({ selector: 'ngx-datatable-column', template: '' })
export class NgxDatatableColumnStubComponent {
    @Input() name: string;
    @Input() flexGrow: number;
}
