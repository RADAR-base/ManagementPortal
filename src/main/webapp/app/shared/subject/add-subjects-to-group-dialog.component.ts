import { Component, Input, OnDestroy } from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { tap } from "rxjs/operators";

import { Group, GroupService } from "../group";
import { AlertService } from "../util/alert.service";
import { EventManager } from "../util/event-manager.service";
import { Subject } from "./subject.model";
import { Subscription } from "rxjs";

@Component({
  selector: 'add-subjects-to-group-dialog',
  templateUrl: 'add-subjects-to-group-dialog.component.html',
})
export class AddSubjectsToGroupDialogComponent implements OnDestroy {
  @Input() groups: Group[];
  @Input() projectName: string;
  @Input() subjects: Subject[];

  group: Group | null;
  isSaving = false;
  subscriptions: Subscription;

  constructor(
    public activeModal: NgbActiveModal,
    private alertService: AlertService,
    private eventManager: EventManager,
    private groupService: GroupService,
  ) {
    this.subscriptions = new Subscription();
  }

  clear() {
    this.activeModal.dismiss('cancel');
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  save() {
    this.isSaving = true;
    let groupName = this.group.name;
    let items = this.subjects.map(s => ({ id: s.id }));
    this.subscriptions.add(this.groupService.addSubjectsToGroup(this.projectName, groupName, items)
      .pipe(tap(() => this.isSaving = false))
      .subscribe(
        () => {
          for (let s of this.subjects) {
            this.eventManager.broadcast({
              name: 'subjectListModification',
              content: { op: 'UPDATE', subject: {...s, group: groupName }},
            });
          }
          this.activeModal.close('saved');
        },
        error => {
          this.alertService.error(error.message, null, null);
        }));
  }
}
