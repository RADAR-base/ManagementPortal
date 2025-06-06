import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'app-delete-query-confirm-dialog',
    template: `
    <div class="modal-header">
        <h4 class="modal-title">Delete Assignment</h4>
    </div>
    <div class="modal-body">
        <p>Do you want to also remove the content?</p>
    </div>
    <div class="modal-footer">
        <button class="btn btn-danger" (click)="confirm(true)">Yes</button>
        <button class="btn btn-secondary" (click)="confirm(false)">No</button>
    </div>
    `
})
export class DeleteQueryConfirmDialogComponent {
    constructor(public activeModal: NgbActiveModal) {}

    confirm(removeContent: boolean) {
        this.activeModal.close(removeContent);
    }
}
