import {Injectable, Component, ComponentFactoryResolver, ViewContainerRef} from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import {SubjectService} from "../../subject/subject.service";
import {ProjectService} from "../project.service";
import {Subject} from "../../subject/subject.model";
@Injectable()
export class ProjectSubjectPopupService {
    private isOpen = false;
    login ?= 'xxxyxxxx';
    constructor(
        private modalService: NgbModal,
        private router: Router,
        private subjectService: SubjectService,
        private projectService: ProjectService,

    ) {}

    open(component: Component, id?: number | any , projectId?: number | any): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (id) {
            this.subjectService.find(id).subscribe((subject) => {
                this.subjectModalRef(component, subject , projectId);
            });
        } else {
            var subject = new Subject();
            subject.login = this.login.replace(/[xy]/g, function(c) {
                var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
                return v.toString(16);
            });
            return this.subjectModalRef(component, subject, projectId);
        }
    }

    subjectModalRef(component: Component, subjects: Subject , projectId?: number ): NgbModalRef {
        const modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});

        modalRef.componentInstance.isProjectSpecific = true;
        modalRef.componentInstance.subject = subjects;

        if(projectId) {
            this.projectService.find(projectId).subscribe((project) => {
                let projects = [project];
                modalRef.componentInstance.projects = projects;
                console.log('hereh',modalRef.componentInstance.projects)
            });
        }
        modalRef.result.then((result) => {
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
            this.isOpen = false;
        }, (reason) => {
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
            this.isOpen = false;
        });
        return modalRef;
    }
}
