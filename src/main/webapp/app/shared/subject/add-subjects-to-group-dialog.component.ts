import { Component, Input } from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { tap } from "rxjs/operators";

import { Group } from "../group/group.model";
import { GroupService } from "../group/group.service";
import { AlertService } from "../util/alert.service";
import { EventManager } from "../util/event-manager.service";
import { Subject } from "./subject.model";

@Component({
  selector: 'add-subjects-to-group-dialog',
  template: `
    <div class="modal-header">
      <h4 class="modal-title">Add subjects to group</h4>
      <button type="button" class="close" aria-label="Close"
        (click)="activeModal.dismiss('cancel')"
      >
        <span aria-hidden="true">&times;</span>
      </button>
    </div>
    <div class="modal-body">
      <select class="form-control" id="field_add_to_group"
        name="add-to-group" [(ngModel)]="group" required
      >
        <option [ngValue]="null"></option>
        <option [ngValue]="g" *ngFor="let g of groups">{{g.name}}</option>
      </select>
    </div>
    <div class="modal-footer">
      <button type="button" class="btn btn-default"
        data-dismiss="modal" (click)="clear()"
      >
        <span class="fa fa-ban"></span>&nbsp;<span
          [translate]="'entity.action.cancel'"></span>
      </button>
      <button type="submit" [disabled]="!group || isSaving"
        class="btn btn-primary" (click)="save()"
      >
        <span class="fa fa-save"></span>&nbsp;<span
          [translate]="'entity.action.save'"></span>
      </button>
    </div>
  `
})
export class AddSubjectsToGroupDialogComponent {
  @Input() groups: Group[];
  @Input() projectName: string;
  @Input() subjects: Subject[];

  group: Group | null;
  isSaving = false;

  constructor(
    public activeModal: NgbActiveModal,
    private alertService: AlertService,
    private eventManager: EventManager,
    private groupService: GroupService,
  ) { }

  clear() {
    this.activeModal.dismiss('cancel');
  }

  save() {
    this.isSaving = true;
    let groupName = this.group.name;
    let items = this.subjects.map(s => ({ id: s.id }));
    this.groupService.addSubjectsToGroup(this.projectName, groupName, items)
      .pipe(tap(() => this.isSaving = false))
      .subscribe(
        () => {
          for (let s of this.subjects) {
            this.eventManager.broadcast({
              name: 'subjectListModification',
              content: { ...s, group: groupName },
            });
          }
          this.activeModal.close('saved');
        },
        error => {
          this.alertService.error(error.message, null, null);
        });
  }
}