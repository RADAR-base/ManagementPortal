import {
    Component,
    OnInit,
    Input,
    IterableDiffers,
    Output,
    EventEmitter,
} from '@angular/core';
import { ContentItem, ContentType } from '../queries.model';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ModalContentComponent } from './modal-content/modal-content.component';

@Component({
    selector: 'query-content',
    templateUrl: './content.component.html',
    styleUrls: ['./content.component.scss'],
})
export class ContentComponent implements OnInit {
    ContentType = ContentType;

    @Input() public items: ContentItem[] = [];
    @Output() public itemsChange = new EventEmitter<ContentItem[]>();

    title: String = '';

    private differ: any;

    @Input() titleItem: ContentItem = {
        type: ContentType.TITLE,
        value: 'this is title',
    };

    constructor(
        private modalService: NgbModal,
        private differs: IterableDiffers
    ) {
        this.differ = this.differs.find([]).create();
    }

    ngOnInit(): void {
        const hasTitle = this.items.some(
            (item) => item.type === ContentType.TITLE
        );
        if (!hasTitle) {
            this.items = [this.titleItem, ...this.items];
        }
    }

    addContent(contentType: ContentType) {
        if (contentType == ContentType.PARAGRAPH) {
            this.items.push({ type: contentType });
            this.itemsChange.emit(this.items);
        }
        if (
            contentType == ContentType.VIDEO ||
            contentType == ContentType.IMAGE
        ) {
            const modalRef = this.modalService.open(ModalContentComponent);
            modalRef.componentInstance.type = contentType;

            modalRef.result
                .then((result) => {
                    if (typeof result === 'object') {
                        this.items = [result, ...this.items];
                        this.itemsChange.emit(this.items);
                    }
                })
                .catch((reason) => {
                    console.log('Modal dismissed:', reason);
                });
        }
    }

    updateContent(event: { content: string; index: string }) {
        const content = this.items.find(
            (_, idx) => idx === Number(event.index)
        );
        if (content) {
            content.value = event.content;
            this.itemsChange.emit(this.items);
        }
    }

    deleteContent(index: string) {
        this.items = this.items.filter((_, idx) => idx !== Number(index));
        this.itemsChange.emit(this.items);
    }

    updateTitle() {}

    isMultimediaDisabled() {
        const isMultimediaPresent = this.items.find(
            (item) =>
                item.type == ContentType.IMAGE || item.type == ContentType.VIDEO
        );
        return !!isMultimediaPresent;
    }

    getTitleItem() {
        let titleItem = this.items.find(
            (item) => item.type == ContentType.TITLE
        );
        return titleItem;
    }
}
