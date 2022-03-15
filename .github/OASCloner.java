///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.eclipse.microprofile.openapi:microprofile-openapi-api:3.0
//DEPS org.openapitools.empoa:empoa-simple-models-impl:2.0.0
//DEPS org.openapitools.openapistylevalidator:openapi-style-validator-lib:1.7
//DEPS org.openapitools.empoa:empoa-swagger-core:2.0.0
//DEPS io.swagger.parser.v3:swagger-parser:2.0.30
//DEPS io.swagger:swagger-inflector:2.0.7
//DEPS org.slf4j:slf4j-jdk14:1.7.36
//DEPS com.fasterxml.jackson.core:jackson-annotations:2.13.1

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.util.function.Predicate.not;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.oas.inflector.examples.models.Example;
import io.swagger.oas.inflector.examples.ExampleBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.oas.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.util.Json;
import java.util.function.Function;

//jbang OASCloner.java folder2
public class OASCloner {

    public static void main(String... args) throws IOException {

        System.out.println("Process to clone OAS files in YAML format into JSON format");

        //Convert YAML to JSON
        //TODO 15/03/2022 Fix the issue with the converter
        Function<String, String> convert = (filePath) -> {
            OpenAPI swagger = new OpenAPIV3Parser().read(filePath);
            Map<String, Schema> definitions = swagger.getComponents().getSchemas();
            Schema model = definitions.get("pets");
            Example example = ExampleBuilder.fromSchema(model, definitions);
            SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
            Json.mapper().registerModule(simpleModule);
            String jsonExample = Json.pretty(example);
            System.out.println(jsonExample);

            return "Converted";
        };

        //Process
        var specDir = args[0];
        var userDirPath = new File(System.getProperty("user.dir"));
        var specPath = userDirPath.getParent() + "/" + specDir;
        var validExtension = ".yaml";

        var specCounter = Files.walk(Path.of(specPath))
                .filter(not(Files::isDirectory))
                .filter(f -> f.toString().endsWith(validExtension))
                .map(String::valueOf)
                .peek(System.out::println)
                .map(convert)
                .peek(System.out::println)
                .count();
    }
}