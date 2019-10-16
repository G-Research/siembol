import { CdkScrollable, ScrollDispatcher } from '@angular/cdk/scrolling';
import {
    AfterViewInit,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    OnDestroy,
    OnInit,
    Output,
    QueryList,
    ViewChildren
} from '@angular/core';
import { Observable, Subscription } from 'rxjs';
import { ContainerDirective } from './ngx-text-diff-container.directive';
import { DiffContent, DiffPart, DiffResults, DiffTableFormat, DiffTableFormatOption, DiffTableRowResult } from './ngx-text-diff.model';
import { NgxTextDiffService } from './ngx-text-diff.service';

@Component({
    // tslint:disable-next-line:component-selector
    selector: 'td-ngx-text-diff',
    templateUrl: './ngx-text-diff.component.html',
    styleUrls: ['./ngx-text-diff.component.css'],
})
export class NgxTextDiffComponent implements OnInit, AfterViewInit, OnDestroy {
    private _hideMatchingLines = false;
    @ViewChildren(ContainerDirective) containers: QueryList<ContainerDirective>;
    @Input() format: DiffTableFormat = 'SideBySide';
    @Input() left = '';
    @Input() right = '';
    @Input() diffContent: Observable<DiffContent>;
    @Input() loading = false;
    @Input() showToolbar = true;
    @Input() showBtnToolbar = true;
    @Input()
    get hideMatchingLines() {
        return this._hideMatchingLines;
    }

    set hideMatchingLines(hide: boolean) {
        this.hideMatchingLinesChanged(hide);
    }
    @Input() outerContainerClass: string;
    @Input() outerContainerStyle: any;
    @Input() toolbarClass: string;
    @Input() toolbarStyle: any;
    @Input() compareRowsClass: string;
    @Input() compareRowsStyle: any;
    @Input() synchronizeScrolling = true;
    @Output() compareResults = new EventEmitter<DiffResults>();
    subscriptions: Subscription[] = [];
    tableRows: DiffTableRowResult[] = [];
    filteredTableRows: DiffTableRowResult[] = [];
    tableRowsLineByLine: DiffTableRowResult[] = [];
    filteredTableRowsLineByLine: DiffTableRowResult[] = [];
    diffsCount = 0;

    formatOptions: DiffTableFormatOption[] = [
        {
            id: 'side-by-side',
            name: 'side-by-side',
            label: 'Side by Side',
            value: 'SideBySide',
            icon: 'la-code',
        },
        {
            id: 'line-by-line',
            name: 'line-by-line',
            label: 'Line by Line',
            value: 'LineByLine',
            icon: 'la-file-text',
        },
    ];

    constructor(private scrollService: ScrollDispatcher, private diff: NgxTextDiffService, private cd: ChangeDetectorRef) { }

    ngOnInit() {
        this.loading = true;
        if (this.diffContent) {
            this.subscriptions.push(
                this.diffContent.subscribe(content => {
                    this.loading = true;
                    this.left = content.leftContent;
                    this.right = content.rightContent;
                    this.renderDiffs()
                        .then(() => {
                            this.cd.detectChanges();
                            this.loading = false;
                        })
                        .catch(() => (this.loading = false));
                })
            );
        }
        this.renderDiffs()
            .then(() => (this.loading = false))
            .catch(e => (this.loading = false));
    }

    ngAfterViewInit() {
        this.initScrollListener();
    }

    ngOnDestroy(): void {
        if (this.subscriptions) {
            this.subscriptions.forEach(subscription => subscription.unsubscribe());
        }
    }

    hideMatchingLinesChanged(value: boolean) {
        this._hideMatchingLines = value;
        if (this.hideMatchingLines) {
            this.filteredTableRows = this.tableRows.filter(
                row => (row.leftContent && row.leftContent.prefix === '-') || (row.rightContent && row.rightContent.prefix === '+')
            );
            this.filteredTableRowsLineByLine = this.tableRowsLineByLine.filter(
                row => (row.leftContent && row.leftContent.prefix === '-') || (row.rightContent && row.rightContent.prefix === '+')
            );
        } else {
            this.filteredTableRows = this.tableRows;
            this.filteredTableRowsLineByLine = this.tableRowsLineByLine;
        }
    }

    setDiffTableFormat(format: DiffTableFormat) {
        this.format = format;
    }

    async renderDiffs() {
        try {
            this.diffsCount = 0;
            this.tableRows = await this.diff.getDiffsByLines(this.left, this.right);
            this.tableRowsLineByLine = this.tableRows.reduce((tableLineByLine: DiffTableRowResult[], row: DiffTableRowResult) => {
                if (!tableLineByLine) {
                    tableLineByLine = [];
                }
                if (row.hasDiffs) {
                    if (row.leftContent) {
                        tableLineByLine.push({
                            leftContent: row.leftContent,
                            rightContent: null,
                            belongTo: row.belongTo,
                            hasDiffs: true,
                            numDiffs: row.numDiffs,
                        });
                    }
                    if (row.rightContent) {
                        tableLineByLine.push({
                            leftContent: null,
                            rightContent: row.rightContent,
                            belongTo: row.belongTo,
                            hasDiffs: true,
                            numDiffs: row.numDiffs,
                        });
                    }
                } else {
                    tableLineByLine.push(row);
                }

                return tableLineByLine;
            }, []);
            this.diffsCount = this.tableRows.filter(row => row.hasDiffs).length;
            this.filteredTableRows = this.tableRows;
            this.filteredTableRowsLineByLine = this.tableRowsLineByLine;
            this.emitCompareResultsEvent();
        } catch (e) {
            throw e;
        }
    }

    emitCompareResultsEvent() {
        const diffResults: DiffResults = {
            hasDiff: this.diffsCount > 0,
            diffsCount: this.diffsCount,
            rowsWithDiff: this.tableRows
                .filter(row => row.hasDiffs)
                .map(row => ({
                    leftLineNumber: row.leftContent ? row.leftContent.lineNumber : null,
                    rightLineNumber: row.rightContent ? row.rightContent.lineNumber : null,
                    numDiffs: row.numDiffs,
                })),
        };

        this.compareResults.next(diffResults);
    }

    trackTableRows(index, row: DiffTableRowResult) {
        return row && row.leftContent ? row.leftContent.lineContent : row && row.rightContent ? row.rightContent.lineContent : undefined;
    }

    trackDiffs(index, diff: DiffPart) {
        return diff && diff.content ? diff.content : undefined;
    }

    private initScrollListener() {
        this.subscriptions.push(this.scrollService.scrolled().subscribe((scrollableEv: CdkScrollable) => {
            if (scrollableEv && this.synchronizeScrolling) {
                const scrollableId = scrollableEv.getElementRef().nativeElement.id;
                const nonScrolledContainer: ContainerDirective = this.containers.find(container => container.id !== scrollableId);
                if (nonScrolledContainer) {
                    nonScrolledContainer.element.scrollTo({
                        top: scrollableEv.measureScrollOffset('top'),
                        left: scrollableEv.measureScrollOffset('left'),
                    });
                }
            }
        }));
    }
}
