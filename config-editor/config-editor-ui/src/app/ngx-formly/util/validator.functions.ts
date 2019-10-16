import { AbstractControl } from '@angular/forms';

/**
 * Validator utility function library:
 *
 * Validator and error utilities:
 *   _executeValidators, _executeAsyncValidators, _mergeObjects, _mergeErrors
 *
 * Individual value checking:
 *   isDefined, hasValue, isEmpty
 *
 * Individual type checking:
 *   isString, isNumber, isInteger, isBoolean, isFunction, isObject, isArray,
 *   isMap, isSet, isPromise, isObservable
 *
 * Multiple type checking and fixing:
 *   getType, isType, isPrimitive, toJavaScriptType, toSchemaType,
 *   _toPromise, toObservable
 *
 * Utility functions:
 *   inArray, xor
 *
 * Typescript types and interfaces:
 *   SchemaPrimitiveType, SchemaType, JavaScriptPrimitiveType, JavaScriptType,
 *   PrimitiveValue, PlainObject, IValidatorFn, AsyncIValidatorFn
 *
 * Note: 'IValidatorFn' is short for 'invertable validator function',
 *   which is a validator functions that accepts an optional second
 *   argument which, if set to TRUE, causes the validator to perform
 *   the opposite of its original function.
 */

export type SchemaPrimitiveType =
  'string' | 'number' | 'integer' | 'boolean' | 'null';
export type SchemaType =
  'string' | 'number' | 'integer' | 'boolean' | 'null' | 'object' | 'array';
export type JavaScriptPrimitiveType =
  'string' | 'number' | 'boolean' | 'null' | 'undefined';
export type JavaScriptType =
  'string' | 'number' | 'boolean' | 'null' | 'undefined' | 'object' | 'array' |
  'map' | 'set' | 'arguments' | 'date' | 'error' | 'function' | 'json' |
  'math' | 'regexp'; // Note: this list is incomplete
export type PrimitiveValue = string | number | boolean | null | undefined;
export interface PlainObject { [k: string]: any; }

export type IValidatorFn = (c: AbstractControl, i?: boolean) => PlainObject;
export type AsyncIValidatorFn = (c: AbstractControl, i?: boolean) => any;


export function isObject(item: any): boolean {
    return item !== null && typeof item === 'object' &&
      Object.prototype.toString.call(item) === '[object Object]';
}
  
export function isArray(item: any): boolean {
    return Array.isArray(item) ||
        Object.prototype.toString.call(item) === '[object Array]';
}

export function isMap(item: any): boolean {
    return typeof item === 'object' &&
        Object.prototype.toString.call(item) === '[object Map]';
}

export function isString(value) {
    return typeof value === 'string';
}

export function isDefined(value) {
    return value !== undefined && value !== null;
}

export function isSet(item: any): boolean {
    return typeof item === 'object' &&
      Object.prototype.toString.call(item) === '[object Set]';
}