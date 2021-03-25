# How to write a response plugin
 We suggest to implement a siembol response plugin as a springboot project that is compiled into a shaded jar file. The plugin is loaded by siembol response using the [springboot properties launcher](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-executable-jar-format.html) The plugin is also integrated into siembol UI and its evaluators can be used in the similar way as the ones provided directly by the siembol response. 
 
 The plugin needs to implement and initiate a `Bean` of the type `ResponsePlugin`. This type extends [`org.springframework.plugin.core.Plugin`](https://github.com/spring-projects/spring-plugin)
## Prepare a maven project with siembol dependencies
- From [siembol POM file](/pom.xml) obtain the versions of
    - siembol
    - springboot
    - other plugin dependencies that are also used in siembol response. If you want to use different versions of dependencies please relocate them in the maven-shade-plugin described bellow

- Add springboot dependency
```
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring_boot_version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
- Add siembol dependencies and other dependencies as required
```
<dependency>
    <groupId>uk.co.gresearch.siembol</groupId>
    <artifactId>siembol-common</artifactId>
    <version>${siembol.version}</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>uk.co.gresearch.siembol</groupId>
    <artifactId>responding-core</artifactId>
    <version>${siembol.version}</version>
    <scope>provided</scope>
</dependency>
```
- Build a project using [maven-shade-plugin](https://maven.apache.org/plugins/maven-shade-plugin/) where you can use filters, relocations and exlusions if needed
```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>${shade_version}</version>
    <configuration>
        <createDependencyReducedPom>true</createDependencyReducedPom>
        <artifactSet>
            <excludes>
            </excludes>
        </artifactSet>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <shadedArtifactAttached>false</shadedArtifactAttached>
                <shadedClassifierName>uber</shadedClassifierName>
                <filters>
                </filters>
                <relocations>
                </relocations>
                <artifactSet>
                    <excludes>
                        <exclude>org.apache.tomcat.*</exclude>
                    </excludes>
                </artifactSet>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Implement a responding evaluator factory
A responding evaluator factory:
- creates evaluator instances
- provides metadata about the evaluator such as its attributes and the type. The attributes json schema is used for integration with siembol UI. We suggest to use tools from siembol common package for generating the json schema from Java Dto classes.
- validates evaluator attributes

We suggest to look at implementation of some provided evaluators in siembol response such as [TableFormatterEvaluatorFactory](/responding/responding-core/src/main/java/uk/co/gresearch/siembol/response/evaluators/markdowntable/TableFormatterEvaluatorFactory.java)

#### RespondingEvaluatorFactory interface
```
public interface RespondingEvaluatorFactory {
    /**
     * Create an evaluator instance from json attributes
     *
     * @param attributes json attributes used for creating evaluator instance
     * @return RespondingResult with OK status code and evaluator in attributes or ERROR status code with message otherwise
     */
    RespondingResult createInstance(String attributes);

    /**
     * Get type of the evaluator including name. This name should be unique between factories.
     *
     * @return RespondingResult with evaluatorType in attributes or ERROR status code with message otherwise
     */
    RespondingResult getType();

    /**
     * Get json schema of evaluator attributes
     *
     * @return RespondingResult with attributesSchema in attributes or ERROR status code with message otherwise
     */
    RespondingResult getAttributesJsonSchema();

    /**
     * Validate evaluator attributes
     *
     * @return RespondingResult with OK status code if attributes are valid or ERROR status code with message otherwise
     */
    default RespondingResult validateAttributes(String attributes) {
        try {
            return createInstance(attributes);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    /**
     * Register metric factory that can be used for creating metrics
     *
     * @return RespondingResult with OK status code on success or ERROR status code with message otherwise
     */
    default RespondingResult registerMetrics(MetricFactory metricFactory) {
        return new RespondingResult(RespondingResult.StatusCode.OK, new RespondingResultAttributes());
    }
}
```

### Implement ResponsePlugin interface
```
/**
 * Plugin interface responding evaluator factories provided by the plugin
 */
public interface ResponsePlugin extends Plugin<String> {
    /**
     * Get responding evaluator factories provided by the plugin
     *
     * @return RespondingResult with OK status code and respondingEvaluatorFactories in attributes or
     *                               ERROR status code with message otherwise
     */
    RespondingResult getRespondingEvaluatorFactories();

    @Override
    default boolean supports(String str) {
        return true;
    }
}
```

## Plugin initialisation
You need to initiate Bean with your plugin implementation during loading the jar file by the springboot properties launcher. This can be achieved by initialisation of the plugin with dependencies in a class annotated with `@Configuration`.

```
Note: You can merge application properties of a plugin into the main application properties and use @ConfigurationProperties with prefix in order to distinguish the properties dedicated for the plugin
```