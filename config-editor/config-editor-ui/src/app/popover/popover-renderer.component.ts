import { ChangeDetectionStrategy, Component, OnInit, TemplateRef } from '@angular/core';
import { PopoverContent, PopoverRef } from './popover-ref';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    selector: 're-popover-renderer',
    templateUrl: './popover-renderer.component.html',
    styleUrls: ['./popover-renderer.component.scss'],
})

export class PopoverRendererComponent implements OnInit {
    renderMethod: 'template' | 'component' | 'text' = 'component';
    content: PopoverContent;
    context;
    constructor(private popoverRef: PopoverRef) {
    }
    ngOnInit() {
        this.content = this.popoverRef.content;
        if (typeof this.content === 'string') {
            this.renderMethod = 'text';
        } else if (this.content instanceof TemplateRef) {
            this.renderMethod = 'template';
            this.context = {
                close: this.popoverRef.close.bind(this.popoverRef),
            }
        }
    }
}
