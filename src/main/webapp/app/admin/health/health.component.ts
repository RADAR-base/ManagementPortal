import {Component, OnInit} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {JhiHealthModalComponent} from './health-modal.component';

import {JhiHealthService} from './health.service';
import {Health, HealthDetails, HealthStatus} from "./health.model";
import {HttpErrorResponse} from "@angular/common/http";

@Component({
    selector: 'jhi-health',
    templateUrl: './health.component.html',
})
export class JhiHealthCheckComponent implements OnInit {
    health?: Health;

    constructor(private modalService: NgbModal, private healthService: JhiHealthService) {
    }

    ngOnInit(): void {
        this.refresh();
    }

    getBadgeClass(statusState: HealthStatus): string {
        if (statusState === 'UP') {
            return 'badge-success';
        }
        return 'badge-danger';
    }

    refresh(): void {
        this.healthService.checkHealth().subscribe(
            health => (this.health = health),
            (error: HttpErrorResponse) => {
                if (error.status === 503) {
                    this.health = error.error;
                }
            }
        );
    }

    showHealth(health: { key: string; value: HealthDetails }): void {
        const modalRef = this.modalService.open(JhiHealthModalComponent);
        modalRef.componentInstance.health = health;
    }
}
