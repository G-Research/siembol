import { UiMetadata } from '@model/ui-metadata-map';
import { cloneDeep } from 'lodash';
import { ConfigData, Config } from '@app/model';
import { JSONSchema7 } from 'json-schema';
import * as omitEmpty from 'omit-empty';
import { SchemaService } from './schema.service';
import { areJsonEqual } from '@app/commons/helper-functions';

export class ConfigSchemaService extends SchemaService {
  private readonly _schema: JSONSchema7;

  constructor(protected uiMetadata: UiMetadata, protected user: string, protected originalSchema: JSONSchema7) {
    super(uiMetadata, user, originalSchema);

    //NOTE: we need to modify the schema to handle optionals and remove metadata
    this._schema = this.returnSubTree(this.originalSchema, this.uiMetadata.perConfigSchemaPath) as JSONSchema7;
    this.wrapOptionalsInSchema(this._schema, '', '');
    this.formatTitlesInSchema(this._schema, '');
    delete this._schema.properties[this.uiMetadata.name];
    delete this._schema.properties[this.uiMetadata.author];
    delete this._schema.properties[this.uiMetadata.version];
    this._schema.required = this._schema.required.filter(
      f => f !== this.uiMetadata.name && f !== this.uiMetadata.author && f !== this.uiMetadata.version
    );
  }

  get schema() {
    return this._schema;
  }

  createReleaseSchema(): JSONSchema7 {
    const depSchema = cloneDeep(this.originalSchema);
    depSchema.properties[this.uiMetadata.release.config_array] = {};
    delete depSchema.properties[this.uiMetadata.release.config_array];
    delete depSchema.properties[this.uiMetadata.release.version];
    depSchema.required = depSchema.required.filter(element => {
      if (element !== this.uiMetadata.release.version && element !== this.uiMetadata.release.config_array) {
        return true;
      }

      return false;
    });

    return depSchema;
  }

  cleanConfig(config: Config): Config {
    config = cloneDeep(config);
    config.configData = this.unwrapConfig(config.configData);
    if (config.isNew) {
      config.configData[this.uiMetadata.name] = config.name;
      config.configData[this.uiMetadata.version] = config.version = 0;
      config.configData[this.uiMetadata.author] = config.author = this.user;
    } else {
      config.configData[this.uiMetadata.name] = config.name;
      config.configData[this.uiMetadata.version] = config.version;
      config.configData[this.uiMetadata.author] = config.author;
    }

    config.description = config.configData[this.uiMetadata.description];
    config.configData = this.cleanConfigData(config.configData);
    return config;
  }

  areTestCasesEqual(config1, config2): boolean {
    return areJsonEqual(omitEmpty(config1), omitEmpty(config2));
  }

  private cleanConfigData(configData: ConfigData): ConfigData {
    let cfg = this.produceOrderedJson(configData, '/');
    cfg = omitEmpty(cfg);
    return cfg;
  }
}