import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiHealthModalComponent } from './health-modal.component';

import { JhiHealthService } from './health.service';

@Component({
    selector: 'jhi-health',
    templateUrl: './health.component.html',
})
export class JhiHealthCheckComponent implements OnInit {
    healthData: any;

    constructor(
            private modalService: NgbModal,
            private healthService: JhiHealthService,
    ) {
    }

    ngOnInit() {
        this.refresh();
    }

    baseName(name: string) {
        return this.healthService.getBaseName(name);
    }

    getBadgeClass(statusState) {
        if (statusState === 'UP') {
            return 'badge-success';
        } else {
            return 'badge-danger';
        }
    }

    refresh() {
        this.healthService.checkHealth().subscribe((health) => {
            this.healthData = this.healthService.transformHealthData(health);
        }, (error) => {
            if (error.status === 503) {
                this.healthData = this.healthService.transformHealthData(error.json());
            }
        });
    }

    showHealth(health: any) {
        const modalRef = this.modalService.open(JhiHealthModalComponent);
        modalRef.componentInstance.currentHealth = health;
        modalRef.result.then((result) => {
            // Left blank intentionally, nothing to do here
        }, (reason) => {
            // Left blank intentionally, nothing to do here
        });
    }

    subSystemName(name: string) {
        return this.healthService.getSubSystemName(name);
    }

}
