// Borrowed from JHipster

import { Injectable } from '@angular/core';
import { Observable, Observer, Subscription } from 'rxjs';
import { filter, share } from 'rxjs/operators';

export class EventWithContent {
    constructor(public name: string, public content: any) { }
}

/**
 * An utility class to manage RX events
 */
@Injectable({ providedIn: 'root' })
export class EventManager {
    observable: Observable<EventWithContent>;
    observer?: Observer<EventWithContent>;

    constructor() {
        this.observable = new Observable((observer: Observer<EventWithContent>) => {
            this.observer = observer;
        }).pipe(share());
    }

    /**
     * Method to broadcast the event to observer
     */
    broadcast(event: EventWithContent): void {
        if (this.observer) {
            this.observer.next(event);
        }
    }

    /**
     * Method to subscribe to an event with callback
     * @param eventNames  Single event name or array of event names to what subscribe
     * @param callback    Callback to run when the event occurs
     */
    subscribe(eventNames: string | string[], callback: (event: EventWithContent) => void): Subscription {
        if (typeof eventNames === 'string') {
            eventNames = [eventNames];
        }
        return this.observable
            .pipe(
                filter((event: EventWithContent) => {
                    for (const eventName of eventNames) {
                        if (event.name === eventName) {
                            return true;
                        }
                    }
                    return false;
                })
            )
            .subscribe(callback);
    }

    /**
     * Method to unsubscribe the subscription
     */
    destroy(subscriber: Subscription): void {
        if (subscriber) {
            subscriber.unsubscribe();
        }
    }
}
