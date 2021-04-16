# How to contribute
## How to contribute to siembol java project

### Environment
- [Maven](https://maven.apache.org/guides/) - version `3.5+`
- [Java Development Kit 8](https://jdk.java.net/)

### How to compile and install
Run `mvn clean install -Dgpg.skip=true` to install the project locally 

Run `mvn clean package` to build and test the project. This command is recommended to execute before committing changes in the git repository.

Run `mvn clean package -DskipTests=true` to build the project but skip testing.
### How to solve dependencies conflicts

Run `mvn dependency:tree` to obtain the dependency trees that can help to resolve issues with conflict in dependencies.

### How to increase the version of modules
Increase versions in all submodules by running
```
mvn versions:set -DnewVersion=your_new_version
```
Increasing version is recommended for each PR with siembol java code change. Use version name with 'SNAPSHOT' if you are not preparing a release version.

## How to contribute to config editor UI project

Angular version can be found in [package.json](/config-editor/config-editor-ui/package.json) file.


### Build

Run `npm run build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `-prod` flag for a production build.

### Development server

Run `npm run start` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

### Running unit tests

Run `npm run test` to execute the unit tests via [Karma](https://karma-runner.github.io).

### Linting

Run `npm run lint` to lint all files or `npm run lint-fix` to lint all files and fix some linting errors.

### Package lock file
If you are changing dependencies in [package.json](/config-editor/config-editor-ui/package.json)  run `npm install` and  always update and commit dependencies in [package lock file](/config-editor/config-editor-ui/package-lock.json)

Use `nocontribute` author for committing the lock file in order not to include this file to contributor statistics
```
git add package-lock.json
git commit -m 'updating package lock file' --author='nocontribute <>'
```

### Increase config editor ui version
Config editor ui version should be increased after each code change in [package.json](/config-editor/config-editor-ui/package.json)
