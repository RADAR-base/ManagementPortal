
import {Dictionary} from "./dictionary-mapper.model";
import {Pipe, PipeTransform} from "@angular/core";

@Pipe({name: 'dictionaryKeys', pure: false})
export class DictionaryKeysPipe implements PipeTransform {
    transform(value: Dictionary) {
        return Object.keys(value);
    }
}
