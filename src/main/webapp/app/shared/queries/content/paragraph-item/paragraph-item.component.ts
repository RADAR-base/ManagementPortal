import {
    Component, OnInit, AfterViewInit

} from '@angular/core';
import tinymce from 'tinymce/tinymce';

// Import what you need
import 'tinymce/themes/silver';
import 'tinymce/icons/default';
import 'tinymce/plugins/code';
import 'tinymce/plugins/lists';
import 'tinymce/plugins/link';

@Component({
    selector: 'query-paragraph-item',
    templateUrl: './paragraph-item.component.html',
    styleUrls: ['./paragraph-item.component.scss']
})
export class ParagraphItemComponent implements OnInit {
    public editorUUID: String = "";
    public heading: String = "";
    private editorInstance: any;

    constructor() {
        const id = Date.now().toString(36) + Math.random().toString(36).substr(2, 5);
        this.editorUUID = id;
    }

    ngOnInit(): void {
    }

    ngAfterViewInit() {
        tinymce.init({
            selector: `#${this.editorUUID}`,
            plugins: 'code lists link',
            toolbar: 'undo redo | bold italic | alignleft aligncenter alignright | code',
            skin_url: '/assets/tinymce/skins/ui/oxide',
            icons_url: '/assets/tinymce/icons/default/icons.min.js',
            models_url: '/assets/tinymce/models/dom/model.min.js',
            base_url: '/assets/tinymce',
            content_css: '/assets/tinymce/skins/content/default/content.min.css',
            setup: editor => {
                this.editorInstance = editor;
            },
            height: 300
        });
    }


    getContent(): string {
        return this.editorInstance?.getContent() || '';
    }

    ngOnDestroy(): void {
        if (this.editorInstance) {
            tinymce.remove(this.editorInstance);
        }
    }
}
