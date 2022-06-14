# How to set-up an enrichment table
## The structure of an enrichment table
An enrichment table is defined in a JSON file, it only supports mappings of `string` type. The table file contains key-value pairs where the key is used for joining a table with an event and the value is a flat JSON mapping of string to string. The example table below could be used to enrich usernames with their teams and full names:

        {
            "janed": { "team": "Engineering", "full_name": "Jane Doe"},
            "johns": { "team": "HR", "full_name": "John Smith"}
        }

## Table Metadata
Each enrichment table after loading into the enrichment application contains a virtual field `table_metadata`. This field provides information about the table that can be used in enrichment rules. 
Supported fields:
- `last_update` - A timestamp of the table update - an Epoch time in milliseconds
- `number_of_rows` - The number of rows (keys) of the table
- `number_of_fields` - The number of unique fields (columns) of the table 
- `number_of_values` - The number of values of the table
- `table_size` - The size of the table in bytes

## The structure of zookeeper update message
After updating an enrichment table it is necessary to inform the enrichment topology of the changes, this is done using Zookeeper. The Zookeeper node is configured in the admin config of enrichment ([see here](../siembol_enrichment_service.md)). This message is stored in a Zookeeper node in JSON format. In it the tables are in a list in the "hdfs_tables" key and each table has a name and the path to the latest table. Here is an example with two tables:

        {
            "enrichment_tables":[
                {
                    "name":"employees",
                    "path":"/siembol-enrichment/employees/1.json"
                },
                {
                    "name":"dns",
                    "path":"/siembol-enrichment/dns/1.json"
                }
            ],
            "build_number":"1"
        }
