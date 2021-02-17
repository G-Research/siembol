import { ADMIN_VERSION_FIELD_NAME, UiMetadataMap } from '@model/ui-metadata-map';
import { cloneDeep } from 'lodash';
import { JSONSchema7 } from 'json-schema';
import * as omitEmpty from 'omit-empty';
import { AdminConfig } from '@app/model/config-model';
import { SchemaService } from './schema.service';

export class AdminSchemaService extends SchemaService {
    private readonly _schema: JSONSchema7;
    private rawObjectsPaths: string[];
    private readonly SPECIAL_CHAR_TO_REPLACE_DOT = "___";

    constructor(protected uiMetadata: UiMetadataMap, protected user: string, protected originalSchema: JSONSchema7) {
        super(uiMetadata, user, originalSchema);
        this.rawObjectsPaths = [];
        this._schema = cloneDeep(this.originalSchema);
        this.wrapOptionalsInSchema(this._schema, '', '');
        this.formatTitlesInSchema(this._schema, '');
        this.wrapAdminSchema(this._schema, '');
        delete this._schema.properties[ADMIN_VERSION_FIELD_NAME];
    }

    public get schema() { return this._schema; }

    public wrapAdminSchema(obj: any, path: string) {
        if (obj.type === "rawobject") {
            this.rawObjectsPaths.push(path);
        }
        if (obj.type === "object") {
            var re = /\./g;
            if (typeof (obj.properties) === typeof ({})) {
                const props = Object.keys(obj.properties);
                for (const property of props) {
                    const newKey = property.replace(re, this.SPECIAL_CHAR_TO_REPLACE_DOT);
                    this.wrapAdminSchema(obj.properties[property], path + '/' + newKey);
                    if (newKey != property) {
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
        if (obj.type === "array") {
            this.wrapAdminSchema(obj.items, path);
        }
    }

    public formatAdminConfig(obj: any, path: string, re: RegExp, replace: string) {
        if (obj === undefined || obj === null || typeof (obj) !== typeof ({})) {
            return;
        }
        if (this.rawObjectsPaths.includes(path)) {
            return;
        }
        for (const key of Object.keys(obj)) {
            const newKey = key.replace(re, replace);
            if (Object.prototype.toString.apply(obj[key]) === '[object Object]') {
                this.formatAdminConfig(obj[key], path + "/" + newKey, re, replace);
            } else if (Object.prototype.toString.apply(obj[key]) === '[object Array]') {
                for (let a of obj[key]) {
                    this.formatAdminConfig(a, path + "/" + newKey, re, replace);
                }
            }
            if (newKey != key) {
                obj[newKey] = cloneDeep(obj[key]);
                delete obj[key];
            }
            
        }
    }

    public wrapAdminConfig(obj: any) {
        var re = /\./g;
        this.formatAdminConfig(obj, '', re, this.SPECIAL_CHAR_TO_REPLACE_DOT);
    }

    public unwrapAdminConfig(config: AdminConfig): AdminConfig {
        var re = new RegExp(this.SPECIAL_CHAR_TO_REPLACE_DOT, "g");
        this.formatAdminConfig(config.configData, '', re, '\.');
        config.configData[ADMIN_VERSION_FIELD_NAME] = config.version;
        config.configData = this.produceOrderedJson(config.configData, '/');
        config.configData = omitEmpty(config.configData);
        return config;
    }

}