import { Component, OnInit, Input } from '@angular/core';
import { ContentItem } from '../../queries.model';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'query-video-item',
  templateUrl: './video-item.component.html',
  styleUrls: ['./video-item.component.scss']
})
export class VideoItemComponent implements OnInit {

    @Input() item: ContentItem

    constructor(private sanitizer: DomSanitizer) {

    }
    ngOnInit(): void {
    }

    getSafeUrl(): SafeResourceUrl {
            return this.sanitizer.bypassSecurityTrustResourceUrl('https://www.youtube.com/embed/' + this.item.value + '?autoplay=0')
    }

}
