import { MonoTypeOperatorFunction, OperatorFunction, pipe } from 'rxjs';
import { distinctUntilChanged, map } from 'rxjs/operators';

export function sortByPredicate<T>(values: T[], predicate: (T) => any, ascending: boolean): T[] {
  const modifier = ascending ? 1 : -1;
  const localValues = [...values];
  localValues.sort((t1, t2) => {
    const v1 = predicate(t1);
    const v2 = predicate(t2);
    if (v1 === v2 || !v1 && !v2) {
      return 0;
    } else if (!v1) {
      return -modifier;
    } else if (!v2) {
      return modifier;
    }
    return modifier * v1.toLocaleString().localeCompare(v2.toLocaleString());
  });
  return localValues;
}

export interface SortOrder {
  predicate: string;
  ascending: boolean;
}

export class SortOrderImpl implements SortOrder {
  constructor(
    public readonly predicate: string,
    public readonly ascending: boolean,
  ) {
  }

  equals(other: SortOrder) {
    if (this === other) return true;
    return this.predicate === other.predicate
      && this.ascending === other.ascending;
  }

  sort<T>(values: T[]): T[] {
    return SortOrderImpl.sortBy(values, this);
  }

  toQueryParam(): string {
    return this.predicate + ',' + (this.ascending ? 'asc' : 'desc');
  }

  static sortBy<T>(values: T[], order: SortOrder): T[] {
    return sortByPredicate(values, t => t[order.predicate], order.ascending);
  }

  static from(order?: SortOrder, defaultPredicate?: string): SortOrderImpl {
    if (!order) {
      return new SortOrderImpl(defaultPredicate || 'id', true);
    } else {
      return new SortOrderImpl(
        order.predicate || (defaultPredicate || 'id'),
        typeof order.ascending === 'boolean' ? order.ascending : true,
      );
    }
  }
}

export function regularSortOrder(defaultPredicate?: string): OperatorFunction<SortOrder, SortOrderImpl> {
    console.log('regularSortOrder', defaultPredicate)
    return pipe(
        map(o => SortOrderImpl.from(o, defaultPredicate)),
        distinctSortOrder(),
    );
}

export function distinctSortOrder(): MonoTypeOperatorFunction<SortOrderImpl> {
  return distinctUntilChanged((o1, o2) => o1.equals(o2));
}
