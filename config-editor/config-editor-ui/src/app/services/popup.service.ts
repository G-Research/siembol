import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable } from 'rxjs/Observable';
import { ErrorDialogComponent } from '@app/components/error-dialog/error-dialog.component';

@Injectable({
  providedIn: 'root',
})
export class PopupService {
  private snackbarAction$: Observable<void>;

  constructor(public snackBar: MatSnackBar, public dialog: MatDialog) {}

  openSnackBar(error: any, description: string, action: string = 'DETAILS') {
    console.error(error);
    this.snackBar.open(description, action, { duration: 5000 });
    this.snackbarAction$ = this.snackBar._openedSnackBarRef.onAction();
    this.snackbarAction$.subscribe(
      () =>
        this.dialog.open(ErrorDialogComponent, {
          data: error,
          width: '500px',
        }),
      () => console.error('an error occurred')
    );
  }

  openNotification(description: string) {
    this.snackBar.open(description, null, { duration: 5000, verticalPosition: 'top' });
  }
}
