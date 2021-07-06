import { ADMIN_VERSION_FIELD_NAME, UiMetadata } from '@model/ui-metadata-map';
import { cloneDeep } from 'lodash';
import { JSONSchema7 } from 'json-schema';
import * as omitEmpty from 'omit-empty';
import { AdminConfig, ConfigData } from '@app/model/config-model';
import { SchemaService } from './schema.service';
import { areJsonEqual } from '@app/commons/helper-functions';

export class AdminSchemaService extends SchemaService {
  private readonly _schema: JSONSchema7;
  private rawObjectsPaths: string[];
  private readonly SPECIAL_CHAR_TO_REPLACE_DOT = '___';

  constructor(protected uiMetadata: UiMetadata, protected user: string, protected originalSchema: JSONSchema7) {
    super(uiMetadata, user, originalSchema);
    this.rawObjectsPaths = [];
    this._schema = cloneDeep(this.originalSchema);
    this.wrapOptionalsInSchema(this._schema, '', '');
    this.formatTitlesInSchema(this._schema, '');
    this.wrapAdminSchema(this._schema, '');
    delete this._schema.properties[ADMIN_VERSION_FIELD_NAME];
  }

  get schema() {
    return this._schema;
  }

  wrapAdminSchema(obj: any, path: string) {
    if (obj.type === 'rawobject') {
      this.rawObjectsPaths.push(path);
    }
    if (obj.type === 'object') {
      const re = /\./g;
      if (typeof obj.properties === typeof {}) {
        const props = Object.keys(obj.properties);
        for (const property of props) {
          const newKey = property.replace(re, this.SPECIAL_CHAR_TO_REPLACE_DOT);
          this.wrapAdminSchema(obj.properties[property], path + '/' + newKey);
          if (newKey !== property) {
            obj.properties[newKey] = cloneDeep(obj.properties[property]);
            delete obj.properties[property];
          }
        }
        if (obj.hasOwnProperty('required')) {
          for (let i = 0; i < obj.required.length; i++) {
            obj.required[i] = obj.required[i].replace(re, this.SPECIAL_CHAR_TO_REPLACE_DOT);
          }
        }
      }
    }
    if (obj.type === 'array') {
      this.wrapAdminSchema(obj.items, path);
    }
  }

  formatAdminConfig(obj: any, path: string, re: RegExp, replace: string) {
    if (obj === undefined || obj === null || typeof obj !== typeof {}) {
      return;
    }
    if (this.rawObjectsPaths.includes(path)) {
      return;
    }
    for (const key of Object.keys(obj)) {
      const newKey = key.replace(re, replace);
      if (Object.prototype.toString.apply(obj[key]) === '[object Object]') {
        this.formatAdminConfig(obj[key], path + '/' + newKey, re, replace);
      } else if (Object.prototype.toString.apply(obj[key]) === '[object Array]') {
        for (const a of obj[key]) {
          this.formatAdminConfig(a, path + '/' + newKey, re, replace);
        }
      }
      if (newKey !== key) {
        obj[newKey] = cloneDeep(obj[key]);
        delete obj[key];
      }
    }
  }

  wrapAdminConfig(obj: any) {
    const re = /\./g;
    this.formatAdminConfig(obj, '', re, this.SPECIAL_CHAR_TO_REPLACE_DOT);
  }

  unwrapAdminConfig(config: AdminConfig): AdminConfig {
    if (config.configData) {
      config.configData[ADMIN_VERSION_FIELD_NAME] = config.version;
      config.configData = this.unwrapAdminConfigData(config.configData);
    }
    return config;
  }

  unwrapAdminConfigData(configData: ConfigData): ConfigData {
    const config = cloneDeep(configData);
    const re = new RegExp(this.SPECIAL_CHAR_TO_REPLACE_DOT, 'g');
    this.formatAdminConfig(config, '', re, '.');
    configData = this.produceOrderedJson(config, '/');
    return omitEmpty(config);
  }

  areConfigEqual(config1: any, config2: any) {
    return areJsonEqual(
      this.unwrapAdminConfig(config1),
      this.unwrapAdminConfig(config2)
    );
  }
}
