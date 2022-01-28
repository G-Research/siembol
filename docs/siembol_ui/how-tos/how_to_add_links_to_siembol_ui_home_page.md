# How to add links to the siembol ui home page or the management page

## Siembol home page
The siembol home page has an 'Explore Siembol' section at the button of its home page, as can be seen in the screenshot below. It is used for quick access to useful resources such as documentation, ticket tracking systems etc. By default there is a link to the documentation and to the issues page on the git repo.

![image](../screenshots/home_page.png)

New links can be added to both from the `ui-config.json` config file. 
Below is the default config file provided where the two default links are in "homeLinks".

        {
            "environment": "demo",
            "serviceRoot": "https://config-editor/",
            "uiBootstrapPath": "./ui-bootstrap.json",
            "authType": "disabled",
            "homeLinks": [
                {
                    "icon": "library_books",
                    "title": "Documentation",
                    "link": "https://github.com/G-Research/siembol/tree/main/docs"
                },
                {   
                    "icon": "live_help",
                    "title": "Issues",
                    "link":  "https://github.com/G-Research/siembol/issues"}
                ]
        }



To add a new link you need three things:
- the url to which the user will be redirected on clicking the link
- the icon to be displayed; this has to be the name of a material icon (you can find them all here: https://material.io/)
- the title to display below the icon 

## Siembol management page

The siembol [management page](./how_to_use_the_management_page.md), which is only accessible by admins of any service, has a 'Management Links' section at the top of the page, similar to the links in the home page. This can be used to add links useful for just admins such as to monitoring dashboards. There are no links in the management page by default.

Links are added by the user in the `ui-config.json` file in the same way as for home links, but the key is "managementLinks". 
