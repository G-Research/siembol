import { TitleCasePipe } from '@angular/common';
import { UiMetadata } from '@model/ui-metadata-map';
import { cloneDeep } from 'lodash';
import { ConfigData } from '@app/model';
import { JSONSchema7 } from 'json-schema';
import { FormlyFieldConfig } from '@ngx-formly/core';

export class SchemaService {
  titleCasePipe: TitleCasePipe = new TitleCasePipe();
  protected unionPath: string;
  protected selectorName: string;
  private optionalObjects: string[] = [];
  private modelOrder = {};

  constructor(protected uiMetadata: UiMetadata, protected user: string, protected originalSchema: JSONSchema7) {}

  static renameDescription(field: FormlyFieldConfig, schema: JSONSchema7) {
    if (
      schema.hasOwnProperty('description') &&
      typeof schema['type'] === 'string' &&
      ['string', 'textarea', 'number', 'boolean', 'integer'].includes(schema['type'])
    ) {
      field.templateOptions['hintEnd'] = schema.description;
      delete field.templateOptions['description'];
    }
    return field;
  }

  wrapConfig(obj: any): any {
    const ret = cloneDeep(obj);
    this.wrapOptionalsInArray(ret);
    if (this.unionPath && Object.keys(ret).length !== 0) {
      this.wrapUnionConfig(ret, this.unionPath);
    }
    return ret;
  }

  unwrapConfig(obj: any): any {
    let returnObject = cloneDeep(obj);
    if (this.unionPath) {
      returnObject = this.unwrapConfigFromUnion(returnObject, this.unionPath);
    }
    return this.unwrapOptionalsFromArrays(returnObject);
  }

  formatTitlesInSchema(obj: any, propKey?: string): any {
    if (obj === undefined || obj === null || typeof obj !== typeof {}) {
      return;
    }

    obj.title = obj.title ? obj.title : propKey;
    obj.title = this.titleCasePipe.transform(obj.title.replace(/_/g, ' '));

    if (obj.type === 'object') {
      if (obj.properties === undefined && !obj.hasOwnProperty('oneOf')) {
        obj.type = 'rawobject';
        obj.title = this.titleCasePipe.transform(propKey.replace(/\./g, ' '));
      } else if (typeof obj.properties === typeof {}) {
        const props = Object.keys(obj.properties);
        for (const property of props) {
          this.formatTitlesInSchema(obj.properties[property], property);
        }
      }
    } else if (obj.type === 'array') {
      if (obj.items.hasOwnProperty('oneOf')) {
        const objs = obj.items.oneOf;
        for (const item of objs) {
          this.formatTitlesInSchema(item, '');
        }
      }
      if (obj.items.type === 'object') {
        this.formatTitlesInSchema(obj.items, propKey);
      }
    } else if (obj.type === undefined && !obj.hasOwnProperty('properties')) {
      for (const key of Object.keys(obj)) {
        this.formatTitlesInSchema(obj[key], key);
      }
    }
  }

  protected returnSubTree(tree, path: string): any {
    let subtree = cloneDeep(tree);
    path.split('.').forEach(node => {
      subtree = subtree[node];
    });

    return subtree;
  }

  // function to go through the output json and reorder the properties such that it is consistent with the schema
  protected produceOrderedJson(configData: ConfigData, path: string) {
    if (configData && this.modelOrder[path]) {
      const currentCfg = cloneDeep(configData);
      configData = {};
      for (const key of this.modelOrder[path]) {
        configData[key] = currentCfg[key];
        const searchPath = path === '/' ? path + key : path + '/' + key;
        // ensure it has children
        if (typeof configData[key] === typeof {} && this.modelOrder[searchPath] !== undefined) {
          if (configData[key].length === undefined) {
            // is an object
            const tempCopy = cloneDeep(configData[key]);
            configData[key] = {};
            const tmpObj = {};
            for (const orderedKey of this.modelOrder[searchPath]) {
              if (tempCopy[orderedKey] !== undefined) {
                tmpObj[orderedKey] = tempCopy[orderedKey];
              }
            }
            configData[key] = tmpObj;
            configData[key] = this.produceOrderedJson(configData[key], searchPath);
          } else {
            // is an array
            const tmp = cloneDeep(configData[key]);
            configData[key] = [];
            for (const data of tmp) {
              configData[key].push(this.produceOrderedJson(data, searchPath));
            }
          }
        }
      }
    }

    return configData;
  }

  protected wrapOptionalsInSchema(obj: any, propKey?: string, path?: string): any {
    if (obj === undefined || obj === null || typeof obj !== typeof {}) {
      return;
    }
    if (obj.type === 'object' && typeof obj.properties === typeof {}) {
      path = path.endsWith('/') ? path + propKey : path + '/' + propKey;
      const requiredProperties = obj.required || [];
      const props = Object.keys(obj.properties);
      this.modelOrder[path] = props;
      for (const property of props) {
        const thingy = obj.properties[property];
        const isRequired = requiredProperties.includes(property);
        const isObject = thingy.type === 'object';
        if (!isRequired && isObject) {
          this.optionalObjects.push(property);
          if (thingy.default) {
            delete thingy.default;
          }
          const sub = { ...thingy };
          thingy.type = 'array';
          delete thingy.title;
          delete thingy.required;
          delete thingy.properties;
          delete thingy.description;
          delete sub.widget;

          thingy.items = sub;
          thingy.maxItems = 1;
          this.wrapOptionalsInSchema(thingy.items, property, path);
        } else {
          this.wrapOptionalsInSchema(thingy, property, path);
        }
      }
    } else if (obj.type === 'array') {
      path = path === '/' ? path : path + '/';
      if (obj.items.hasOwnProperty('oneOf')) {
        this.wrapSchemaUnion(obj.items.oneOf);
        this.unionPath = path + propKey;
      }
      if (obj.items.type === 'object') {
        this.wrapOptionalsInSchema(obj.items, propKey, path);
      }
    } else if (obj.type === undefined && !obj.hasOwnProperty('properties')) {
      path = path === '/' ? path + propKey : path + '/' + propKey;
      for (const key of Object.keys(obj)) {
        this.wrapOptionalsInSchema(obj[key], key, path);
      }
    }
  }

  private wrapUnionConfig(obj, oneOfPath: string) {
    const path = oneOfPath.split('/').filter(f => f !== '');
    let sub = obj;
    for (const part of path) {
      sub = sub[part];
    }
    for (let i = 0; i < sub.length; i++) {
      const temp = sub[i];
      sub[i] = { [sub[i][this.selectorName]]: temp };
    }
  }

  private wrapOptionalsInArray(obj: any) {
    for (const optional of this.optionalObjects) {
      this.findAndWrap(obj, optional);
    }
  }

  private findAndWrap(obj: any, optionalKey: string) {
    if (typeof obj === typeof {}) {
      for (const key of Object.keys(obj)) {
        if (key === optionalKey) {
          obj[key] = [obj[key]];

          return;
        }
        this.findAndWrap(obj[key], optionalKey);
      }
    }
  }

  private unwrapConfigFromUnion(obj, oneOfPath: string): any {
    const path = oneOfPath.split('/').filter(f => f !== '');
    let sub = obj;
    for (const part of path) {
      sub = sub[part];
    }
    if (sub) {
      for (let i = 0; i < sub.length; i++) {
        const keys = Object.keys(sub[i]);
        const temp = sub[i][keys[0]];
        sub[i][keys[0]] = undefined;
        sub[i] = { ...temp };
      }
    }
    return obj;
  }

  private wrapSchemaUnion(obj: any): any {
    for (const item of obj) {
      const temp = item.properties;

      const required = item.required;
      item.properties = { [item.title]: { properties: temp, required, type: 'object' } };
      item.required = [item.title];
    }
  }

  private unwrapOptionalsFromArrays(obj: any) {
    if (obj === undefined || obj === null || typeof obj !== typeof {}) {
      return obj;
    }

    for (const key of Object.keys(obj)) {
      if (this.optionalObjects.includes(key)) {
        obj[key] = obj[key] === [] || obj[key] === undefined || obj[key] === null ? undefined : obj[key][0];
      }
    }
    for (const key of Object.keys(obj)) {
      this.unwrapOptionalsFromArrays(obj[key]);
    }

    return obj;
  }
}
