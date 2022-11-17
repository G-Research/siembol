How to Contribute
=================

- [1. Contribute to the siembol Java project](#contribute-to-the-siembol-java-project)
    * [1.1 Environment](#environment)
    * [1.2 Compile and install](#compile-and-install)
    * [1.3 Solving dependencies conflicts](#solving-dependencies-conflicts)
    * [1.4 Increasing the version of submodules](#increasing-the-version-of-submodules)
- [2. Contribute to config editor UI project](#contribute-to-config-editor-ui-project)
    * [2.1 NodeJS version](#nodejs-version)
    * [2.2 Angular version](#angular-version)
    * [2.3 Build](#build)
    * [2.4 Development server](#development-server)
    * [2.5 Running unit tests](#running-unit-tests)
    * [2.6 Linting](#linting)
    * [2.7 Package lock file](#package-lock-file)
    * [2.8 Increasing the config editor's UI version](#increasing-the-config-editors-ui-version)

Contribute to the siembol Java project
---------------------------------------------

### Environment

- [Maven](https://maven.apache.org/guides/) - version `3.5+`
- [Java Development Kit 17+](https://adoptopenjdk.net/)

### Compile and install

To install the project locally:

```shell
mvn clean install -Dgpg.skip=true
```

To build and test the project:

```shell
mvn clean package
```

> **_note:_** We recommend that you execute this command before committing changes in the git repository.

To build the project but skip testing:

```shell
mvn clean package -DskipTests=true
```

### Solving dependencies conflicts

To obtain the dependency trees that can help to resolve issues with conflict in dependencies

```shell
mvn dependency:tree
```

### Increasing the version of submodules

To increment versions in all submodules:

```shell
mvn versions:set -DnewVersion=your_new_version
```

> **_note:_** Incrementing the version is recommended for each PR with siembol java code change. Use version name with `SNAPSHOT` if you are not preparing a release version.


Contribute to config editor UI project
---------------------------------------

### NodeJS version

The current NodeJS version used can be found in the github actions pipeline [here](https://github.com/G-Research/siembol/blob/main/.github/workflows/ci.yml#L215).
### Angular version

The current Angular version can be found in the [package.json](/config-editor/config-editor-ui/package.json) file.


### Build

To build the project:

```shell
npm run build
```

The build artifacts will be stored in the `dist/` directory. Use the `-prod` flag for a production build.

### Development server

To start a development server, run:

```shell
npm run start
``` 

Navigate to [`http://localhost:4200`](http://localhost:4200) to view the UI.

The app will automatically reload if you change any of the source files.

### Running unit tests

 to execute the unit tests via [Karma](https://karma-runner.github.io), run:

```shell
npm run test
```

### Linting

To lint all files:

```shell
npm run lint
```

To lint all files and fix linting errors:

```shell
npm run lint-fix
``` 

### Package lock file

To change the dependencies listed in [package.json](/config-editor/config-editor-ui/package.json), run:

```shell
npm install
``` 

Always update and commit dependencies in the [package lock file](/config-editor/config-editor-ui/package-lock.json)

> **_note:_** Please compress the package-lock.json file into one line so it does not affect the contributor statistics. Powershell example:

```shell
Get-Content package-lock.json | ConvertFrom-Json | ConvertTo-Json -compress -Depth 10 | Out-File package-lock2.json -Encoding ascii ; rm package-lock.json ; mv package-lock2.json package-lock.json
```

### Increasing the config editor's UI version

The config editor's UI version should be increased after each code change in [package.json](/config-editor/config-editor-ui/package.json).
