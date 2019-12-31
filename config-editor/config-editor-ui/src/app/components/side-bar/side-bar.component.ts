import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { ConfigData, ConfigWrapper } from '@app/model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-side-bar',
  styleUrls: ['./side-bar.component.scss', '../config-manager/config-manager.component.scss'],
  templateUrl: './side-bar.component.html',
})
export class SideBarComponent {
  @Input() configs: ConfigWrapper<ConfigData>[];
  @Input() selectedConfig: number;
  @Output() onReturn: EventEmitter<void> = new EventEmitter<void>();
  @Output() selectedIndex: EventEmitter<number> = new EventEmitter<number>();
}
