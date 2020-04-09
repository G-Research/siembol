import { UiMetadataMap } from '@model/ui-metadata-map';
import { cloneDeep } from 'lodash';
import { ConfigData, Deployment, ConfigWrapper } from '@app/model';

export class ConfigWrapperService {
    modelOrder = {};
    unionPath: string;
    optionalObjects: string[] = [];
    selectorName: string;

    constructor(private uiMetadata: UiMetadataMap) {
        if (uiMetadata.unionType) {
            this.unionPath = uiMetadata.unionType.unionPath ?? undefined;
            this.selectorName = uiMetadata.unionType.unionSelectorName ?? undefined;
        }
    }

// function to go through the output json and reorder the properties such that it is consistent with the schema
public produceOrderedJson(configData: ConfigData, path: string) {
    if (this.modelOrder[path]) {
        const currentCfg = cloneDeep(configData);
        configData = {};
        for (const key of this.modelOrder[path]) {
            configData[key] = currentCfg[key];
            const searchPath =  path === '/' ? path + key : path + '/' + key;
            // ensure it has children
            if (typeof(configData[key]) === typeof({}) && this.modelOrder[searchPath] !== undefined) {
                if (configData[key].length === undefined) {
                    // is an object
                    const tempCopy = cloneDeep(configData[key])
                    configData[key] = {};
                    const tmpObj = {}
                    for (const orderedKey of this.modelOrder[searchPath]) {
                        if (tempCopy[orderedKey] !== undefined) {
                            tmpObj[orderedKey] = tempCopy[orderedKey];
                        }
                    }
                    configData[key] = tmpObj;
                    configData[key] = this.produceOrderedJson(configData[key], searchPath)
                } else {
                    // is an array
                    const tmp = cloneDeep(configData[key]);
                    configData[key] = [];
                    for (let i = 0; i < tmp.length; ++i) {
                        configData[key].push(this.produceOrderedJson(tmp[i], searchPath));
                    }
                }
            }
        }
    }

    return configData;
  }

  public wrapOptionalsInSchema(obj: any, propKey?: string, path?: string): any {
    if (obj === undefined || obj === null || typeof (obj) !== typeof ({})) {
      return;
    }
    if (obj.type === 'object' && typeof(obj.properties) === typeof ({})) {
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
                const sub = {...thingy};
                thingy.type = 'array';
                delete thingy.required;
                delete thingy.properties;
                delete thingy.title;
                delete thingy.description;

                // tabs is not compatible with the array type so delete it if it is at the parent level but keep it on the sub level
                if (sub['x-schema-form'] !== undefined && sub['x-schema-form']['type'] !== 'tabs') {
                    delete sub['x-schema-form'];
                }
                if (thingy['x-schema-form'] !== undefined && thingy['x-schema-form']['type'] === 'tabs' && thingy['type'] === 'array') {
                    delete thingy['x-schema-form'];
                }
                // ***********************

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
    const path = oneOfPath.split("/").filter(f => f !== '');
    let sub = obj;
    for (const part of path) {
        sub = sub[part];
    }
    for (let i=0; i<sub.length; i++) {
        let temp = sub[i];
        sub[i] = {[sub[i][this.selectorName]]: temp}
    }
  }

  public wrapOptionalsInArray(obj: object) {
      for (const optional of this.optionalObjects) {
        this.findAndWrap(obj, optional);
    }

    return obj;
  }

  public wrapConfig(obj: object): object {
      let config = this.wrapOptionalsInArray(obj);
      if (this.unionPath) {
          this.wrapUnionConfig(config, this.unionPath);
      }
      return config;
  }

  private findAndWrap(obj: any, optionalKey: string) {
    if (typeof(obj) === typeof ({})) {
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
    const path = oneOfPath.split("/").filter(f => f !== '');
    let sub = obj;
    for (const part of path) {
        sub = sub[part];
    }
    for (let i = 0; i < sub.length; i++) {
        let keys = Object.keys(sub[i]);
        let temp = sub[i][keys[0]];
        sub[i][keys[0]] = undefined;
        sub[i] = {...temp};  
    }
    return obj;
  }

  private wrapSchemaUnion(obj: any, propKey?: string, path?: string): any {
      for (let i=0; i<obj.length; i++) {
          let temp = obj[i].properties;
          
          let required = obj[i].required;
          obj[i].properties = {[obj[i].title]: {type: 'object', properties: temp, required: required}}
          obj[i].required = [obj[i].title];
        }
  }

  public unwrapConfig(obj: object): object {
     if (this.unionPath) {
         obj = this.unwrapConfigFromUnion(obj, this.unionPath);
     }
     return this.unwrapOptionalsFromArrays(obj);
  }

  public unwrapOptionalsFromArrays(obj: any) {
    if (obj === undefined || obj === null || typeof (obj) !== typeof ({})) {
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

  public marshalDeploymentFormat(deployment: Deployment<ConfigWrapper<ConfigData>>): any {
    const d = cloneDeep(deployment);
    delete d.deploymentVersion;
    delete d.configs;

    return Object.assign(d, {
      [this.uiMetadata.deployment.version]: deployment.deploymentVersion,
      [this.uiMetadata.deployment.config_array]:
        deployment.configs.map(config => this.unwrapOptionalsFromArrays(cloneDeep(config.configData))),
    });
  }
}