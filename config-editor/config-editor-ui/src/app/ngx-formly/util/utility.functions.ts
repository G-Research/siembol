import {
    isArray,
    isMap,
    isObject,
    isSet,
} from './validator.functions';

/**
 * Utility function library:
 *
 * addClasses, copy, forEach, forEachCopy, hasOwn, mergeFilteredObject,
 * uniqueItems, commonItems, fixTitle, toTitleCase
*/

/**
 * 'hasOwn' utility function
 *
 * Checks whether an object or array has a particular property.
 *
 * // {any} object - the object to check
 * // {string} property - the property to look for
 * // {boolean} - true if object has property, false if not
 */
export function hasOwn(object: any, property: string): boolean {
    if (!object || !['number', 'string', 'symbol'].includes(typeof property) ||
        (!isObject(object) && !isArray(object) && !isMap(object) && !isSet(object))
    ) { return false; }
    if (isMap(object) || isSet(object)) { return object.has(property); }
    if (typeof property === 'number') {
        if (isArray(object)) { return object[<number>property]; }
        property = property + '';
    }
    return object.hasOwnProperty(property);
}
