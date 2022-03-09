import { ConfigData, Config } from '../../model/config-model';

import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-release-tile',
    styleUrls: ['./config-tile.component.scss'],
    templateUrl: './release-tile.component.html',
})
export class ReleaseTileComponent {

    @Input() config: Config;

    @Output() onDelete = new EventEmitter<number>();
    @Output() onUpgrade = new EventEmitter<number>();
    @Output() onViewDiff = new EventEmitter<any>();

    constructor() {}

    deleteConfig() {
        this.onDelete.emit();
    }

    upgradeConfig() {
        this.onUpgrade.emit();
    }

    viewDiff() {
        this.onViewDiff.emit();
    }
}
