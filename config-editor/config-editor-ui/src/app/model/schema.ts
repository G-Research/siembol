export class SchemaDto {
    schema: Schema;
}

export interface Schema {
    description: string;
    properties: any;
    required: string[];
    title: string;
    type: string;
}
