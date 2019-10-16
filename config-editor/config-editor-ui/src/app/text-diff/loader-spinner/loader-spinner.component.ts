import { Component, Input, OnInit } from '@angular/core';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'td-loader-spinner',
  templateUrl: './loader-spinner.component.html',
  styleUrls: ['./loader-spinner.component.css'],
})
export class LoaderSpinnerComponent implements OnInit {
  @Input() active = false;

  constructor() {}

  ngOnInit() {}
}
