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
    private readonly _projects$ = new BehaviorSubject<Project[]>([]);
    private readonly _trigger$ = new Subject<void>();

    private organizationResourceUrl = 'api/organizations';

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
          switchMap(([account]) => {
              if (account) {
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
        console.log(project)
        // project.organizationName = 'aaa'
        // project.organization = {name: 'aaa'}
        // {
        //     description: "the hyve",
        //     id: 1301,
        //     location: "utrecht",
        //     name: "the hyve",
        // };
        return this.http.post(this.projectUrl(), this.convertProjectToServer(project)).pipe(
          map(p => this.convertProjectFromServer(p)),
          tap(
            p => this.updateProject(p),
            () => this.reset(),
          ),
        )
    }

    update(project: Project): Observable<Project> {
        return this.http.put<Project>(this.projectUrl(), this.convertProjectToServer(project)).pipe(
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
        return this.http.get(this.projectUrl(projectName)).pipe(
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
        return this.http.get<Project[]>(this.projectUrl(), {params: options, observe: 'response'});
    }

    findAllByOrganization(orgName: string): Observable<Project[]> {
        let url = `${this.organizationResourceUrl}/${orgName}/projects`;
        return this.http.get<Project[]>(url);
    }


    findSourceTypesByName(projectName: string): Observable<SourceType[]> {
        return this.find(projectName).pipe(
          pluck('sourceTypes'),
          switchMap(sourceTypes => {
              if (sourceTypes) {
                  return of(sourceTypes);
              } else {
                  return this.http.get<SourceType[]>(this.projectUrl(projectName) + '/source-types');
              }
          })
        );
    }

    delete(projectName: string): Observable<any> {
        return this.http.delete(this.projectUrl(projectName)).pipe(
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

    protected convertProjectToServer(project: Project): any {
        return {
            ...project,
            startDate: toDate(project.startDate),
            endDate: toDate(project.endDate),
        };
    }

    protected convertProjectFromServer(projectFromServer: any): Project {
        return {
            ...projectFromServer,
            startDate: convertDateTimeFromServer(projectFromServer.startDate),
            endDate: convertDateTimeFromServer(projectFromServer.endDate),
        };
    }

    protected projectUrl(projectName?: string): string {
        let url = 'api/projects';
        if (projectName) {
            url += '/' + encodeURIComponent(projectName);
        }
        return url;
    }

}
