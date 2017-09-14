import {
    Component, OnInit, OnDestroy, Input, OnChanges, SimpleChanges,
    SimpleChange
} from '@angular/core';
import { Response } from '@angular/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager, ParseLinks, PaginationUtil, JhiLanguageService, AlertService } from 'ng-jhipster';

import { ITEMS_PER_PAGE, Principal } from '../../shared';
import { PaginationConfig } from '../../blocks/config/uib-pagination.config';
import {Project} from "../project/project.model";
import {Subject} from "../../shared/subject/subject.model";
import {SubjectService} from "../../shared/subject/subject.service";

@Component({
    selector: 'jhi-subject',
    templateUrl: './general.subject.component.html'
})
export class GeneralSubjectComponent {
    isProjectSpecific =false;
}
