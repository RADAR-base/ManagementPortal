import { Component, OnInit, SimpleChanges, Input, IterableDiffers } from '@angular/core';
import { ContentItem, ContentType } from '../queries.model';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ModalContentComponent } from './modal-content/modal-content.component';

@Component({
    selector: 'query-content',
    templateUrl: './content.component.html',
    styleUrls: ['./content.component.scss']
})
export class ContentComponent implements OnInit {
    ContentType = ContentType;

    @Input() public items: ContentItem[] = []

    title: String = "";

    private differ: any;

    @Input() titleItem: ContentItem = { type: ContentType.TITLE, value: "this is title" }


    constructor(private modalService: NgbModal, private differs: IterableDiffers) {
        this.differ = this.differs.find([]).create();

    }

    ngOnInit(): void {
        this.items = [...this.items, this.titleItem]; // creates new array reference

    }


    ngOnChanges(changes: SimpleChanges) {
        if (changes['titleItem']) {
            console.log('Object changed:', changes['titleItem'].currentValue);
        }
    }


    ngDoCheck(): void {
        //   console.log("ngDoCheck", this.titleItem)
        const changes = this.differ.diff(this.items);

        if (changes) {
            console.log(" there are changes in the array", changes)
        }

    }

    addContent(contentType: ContentType) {
        console.log("items", this.items)
        if (contentType == ContentType.PARAGRAPH) {
            this.items.push({ type: contentType })
        }

        if (contentType == ContentType.VIDEO || contentType == ContentType.IMAGE) {
            const modalRef = this.modalService.open(ModalContentComponent);
            modalRef.componentInstance.type = contentType

            modalRef.result.then((result) => {
                if (typeof result === 'object') {
                    this.items = [result, ...this.items]
                }

            }).catch((reason) => {
                console.log('Modal dismissed:', reason);
            });
        }


    }

    updateTitle() {

    }

    updateContent(event: { content: string; index: string }) {
        console.log("event index", event.index)
        console.log("event context", event.content)

        const content = this.items.find((item, itemIndex) => itemIndex == Number(event.index))
        content.value = event.content;
    }

    deleteContent(index: string) {
        this.items = this.items.filter((item, itemIndex) => itemIndex !== Number(index));
    }


    isMultimediaDisabled() {
        const isMultimediaPresent = this.items.find((item) => item.type == ContentType.IMAGE || item.type == ContentType.VIDEO)
        return !!isMultimediaPresent
    }

    getTitleItem() {
        let titleItem = this.items.find((item) => item.type == ContentType.TITLE);
        return titleItem
    }

}
