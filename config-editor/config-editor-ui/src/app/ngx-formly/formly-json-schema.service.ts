import { AppConfigService } from '../config/app-config.service';

import { TitleCasePipe } from '@angular/common';
import { Injectable, OnDestroy } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { Store } from '@ngrx/store';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { ɵreverseDeepMerge as reverseDeepMerge } from '@ngx-formly/core';
import * as fromStore from 'app/store';
import { JSONSchema7, JSONSchema7TypeName } from 'json-schema';
import { cloneDeep } from 'lodash';
import { Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';

export interface FormlyJsonschemaOptions {
  /**
   * allows to intercept the mapping, taking the already mapped
   * formly field and the original JSONSchema source from which it
   * was mapped.
   */
  map?: (mappedField: FormlyFieldConfig, mapSource: JSONSchema7) => FormlyFieldConfig;
}

function isEmpty(v) {
  return v === '' || v === undefined || v === null;
}

interface IOptions extends FormlyJsonschemaOptions {
  schema: JSONSchema7;
}

@Injectable({ providedIn: 'root' })
export class FormlyJsonschema implements OnDestroy {
  private dynamicFieldsMap: Map<string, string>;
  private useTextarea = false;
  private ngUnsubscribe = new Subject();

  titleCasePipe: TitleCasePipe = new TitleCasePipe();

  constructor(private store: Store<fromStore.State>, private appConfig: AppConfigService) {
      this.store.select(fromStore.getServiceName).pipe(takeUntil(this.ngUnsubscribe)).subscribe(s =>
        this.useTextarea = this.appConfig.getUiMetadata(s).enableSensorFields
      );
  }

  toFieldConfig(schema: JSONSchema7, options?: FormlyJsonschemaOptions): FormlyFieldConfig {
    this.dynamicFieldsMap = new Map<string, string>();
    const fieldConfig = this._toFieldConfig(schema, { schema, ...(options || {}) }, []);
    this.store.dispatch(new fromStore.UpdateDynamicFieldsMap(this.dynamicFieldsMap));

    return fieldConfig;
  }

  private _toFieldConfig(schema: JSONSchema7, options: IOptions, propKey?: string[]): FormlyFieldConfig {
    if (schema.$ref) {
      schema = this.resolveDefinition(schema, options);
    }

    if (schema.allOf) {
      schema = this.resolveAllOf(schema, options);
    }

    let field: FormlyFieldConfig = {
      type: this.guessType(schema),
      defaultValue: schema.default,
      templateOptions: {
        label: schema.title ? this.titleCasePipe.transform(<string> schema.title.replace(/_/g, ' ')) : '',
        readonly: schema.readOnly,
        description: schema.description,
      },
    };

    switch (field.type) {
      case 'null': {
        this.addValidator(field, 'null', c => c.value === null);
        break;
      }
      case 'boolean': {
          field.templateOptions.label = this.titleCasePipe.transform(propKey[propKey.length - 1].replace(/_/g, ' '));
          field.templateOptions.description = schema.description;
          break;
      }
      case 'number':
      case 'integer': {
        field.templateOptions.label = this.titleCasePipe.transform(propKey[propKey.length - 1].replace(/_/g, ' '));
        field.parsers = [v => isEmpty(v) ? null : Number(v)];
        if (schema.hasOwnProperty('minimum')) {
          field.templateOptions.min = schema.minimum;
        }

        if (schema.hasOwnProperty('maximum')) {
          field.templateOptions.max = schema.maximum;
        }

        if (schema.hasOwnProperty('exclusiveMinimum')) {
          field.templateOptions.exclusiveMinimum = schema.exclusiveMinimum;
          this.addValidator(field, 'exclusiveMinimum', c => isEmpty(c.value) || (c.value > schema.exclusiveMinimum));
        }

        if (schema.hasOwnProperty('exclusiveMaximum')) {
          field.templateOptions.exclusiveMaximum = schema.exclusiveMaximum;
          this.addValidator(field, 'exclusiveMaximum', c => isEmpty(c.value) || (c.value < schema.exclusiveMaximum));
        }

        if (schema.hasOwnProperty('multipleOf')) {
          field.templateOptions.step = schema.multipleOf;
          this.addValidator(field, 'multipleOf', c => isEmpty(c.value) || (c.value % schema.multipleOf === 0));
        }
        break;
      }
      case 'string': {
        ['minLength', 'maxLength', 'pattern'].forEach(prop => {
          if (schema.hasOwnProperty(prop)) {
            field.templateOptions[prop] = schema[prop];
          }
        });
        if (this.useTextarea) {
            field.type = 'text-area';
        }
        field.templateOptions.label = propKey[propKey.length - 1] !== '-'
            ? this.titleCasePipe.transform(propKey[propKey.length - 1].replace(/_/g, ' '))
            : '';
        break;
      }
      case 'object': {
        field.fieldGroup = [];

        const [propDeps, schemaDeps] = this.resolveDependencies(schema);
        Object.keys(schema.properties || {}).forEach(key => {
          const newPropKey = cloneDeep(propKey);
          newPropKey.push(key);
          const f = this._toFieldConfig(<JSONSchema7> schema.properties[key], options, newPropKey);
          field.fieldGroup.push(f);
          f.key = key;
          if (Array.isArray(schema.required) && schema.required.indexOf(key) !== -1) {
            f.templateOptions.required = true;
          }
          if (!f.templateOptions.required && propDeps[key]) {
            f.expressionProperties = {
              'templateOptions.required': m => m && propDeps[key].some(k => !isEmpty(m[k])),
            };
          }

          if (schemaDeps[key]) {
            field.fieldGroup.push({
              ...this._toFieldConfig(schemaDeps[key], options),
              hideExpression: m => !m || isEmpty(m[key]),
            });
          }
        });

        break;
      }
      case 'array': {
        field.fieldGroup = [];
        field.templateOptions.label = this.titleCasePipe.transform(propKey[propKey.length - 1].replace(/_/g, ' '));
        const newPropKey2 = cloneDeep(propKey);

        if (schema.hasOwnProperty('minItems')) {
          field.templateOptions.minItems = schema.minItems;
          this.addValidator(field, 'minItems', c => isEmpty(c.value) || (c.value.length >= schema.minItems));
        }
        if (schema.hasOwnProperty('maxItems')) {
          field.templateOptions.maxItems = schema.maxItems;
          this.addValidator(field, 'maxItems', c => isEmpty(c.value) || (c.value.length <= schema.maxItems));
        }

        Object.defineProperty(field, 'fieldArray', {
          get: () => {
            if (!Array.isArray(schema.items)) {
              // When items is a single schema, the additionalItems keyword is meaningless, and it should not be used.
              if (newPropKey2[newPropKey2.length - 1] !== '-') {
                  newPropKey2.push('-');
              }

              return this._toFieldConfig(<JSONSchema7> schema.items, options, newPropKey2);
            }

            const itemSchema = schema.items[field.fieldGroup.length]
              ? schema.items[field.fieldGroup.length]
              : schema.additionalItems;

            return itemSchema
              ? this._toFieldConfig(<JSONSchema7> itemSchema, options)
              : null;
          },
          enumerable: true,
          configurable: true,
        });
        break;
      }
    }

    if (schema.hasOwnProperty('x-schema-form')) {
        if (schema['x-schema-form'].hasOwnProperty('type')) {
            field.type = schema['x-schema-form'].type;
        }
        if (schema['x-schema-form'].hasOwnProperty('wrappers')) {
            field.wrappers = schema['x-schema-form'].wrappers;
        } else if (field.type === 'object') {
            field.wrappers = ['panel']
        }
        if (schema['x-schema-form'].hasOwnProperty('condition')) {
            if (schema['x-schema-form'].condition.hasOwnProperty('hideExpression')) {
                try {
                    const dynFunc: Function =
                        new Function('model', 'localFields', 'field', schema['x-schema-form'].condition.hideExpression);
                    field.hideExpression = (model, formState, f) => dynFunc(formState.mainModel, model, f);
                } catch {
                    console.warn('Something went wrong with applying condition evaluation to form');
                }
                this.dynamicFieldsMap.set(('/' + propKey.join('/')), schema['x-schema-form'].condition.hideExpression);
            }
        }
    }

    if (schema.enum) {
      field.type = 'enum';
      field.templateOptions.options = schema.enum.map(value => ({ value, label: value }));
    }

    // map in possible formlyConfig options from the widget property
    if (schema['widget'] && schema['widget'].formlyConfig) {
      field = reverseDeepMerge(schema['widget'].formlyConfig, field);
    }

    // if there is a map function passed in, use it to allow the user to
    // further customize how fields are being mapped
    return options.map ? options.map(field, schema) : field;
  }

  private resolveAllOf({ allOf, ...baseSchema }: JSONSchema7, options: IOptions) {
    if (!allOf.length) {
      throw Error(`allOf array can not be empty ${allOf}.`);
    }

    return allOf.reduce((base: JSONSchema7, schema: JSONSchema7) => {
      if (schema.$ref) {
        schema = this.resolveDefinition(schema, options);
      }

      if (schema.allOf) {
        schema = this.resolveAllOf(schema, options);
      }
      if (base.required && schema.required) {
        base.required = [...base.required, ...schema.required];
      }

      if (schema.uniqueItems) {
        base.uniqueItems = schema.uniqueItems;
      }

      // resolve to min value
      ['maxLength', 'maximum', 'exclusiveMaximum', 'maxItems', 'maxProperties']
        .forEach(prop => {
          if (!isEmpty(base[prop]) && !isEmpty(schema[prop])) {
            base[prop] = base[prop] < schema[prop] ? base[prop] : schema[prop];
          }
        });

      // resolve to max value
      ['minLength', 'minimum', 'exclusiveMinimum', 'minItems', 'minProperties']
        .forEach(prop => {
          if (!isEmpty(base[prop]) && !isEmpty(schema[prop])) {
            base[prop] = base[prop] > schema[prop] ? base[prop] : schema[prop];
          }
        });

      return reverseDeepMerge(base, schema);
    }, baseSchema);
  }

  private resolveDefinition(schema: JSONSchema7, options: IOptions): JSONSchema7 {
    const [uri, pointer] = schema.$ref.split('#/');
    if (uri) {
      throw Error(`Remote schemas for ${schema.$ref} not supported yet.`);
    }

    const definition = !pointer ? null : pointer.split('/').reduce(
      (def, path) => def && def.hasOwnProperty(path) ? def[path] : null,
      options.schema
    );

    if (!definition) {
      throw Error(`Cannot find a definition for ${schema.$ref}.`);
    }

    if (definition.$ref) {
      return this.resolveDefinition(definition, options);
    }

    return {
      ...definition,
      ...['title', 'description', 'default'].reduce((annotation, p) => {
        if (schema.hasOwnProperty(p)) {
          annotation[p] = schema[p];
        }

        return annotation;
      }, {}),
    };
  }

  private resolveDependencies(schema: JSONSchema7) {
    const deps = {};
    const schemaDeps = {};

    Object.keys(schema.dependencies || {}).forEach(prop => {
      const dependency = schema.dependencies[prop] as JSONSchema7;
      if (Array.isArray(dependency)) {
        // Property dependencies
        dependency.forEach(dep => {
          if (!deps[dep]) {
            deps[dep] = [prop];
          } else {
            deps[dep].push(prop);
          }
        });
      } else {
        // schema dependencies
        schemaDeps[prop] = dependency;
      }
    });

    return [deps, schemaDeps];
  }

  private guessType(schema: JSONSchema7) {
    const type = schema.type as JSONSchema7TypeName;
    if (!type && schema.properties) {
      return 'object';
    }

    return type;
  }

  private addValidator(field: FormlyFieldConfig, name: string, validator: (control: AbstractControl) => boolean) {
    field.validators = field.validators || {};
    field.validators[name] = validator;
  }

  ngOnDestroy() {
      this.ngUnsubscribe.next();
      this.ngUnsubscribe.complete();
  }
}
