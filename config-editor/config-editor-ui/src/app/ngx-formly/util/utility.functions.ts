import { isObservable } from 'rxjs';

import { AbstractControl } from '@angular/forms';
import { FormlyFieldConfigCache } from '@ngx-formly/core/lib/components/formly.field.config';

/**
 * Copied function library: TODO: remove or use package instead
 * 
*/

export function getKeyPath(field: FormlyFieldConfigCache): string[] {
    if (!field.key) {
      return [];
    }
  
    /* We store the keyPath in the field for performance reasons. This function will be called frequently. */
    if (!field._keyPath || field._keyPath.key !== field.key) {
      const key = field.key.indexOf('[') === -1
        ? field.key
        : field.key.replace(/\[(\w+)\]/g, '.$1');
  
      field._keyPath = { key: field.key, path: key.indexOf('.') !== -1 ? key.split('.') : [key] };
    }
  
    return field._keyPath.path.slice(0);
}
  
export function assignModelValue(model: any, paths: string[], value: any) {
    for (let i = 0; i < (paths.length - 1); i++) {
    const path = paths[i];
    if (!model[path] || !isObject(model[path])) {
        model[path] = /^\d+$/.test(paths[i + 1]) ? [] : {};
    }

    model = model[path];
    }

    model[paths[paths.length - 1]] = clone(value);
}

export function clone(value: any): any {
    if (
      !isObject(value)
      || isObservable(value)
      || /* instanceof SafeHtmlImpl */ value.changingThisBreaksApplicationSecurity
      || ['RegExp', 'FileList', 'File', 'Blob'].indexOf(value.constructor.name) !== -1
    ) {
      return value;
    }
  
    // https://github.com/moment/moment/blob/master/moment.js#L252
    if (value._isAMomentObject && isFunction(value.clone)) {
      return value.clone();
    }
  
    if (value instanceof AbstractControl) {
      return null;
    }
  
    if (value instanceof Date) {
      return new Date(value.getTime());
    }
  
    if (Array.isArray(value)) {
      return value.slice(0).map(v => clone(v));
    }
  
    // best way to clone a js object maybe
    // https://stackoverflow.com/questions/41474986/how-to-clone-a-javascript-es6-class-instance
    const proto = Object.getPrototypeOf(value);
    let c = Object.create(proto);
    c = Object.setPrototypeOf(c, proto);
    // need to make a deep copy so we dont use Object.assign
    // also Object.assign wont copy property descriptor exactly
    return Object.keys(value).reduce((newVal, prop) => {
      const propDesc = Object.getOwnPropertyDescriptor(value, prop);
      if (propDesc.get) {
        Object.defineProperty(newVal, prop, propDesc);
      } else {
        newVal[prop] = clone(value[prop]);
      }
  
      return newVal;
    }, c);
  }

  export function isNullOrUndefined(value: any) {
    return value === undefined || value === null;
  }

  export function isFunction(value: any) {
    return typeof(value) === 'function';
  }

  export function isObject(item: any): boolean {
    return item !== null && typeof item === 'object' &&
      Object.prototype.toString.call(item) === '[object Object]';
  }