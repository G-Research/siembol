import { Injectable } from '@angular/core';
import { hasOwn } from './utility.functions';
import {
  isArray,
  isDefined,
  isMap,
  isObject,
  isString
  } from './validator.functions';


/**
 * 'JsonPointer' class
 *
 * Some utilities for using JSON Pointers with JSON objects
 * https://tools.ietf.org/html/rfc6901
 *
 * get, getCopy, getFirst, set, setCopy, insert, insertCopy, remove, has, dict,
 * forEachDeep, forEachDeepCopy, escape, unescape, parse, compile, toKey,
 * isJsonPointer, isSubPointer, toIndexedPointer, toGenericPointer,
 * toControlPointer, toSchemaPointer, toDataPointer, parseObjectPath
 *
 * Some functions based on manuelstofer's json-pointer utilities
 * https://github.com/manuelstofer/json-pointer
 */
export type Pointer = string | string[];

@Injectable({
    providedIn: 'root',
  })
export class JsonPointer {

  /**
   * 'get' function
   *
   * Uses a JSON Pointer to retrieve a value from an object.
   *
   * //  { object } object - Object to get value from
   * //  { Pointer } pointer - JSON Pointer (string or array)
   * //  { number = 0 } startSlice - Zero-based index of first Pointer key to use
   * //  { number } endSlice - Zero-based index of last Pointer key to use
   * //  { boolean = false } getBoolean - Return only true or false?
   * //  { boolean = false } errors - Show error if not found?
   * // { object } - Located value (or true or false if getBoolean = true)
   */
  static get(
    object, pointer, startSlice = 0, endSlice: number = null,
    getBoolean = false, errors = false
  ) {
    if (object === null) { return getBoolean ? false : undefined; }
    let keyArray: any[] = this.parse(pointer, errors);
    if (typeof object === 'object' && keyArray !== null) {
      let subObject = object;
      if (startSlice >= keyArray.length || endSlice <= -keyArray.length) { return object; }
      if (startSlice <= -keyArray.length) { startSlice = 0; }
      if (!isDefined(endSlice) || endSlice >= keyArray.length) { endSlice = keyArray.length; }
      keyArray = keyArray.slice(startSlice, endSlice);
      for (let key of keyArray) {
        if (key === '-' && isArray(subObject) && subObject.length) {
          key = subObject.length - 1;
        }
        if (isMap(subObject) && subObject.has(key)) {
          subObject = subObject.get(key);
        } else if (typeof subObject === 'object' && subObject !== null &&
          hasOwn(subObject, key)
        ) {
          subObject = subObject[key];
        } else {
          if (errors) {
            console.error(`get error: "${key}" key not found in object.`);
            console.error(pointer);
            console.error(object);
          }
          return getBoolean ? false : undefined;
        }
      }
      return getBoolean ? true : subObject;
    }
    if (errors && keyArray === null) {
      console.error(`get error: Invalid JSON Pointer: ${pointer}`);
    }
    if (errors && typeof object !== 'object') {
      console.error('get error: Invalid object:');
      console.error(object);
    }
    return getBoolean ? false : undefined;
  }

  /**
   * 'remove' function
   *
   * Uses a JSON Pointer to remove a key and its attribute from an object
   *
   * //  { object } object - object to delete attribute from
   * //  { Pointer } pointer - JSON Pointer (string or array)
   * // { object }
   */
  static remove(object, pointer) {
    const keyArray = this.parse(pointer);
    if (keyArray !== null && keyArray.length) {
      let lastKey = keyArray.pop();
      const parentObject = this.get(object, keyArray);
      if (isArray(parentObject)) {
        if (lastKey === '-') { lastKey = parentObject.length - 1; }
        parentObject.splice(lastKey, 1);
      } else if (isObject(parentObject)) {
        delete parentObject[lastKey];
      }
      return object;
    }
    console.error(`remove error: Invalid JSON Pointer: ${pointer}`);
    return object;
  }

  /**
   * 'has' function
   *
   * Tests if an object has a value at the location specified by a JSON Pointer
   *
   * //  { object } object - object to chek for value
   * //  { Pointer } pointer - JSON Pointer (string or array)
   * // { boolean }
   */
  static has(object, pointer) {
    const hasValue = this.get(object, pointer, 0, null, true);
    return hasValue;
  }

  /**
   * 'dict' function
   *
   * Returns a (pointer -> value) dictionary for an object
   *
   * //  { object } object - The object to create a dictionary from
   * // { object } - The resulting dictionary object
   */
  static dict(object) {
    const results: any = {};
    this.forEachDeep(object, (value, pointer) => {
      if (typeof value !== 'object') { results[pointer] = value; }
    });
    return results;
  }

  /**
   * 'forEachDeep' function
   *
   * Iterates over own enumerable properties of an object or items in an array
   * and invokes an iteratee function for each key/value or index/value pair.
   * By default, iterates over items within objects and arrays after calling
   * the iteratee function on the containing object or array itself.
   *
   * The iteratee is invoked with three arguments: (value, pointer, rootObject),
   * where pointer is a JSON pointer indicating the location of the current
   * value within the root object, and rootObject is the root object initially
   * submitted to th function.
   *
   * If a third optional parameter 'bottomUp' is set to TRUE, the iterator
   * function will be called on sub-objects and arrays after being
   * called on their contents, rather than before, which is the default.
   *
   * This function can also optionally be called directly on a sub-object by
   * including optional 4th and 5th parameterss to specify the initial
   * root object and pointer.
   *
   * //  { object } object - the initial object or array
   * //  { (v: any, p?: string, o?: any) => any } function - iteratee function
   * //  { boolean = false } bottomUp - optional, set to TRUE to reverse direction
   * //  { object = object } rootObject - optional, root object or array
   * //  { string = '' } pointer - optional, JSON Pointer to object within rootObject
   * // { object } - The modified object
   */
  static forEachDeep(
    object, fn: (v: any, p?: string, o?: any) => any = (v) => v,
    bottomUp = false, pointer = '', rootObject = object
  ) {
    if (typeof fn !== 'function') {
      console.error(`forEachDeep error: Iterator is not a function:`, fn);
      return;
    }
    if (!bottomUp) { fn(object, pointer, rootObject); }
    if (isObject(object) || isArray(object)) {
      for (const key of Object.keys(object)) {
        const newPointer = pointer + '/' + this.escape(key);
        this.forEachDeep(object[key], fn, bottomUp, newPointer, rootObject);
      }
    }
    if (bottomUp) { fn(object, pointer, rootObject); }
  }

  /**
   * 'forEachDeepCopy' function
   *
   * Similar to forEachDeep, but returns a copy of the original object, with
   * the same keys and indexes, but with values replaced with the result of
   * the iteratee function.
   *
   * //  { object } object - the initial object or array
   * //  { (v: any, k?: string, o?: any, p?: any) => any } function - iteratee function
   * //  { boolean = false } bottomUp - optional, set to TRUE to reverse direction
   * //  { object = object } rootObject - optional, root object or array
   * //  { string = '' } pointer - optional, JSON Pointer to object within rootObject
   * // { object } - The copied object
   */
  static forEachDeepCopy(
    object, fn: (v: any, p?: string, o?: any) => any = (v) => v,
    bottomUp = false, pointer = '', rootObject = object
  ) {
    if (typeof fn !== 'function') {
      console.error(`forEachDeepCopy error: Iterator is not a function:`, fn);
      return null;
    }
    if (isObject(object) || isArray(object)) {
      let newObject = isArray(object) ? [ ...object ] : { ...object };
      if (!bottomUp) { newObject = fn(newObject, pointer, rootObject); }
      for (const key of Object.keys(newObject)) {
        const newPointer = pointer + '/' + this.escape(key);
        newObject[key] = this.forEachDeepCopy(
          newObject[key], fn, bottomUp, newPointer, rootObject
        );
      }
      if (bottomUp) { newObject = fn(newObject, pointer, rootObject); }
      return newObject;
    } else {
      return fn(object, pointer, rootObject);
    }
  }

  /**
   * 'isJsonPointer' function
   *
   * Checks a string or array value to determine if it is a valid JSON Pointer.
   * Returns true if a string is empty, or starts with '/' or '#/'.
   * Returns true if an array contains only string values.
   *
   * //   value - value to check
   * // { boolean } - true if value is a valid JSON Pointer, otherwise false
   */
  static isJsonPointer(value) {
    if (isArray(value)) {
      return value.every(key => typeof key === 'string');
    } else if (isString(value)) {
      if (value === '' || value === '#') { return true; }
      if (value[0] === '/' || value.slice(0, 2) === '#/') {
        return !/(~[^01]|~$)/g.test(value);
      }
    }
    return false;
  }

  /**
   * 'escape' function
   *
   * Escapes a string reference key
   *
   * //  { string } key - string key to escape
   * // { string } - escaped key
   */
  static escape(key) {
    const escaped = key.toString().replace(/~/g, '~0').replace(/\//g, '~1');
    return escaped;
  }

  /**
   * 'unescape' function
   *
   * Unescapes a string reference key
   *
   * //  { string } key - string key to unescape
   * // { string } - unescaped key
   */
  static unescape(key) {
    const unescaped = key.toString().replace(/~1/g, '/').replace(/~0/g, '~');
    return unescaped;
  }

  /**
   * 'parse' function
   *
   * Converts a string JSON Pointer into a array of keys
   * (if input is already an an array of keys, it is returned unchanged)
   *
   * //  { Pointer } pointer - JSON Pointer (string or array)
   * //  { boolean = false } errors - Show error if invalid pointer?
   * // { string[] } - JSON Pointer array of keys
   */
  static parse(pointer, errors = false) {
    if (!this.isJsonPointer(pointer)) {
      if (errors) { console.error(`parse error: Invalid JSON Pointer: ${pointer}`); }
      return null;
    }
    if (isArray(pointer)) { return <string[]>pointer; }
    if (typeof pointer === 'string') {
      if ((<string>pointer)[0] === '#') { pointer = pointer.slice(1); }
      if (<string>pointer === '' || <string>pointer === '/') { return []; }
      return (<string>pointer).slice(1).split('/').map(this.unescape);
    }
  }

    /**
   * 'toIndexedPointer' function
   *
   * Merges an array of numeric indexes and a generic pointer to create an
   * indexed pointer for a specific item.
   *
   * For example, merging the generic pointer '/foo/-/bar/-/baz' and
   * the array [4, 2] would result in the indexed pointer '/foo/4/bar/2/baz'
   *
   *
   * //  { Pointer } genericPointer - The generic pointer
   * //  { number[] } indexArray - The array of numeric indexes
   * //  { Map<string, number> } arrayMap - An optional array map
   * // { string } - The merged pointer with indexes
   */
  static toIndexedPointer(
    genericPointer, indexArray, arrayMap: Map<string, number> = null
  ) {
    if (this.isJsonPointer(genericPointer) && isArray(indexArray)) {
      let indexedPointer = this.compile(genericPointer);
      if (isMap(arrayMap)) {
        let arrayIndex = 0;
        return indexedPointer.replace(/\/\-(?=\/|$)/g, (key, stringIndex) =>
          arrayMap.has((<string>indexedPointer).slice(0, stringIndex)) ?
            '/' + indexArray[arrayIndex++] : key
        );
      } else {
        for (const pointerIndex of indexArray) {
          indexedPointer = indexedPointer.replace('/-', '/' + pointerIndex);
        }
        return indexedPointer;
      }
    }
    if (!this.isJsonPointer(genericPointer)) {
      console.error(`toIndexedPointer error: Invalid JSON Pointer: ${genericPointer}`);
    }
    if (!isArray(indexArray)) {
      console.error(`toIndexedPointer error: Invalid indexArray: ${indexArray}`);
    }
  }

    /**
   * 'compile' function
   *
   * Converts an array of keys into a JSON Pointer string
   * (if input is already a string, it is normalized and returned)
   *
   * The optional second parameter is a default which will replace any empty keys.
   *
   * //  { Pointer } pointer - JSON Pointer (string or array)
   * //  { string | number = '' } defaultValue - Default value
   * //  { boolean = false } errors - Show error if invalid pointer?
   * // { string } - JSON Pointer string
   */
  static compile(pointer, defaultValue = '', errors = false) {
    if (pointer === '#') { return ''; }
    if (!this.isJsonPointer(pointer)) {
      if (errors) { console.error(`compile error: Invalid JSON Pointer: ${pointer}`); }
      return null;
    }
    if (isArray(pointer)) {
      if ((<string[]>pointer).length === 0) { return ''; }
      return '/' + (<string[]>pointer).map(
        key => key === '' ? defaultValue : this.escape(key)
      ).join('/');
    }
    if (typeof pointer === 'string') {
      if (pointer[0] === '#') { pointer = pointer.slice(1); }
      return pointer;
    }
  }
}
