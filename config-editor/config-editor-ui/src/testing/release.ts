import { mockParserConfigData } from './configs';


export const mockReleaseFiles =
{
    files: [
        {
            content: {
                parsers_configurations: [
                    mockParserConfigData,
                ],
                parsers_version: 1,
            },
            file_history: [
                {
                    added: 40,
                    author: 'siembol',
                    date: '2021-02-19T13:32:17',
                    removed: 4,
                },
                {
                    added: 1,
                    author: 'siembol',
                    date: '2021-01-07T15:47:57',
                    removed: 1,
                },
            ],
            file_name: 'parsers.json',
        },
    ],
    rules_version: 1,
};
