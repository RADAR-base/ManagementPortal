import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import {
    BehaviorSubject,
    combineLatest,
    Observable,
    of,
    Subject,
    throwError
} from 'rxjs';
import {
    concatMap,
    delay,
    map,
    pluck,
    retryWhen,
    startWith,
    switchMap,
    take,
    tap,
} from 'rxjs/operators';

import { Project } from './project.model';
import { SourceType } from '../../entities/source-type';
import { createRequestOption } from '../model/request.utils';
import { convertDateTimeFromServer, toDate } from '../util/date-util';
import { Principal } from "../auth/principal.service";
import { AlertService } from "../util/alert.service";

@Injectable({ providedIn: 'root' })
export class ProjectService {
    private resourceUrl = 'api/projects';
    private readonly _projects$ = new BehaviorSubject<Project[]>([]);
    private readonly _trigger$ = new Subject<void>();

    projects$: Observable<Project[]> = this._projects$.asObservable();

    constructor(
      private http: HttpClient,
      private principal: Principal,
      private alertService: AlertService,
    ) {
        combineLatest([
            principal.account$,
            this._trigger$.pipe(startWith(undefined as void)),
        ]).pipe(
          switchMap(([state]) => {
              if (state) {
                  return this.fetch().pipe(
                    retryWhen(errors => errors.pipe(
                      delay(1000),
                      take(10),
                      concatMap(err => throwError(err)))
                    ),
                  );
              } else {
                  return of([]);
              }
          })
        ).subscribe(
          projects => this._projects$.next(projects),
          err => this.alertService.error(err.message, null, null),
        );
    }

    reset() {
        this._trigger$.next();
    }

    create(project: Project): Observable<Project> {
        return this.http.post(this.resourceUrl, this.convertProjectToServer(project)).pipe(
          map(p => this.convertProjectFromServer(p)),
          tap(
            p => this.updateProject(p),
            () => this.reset(),
          ),
        )
    }

    update(project: Project): Observable<Project> {
        return this.http.put<Project>(this.resourceUrl, this.convertProjectToServer(project)).pipe(
          map(p => this.convertProjectFromServer(p)),
          tap(
            p => this.updateProject(p),
            () => this.reset(),
          ),
        )
    }

    find(projectName: string): Observable<Project> {
        return this.projects$.pipe(
          switchMap(projects => {
              const existingProject = projects.find(p => p.projectName === projectName);
              if (existingProject) {
                  return of(existingProject);
              } else {
                  return this.fetchProject(projectName);
              }
          })
        );
    }

    fetchProject(projectName: string): Observable<Project> {
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(projectName)}`).pipe(
          map(p => this.convertProjectFromServer(p)),
          tap(p => this.updateProject(p)),
        );
    }

    fetch(): Observable<Project[]> {
        return this.query().pipe(
          map(res => res.body.map(p => this.convertProjectFromServer(p))),
        );
    }

    query(req?: any): Observable<HttpResponse<Project[]>> {
        const options = createRequestOption(req);
        return this.http.get<Project[]>(this.resourceUrl, {params: options, observe: 'response'});
    }

    findSourceTypesByName(projectName: string): Observable<SourceType[]> {
        return this.find(projectName).pipe(
          pluck('sourceTypes'),
          switchMap(sourceTypes => {
              if (sourceTypes) {
                  return of(sourceTypes);
              } else {
                  return this.http.get<SourceType[]>(`${this.resourceUrl}/${projectName}/source-types`)
              }
          })
        );
    }

    delete(projectName: string): Observable<any> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(projectName)}`).pipe(
          tap(
            () => {
                const newProjects = this._projects$.value.slice()
                const idx = newProjects.findIndex(p => p.projectName === projectName);
                if (idx >= 0) {
                    newProjects.splice(idx, 1);
                    this._projects$.next(newProjects);
                }
            },
            () => this.reset(),
          ),
        );
    }

    private updateProject(project: Project) {
        const nextValue = this._projects$.value.slice();
        const idx = nextValue.findIndex(p => p.projectName === project.projectName);
        if (idx >= 0) {
            nextValue[idx] = project;
        } else {
            nextValue.push(project);
        }
        this._projects$.next(nextValue);
    }

    private convertProjectToServer(project: Project): any {
        const copy: Project = Object.assign({}, project);
        copy.startDate = toDate(project.startDate);
        copy.endDate = toDate(project.endDate);
        return copy;
    }

    private convertProjectFromServer(projectFromServer: any): Project {
        const copy: Project = Object.assign({}, projectFromServer);
        copy.startDate = convertDateTimeFromServer(projectFromServer.startDate);
        copy.endDate = convertDateTimeFromServer(projectFromServer.endDate);
        return copy;
    }

}
