import {Component, ElementRef, Input, Renderer2} from '@angular/core';
import {Password} from './password.service';

@Component({
    selector: 'jhi-password-strength-bar',
    template: `
        <div id="strength">
            <small [translate]="'global.messages.validate.newpassword.strength'">
            </small>
            <ul id="strengthBar">
                <li class="point"></li>
                <li class="point"></li>
                <li class="point"></li>
                <li class="point"></li>
                <li class="point"></li>
            </ul>
        </div>`,
    styleUrls: [
        'password-strength-bar.scss',
    ],
})
export class PasswordStrengthBarComponent {

    colors = ['#F00', '#F90', '#FF0', '#9F0', '#0F0'];

    constructor(
        private renderer: Renderer2,
        private elementRef: ElementRef,
        private passwordService: Password,
    ) {
    }

    @Input()
    set passwordToCheck(password: string) {
        if (password) {
            const c = this.getColor(this.passwordService.measureStrength(password));
            const element = this.elementRef.nativeElement;
            if (element.className) {
                this.renderer.removeClass(element, element.className);
            }
            const lis = element.getElementsByTagName('li');
            for (let i = 0; i < lis.length; i++) {
                if (i < c.idx) {
                    this.renderer.setStyle(lis[i], 'backgroundColor', c.col);
                } else {
                    this.renderer.setStyle(lis[i], 'backgroundColor', '#DDD');
                }
            }
        }
    }

    getColor(s: number): any {
        let idx = 0;
        if (s < 20) {
            idx = 0;
        } else if (s < 30) {
            idx = 1;
        } else if (s < 40) {
            idx = 2;
        } else if (s < 55) {
            idx = 3;
        } else {
            idx = 4;
        }
        return {idx: idx + 1, col: this.colors[idx]};
    }
}
