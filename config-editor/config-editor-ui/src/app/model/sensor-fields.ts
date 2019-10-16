export interface SensorFieldTemplate {
    sensor_template_fields: SensorFields[];
}

export interface SensorFields {
    fields: Field[];
    sensor_name: string;
}

export interface Field {
    name: string;
}
