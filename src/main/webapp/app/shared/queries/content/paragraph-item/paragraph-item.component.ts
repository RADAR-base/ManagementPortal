import {
    Component, OnInit, AfterViewInit, Output, EventEmitter, Input

} from '@angular/core';
import tinymce from 'tinymce/tinymce';

// Import what you need
import 'tinymce/themes/silver';
import 'tinymce/icons/default';
import 'tinymce/plugins/code';
import 'tinymce/plugins/lists';
import 'tinymce/plugins/link';
import { ContentItem } from '../../queries.model';
import { generateUUID } from '../../utils';


@Component({
    selector: 'query-paragraph-item',
    templateUrl: './paragraph-item.component.html',
    styleUrls: ['./paragraph-item.component.scss']
})
export class ParagraphItemComponent implements OnInit {
    @Output() triggerDeleteItemFunction = new EventEmitter<string>();


    public heading: String = "";
    private editorInstance: any;

    @Input() item: ContentItem

    textAreaId = generateUUID();

    ngOnInit(): void {
    }

    ngAfterViewInit() {
        tinymce.init({
            selector: `#${this.textAreaId}`,
            menubar: false,
            plugins: 'lists',
            toolbar: 'undo redo | bold italic underline | link | bullist numlist | alignleft aligncenter alignright',
            skin_url: '/managementportal/assets/tinymce/skins/ui/oxide',
            icons_url: '/managementportal/assets/tinymce/icons/default/icons.min.js',
            models_url: '/managementportal/assets/tinymce/models/dom/model.min.js',
            base_url: '/managementportal/assets/tinymce',
            content_css: '/managementportal/assets/tinymce/skins/content/default/content.min.css',
            setup: editor => {
                this.editorInstance = editor;
                editor.on('change', () => {
                    const content = editor.getContent();
                    this.item.value = content;
                });

                editor.on('init', () => {
                    const text = this.item.value as string
                    if (text) {
                        editor.setContent(text);
                    }
                });
            },
            height: 300
        });
    }

    ngOnDestroy(): void {
        if (this.editorInstance) {
            tinymce.remove(this.editorInstance);
        }
    }

    onDeleteItem() {
        this.triggerDeleteItemFunction.emit()
    }
}
