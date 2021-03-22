# How to Setup OAUTH2 OIDC in Siembol UI
Siembol UI supports OAUTH2/OIDC authorisation which we are using at GR with the internal identity provider. In the future we are planning to test the implementation on major identity providers. 

## Scopes and Claims
You need to register siembol application in the identity provider and define:
- clients and withn the list of redirect urls for 
    - siembol UI 
    - swagger UI for config editor rest if needed
- claims
    - `openid` - Identifies the audience `aud` that is used for authentication by checking the value from the property `config-editor-auth.oauth2.audience`
    - `groups` - The groups are used for authorisation to a siembol service
    - `email` - This field is using for formatting commits in git repositories in order to preserve history of config changes realised by users of siembol ui

## Supported flows
Siembol currently supports only Authorization Code Flow with Proof Key for Code Exchange (PKCE).

## Config editor UI properties
- `authType` - This must be  `oauth2`
- `authAttributes` - The attributes for setting authentication attributes. We are using the library [oidc-client](https://www.npmjs.com/package/oidc-client)
    - `callbackPath` - Path of the call-back url registered in the identity provider        
    - `expiresIntervalMinimum` - Expiration time in seconds for token that is checked during the routing in the siembol ui application. If the token has lifetime less than this value than the application will be reloaded and token will be refreshed. We suggest to use the value `1800` e.g. 30 minutes.
    - `oidcSettings` - The settings of [oidc-client](https://www.npmjs.com/package/oidc-client)
        ``` 
            "authority": "the issuer url",
            "client_id": "the client registered in the provider"
            "redirect_uri": "the registered callback url",
            "response_type": "code",
            "scope": "openid",
            "automaticSilentRenew": false,
            "loadUserInfo": false
        ```

## Config editor rest application properties
- `config-editor-auth.type` - This value must be `oauth2`

- `config-editor-auth.oauth2.excluded-url-patterns` - The url patterns of config editor rest which are excluded for oauth2 authorisation and allows unauthenticated access. We suggest to include `/info,/health,/health/liveness,/health/readiness,/metrics,/v3/api-docs/**,/swagger-ui/**,/swagger-ui.html,/api/v1/sync/webhook**`

- `config-editor-auth.oauth2.audience` - The audience registered in the identity provider

- `config-editor-auth.oauth2.tokenUrl` - The url to the token endpoint of the identity provider

- `config-editor-auth.oauth2.authorizationUrl` - The url to the authorisation endpoint of the identity provider

- `config-editor-auth.oauth2.issuerUrl` - The url to the JWT issuer - the identity provider

- `config-editor-auth.oauth2.jwkSetUrl` - The url to the set of public keys of the issuer

- `config-editor-auth.oauth2.jwsAlgorithm` - We suggest to use `RS256` 

- `config-editor-auth.oauth2.jwtType` - We suggest to use `at+jwt`

- `config-editor-auth.oauth2.scopes` - We suggest to include `openid`

- swagger ui properties if needed 
    - `springdoc.pathsToMatch`- We suggest to include `/health,/info,/metrics,/api/**,/user`
    - `springdoc.show-actuator` - - We suggest to set `true`
    - `springdoc.swagger-ui.oauth.clientId` - The client name for swagger ui that is registered in the identity provider
    - `springdoc.swagger-ui.oauth.appName` - The audience `aud` registered in the identity provider
    - `springdoc.swagger-ui.oauth.clientSecret` - The Secret in PKCE flow is not confidential and could be provided in the properties
    - `springdoc.swagger-ui.oauth.usePkceWithAuthorizationCodeGrant` - This value must be `TRUE`

## Auhtorisation of services based on groups claim
You can find more details in [how_to_set_up_service](https://github.com/G-Research/siembol/blob/master/docs/services/how-tos/how_to_set_up_service_in_config_editor_rest.md) The access to the service is based on checking OIDC `group` scope membership.

#### Authorisation for service users
- `config-editor-auth.authorisation-groups.service-name` - The list of groups for accessing the service

#### Authorisation for service administrators
- `config-editor-auth.authorisation-admin-groups.service-name` - - The list of groups for accessing the admin configuration of the service.
