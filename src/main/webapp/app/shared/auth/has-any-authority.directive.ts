import { Directive, Input, OnDestroy, OnInit, TemplateRef, ViewContainerRef } from '@angular/core';
import { Principal } from './principal.service';
import { Subscription } from "rxjs";
import { filter, map } from "rxjs/operators";

/**
 * @whatItDoes Conditionally includes an HTML element if current user has any
 * of the authorities passed as the `expression`.
 *
 * @howToUse
 * ```
 *     <some-element *jhiHasAnyAuthority="'ROLE_ADMIN'">...</some-element>
 *
 *     <some-element *jhiHasAnyAuthority="['ROLE_ADMIN', 'ROLE_USER']">...</some-element>
 * ```
 */
@Directive({
    selector: '[jhiHasAnyAuthority]',
})
export class HasAnyAuthorityDirective implements OnDestroy {
    private subscriptions = new Subscription();

    constructor(private principal: Principal, private templateRef: TemplateRef<any>, private viewContainerRef: ViewContainerRef) {
    }

    ngOnDestroy(): void {
        this.subscriptions.unsubscribe();
    }

    @Input()
    set jhiHasAnyAuthority(value: string | string[]) {
        const authorities = typeof value === 'string' ? [<string> value] : <string[]> value;
        this.subscriptions.add(
            this.principal.account$
                .pipe(
                  map(user => this.principal.accountHasAnyAuthority(user, authorities))
                )
                .subscribe((hasAuthority) => {
                    this.viewContainerRef.clear();
                    if (hasAuthority) {
                        this.viewContainerRef.createEmbeddedView(this.templateRef);
                    }
                })
        );
    }

}
