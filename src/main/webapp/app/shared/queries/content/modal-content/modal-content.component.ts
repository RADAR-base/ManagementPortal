import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ContentItem, ContentType } from '../../queries.model';
import { generateUUID } from '../../utils';


type ImageSizeValidType = {
    base64String?: string;
    imageSizeBytes?: number;
    imageSizeLimitInMB?: number;
}

type SizeRatio = {
    width: number;
    height: number;
}

type SelectedImageBase64 = {
    imageBase64Data?: String;
    isImageValid?: Boolean;
}

@Component({
    selector: 'app-modal-content',
    templateUrl: './modal-content.component.html',
    styleUrls: ['./modal-content.component.scss']
})
export class ModalContentComponent implements OnInit {

    ContentType = ContentType;
    public content: string
    public type: ContentType
    public imageBase64: string
    public contentItem: ContentItem


    constructor(public activeModal: NgbActiveModal) {

    }


    ngOnInit(): void {
        console.log("type modal", this.type)
    }

    isImageSizeValid(args: ImageSizeValidType) {
        const { base64String, imageSizeBytes, imageSizeLimitInMB } = { ...args }
        const bytes = base64String
            ? base64String.length * (3 / 4) - 1
            : imageSizeBytes
        const megabytes = bytes / (1024 * 1024)

        return megabytes < imageSizeLimitInMB
    }

    getBase64FromImage(file: File, targetWidth: number, targetHeight: number) {



    }


    getScaledImageBase64(image: HTMLImageElement, resizeRatio: number) {
        const [imageWidth, imageHeight] = [
            image.naturalWidth,
            image.naturalHeight
        ];

        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        canvas.width = imageWidth * resizeRatio;
        canvas.height = imageHeight * resizeRatio;

        ctx.fillStyle = '#FFF';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        ctx.drawImage(image, 0, 0, canvas.width, canvas.height);

        const base64 = canvas.toDataURL('image/jpeg', 0.7);
        canvas.remove();
        return base64;
    }


    getResizeRatio(image: HTMLImageElement, targetSize: SizeRatio) {
        const [imageWidth, imageHeight] = [
            image.naturalWidth,
            image.naturalHeight
        ]


        if (imageHeight > targetSize.height && imageWidth > targetSize.width) {
            const ratio = Math.min(
                targetSize.width / imageWidth,
                targetSize.height / imageHeight
            )

            return ratio
        }

        return 1
    }

    async getImage(file: File) {
        const reader = new FileReader();

        console.log("before original base 64")
        const originalBase64 = await new Promise<string | ArrayBuffer>(resolve => {
             reader.onload = (event) => {
                resolve(event.target.result)
            };
            reader.readAsDataURL(file);


        }) as string;

        console.log("after original base 64")

        const image = new Image();

        console.log("before onload")
        await new Promise(resolve => {
            image.onload = () => {
                console.log("image loaded")
                resolve("true")
            }
            image.src = originalBase64;
        });

        console.log("after onload")

        return { image, originalBase64 };


    }

    async onImageSelected(event: Event) {

        let imageSizeLimitInMB = 0.5;

        let rtn: SelectedImageBase64 = {}

        const input = event.target as HTMLInputElement;

        if (!input.files || input.files.length === 0) return;

        const file = input.files[0];


        console.log("before get image")

        const { image, originalBase64 } = await this.getImage(file);

        console.log("after image")


        const isResizeNeeded = this.isImageSizeValid({

            imageSizeBytes: file.size,
            imageSizeLimitInMB: imageSizeLimitInMB
        })

        if (isResizeNeeded) {
            const resizeRation = this.getResizeRatio(image, { width: 1024, height: 768 })

            const compressedBase64 = this.getScaledImageBase64(image, resizeRation).split(',')[1];

            const isImageValid = this.isImageSizeValid({
                base64String: compressedBase64,
                imageSizeLimitInMB: imageSizeLimitInMB
            });

             rtn =  {
                imageBase64Data: compressedBase64,
                isImageValid: isImageValid
            };
        } else {
             rtn =  {
                imageBase64Data: originalBase64.split(',')[1],
                isImageValid: true
            };
        }

        console.log("rtn", rtn)

        this.contentItem = {
            type: ContentType.IMAGE,
            value: rtn.imageBase64Data,
            isValidImage: rtn.isImageValid
            }

        // const reader = new FileReader();

        // reader.onload = () => {
        //     const base64 = reader.result as string;
        //     this.imageBase64 = reader.result as string;
        //     console.log('Base64 Image:', this.imageBase64);

        //     const img = new Image();
        //     img.onload = () => {
        //         const width = img.naturalWidth;

        //         const height = img.naturalHeight;
        //         const aspectRatio = width / height;

        //         console.log('Width:', width);
        //         console.log('Height:', height);
        //         console.log('Aspect Ratio:', aspectRatio);
        //     };
        //     img.src = base64;



        // };

        // reader.readAsDataURL(file);
    }

    addItem() {

        if (this.type == ContentType.VIDEO) {
            let getYoutubeIdRegex = new RegExp(
                // eslint-disable-next-line no-useless-escape
                `.*(?:(?:youtu\.be\/|v\/|vi\/|u\/\w\/|embed\/|shorts\/)|(?:(?:watch)?\?v(?:i)?=|\&v(?:i)?=))([^#\&\?]*).*`
            );

            const youtubeId = getYoutubeIdRegex.exec(this.content)[1];
            const contentItem: ContentItem = {
                type: ContentType.VIDEO,
                value: youtubeId
            }

            this.activeModal.close(contentItem)
        } else {
            // process image
            console.log("this content", this.contentItem)

            this.activeModal.close(this.contentItem)
        }

    }

}
