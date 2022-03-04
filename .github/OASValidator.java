///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.eclipse.microprofile.openapi:microprofile-openapi-api:3.0
//DEPS org.openapitools.empoa:empoa-simple-models-impl:2.0.0
//DEPS org.openapitools.openapistylevalidator:openapi-style-validator-lib:1.7
//DEPS org.openapitools.empoa:empoa-swagger-core:2.0.0
//DEPS io.swagger.parser.v3:swagger-parser:2.0.30
//DEPS org.slf4j:slf4j-jdk14:1.7.36
//DEPS com.fasterxml.jackson.core:jackson-annotations:2.13.1

import java.io.File;
import java.util.stream.Stream;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.openapitools.empoa.swagger.core.internal.SwAdapter;
import org.openapitools.openapistylevalidator.OpenApiSpecStyleValidator;
import org.openapitools.openapistylevalidator.ValidatorParameters;
import org.openapitools.openapistylevalidator.ValidatorParameters.NamingConvention;
import org.openapitools.openapistylevalidator.styleerror.StyleError;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public class OASValidator {

    public static void main(String[] args) {
        System.out.println("Validating multiple OAS files");

        //Configuration parameters
        Supplier<ValidatorParameters> createValidatorParameters = () -> {
            ValidatorParameters parameters = new ValidatorParameters();
            parameters.setValidateInfoLicense(true);
            parameters.setValidateInfoDescription(false);
            parameters.setValidateInfoContact(false);
            parameters.setValidateOperationOperationId(true);
            parameters.setValidateOperationDescription(false);
            parameters.setValidateOperationTag(true);
            parameters.setValidateOperationSummary(true);
            parameters.setValidateModelPropertiesExample(false);
            parameters.setValidateModelPropertiesDescription(false);
            parameters.setValidateModelRequiredProperties(true);
            parameters.setValidateModelNoLocalDef(true);
            parameters.setValidateNaming(true);
            parameters.setIgnoreHeaderXNaming(true);
            parameters.setPathNamingConvention(ValidatorParameters.NamingConvention.HyphenCase);
            parameters.setHeaderNamingConvention(ValidatorParameters.NamingConvention.CamelCase);
            parameters.setParameterNamingConvention(ValidatorParameters.NamingConvention.CamelCase);
            parameters.setPropertyNamingConvention(ValidatorParameters.NamingConvention.CamelCase);
            return parameters;
        };

        BiConsumer<String,String> validateOAS = (specPath, file) -> {
            OpenAPIParser openApiParser = new OpenAPIParser();
            ParseOptions parseOptions = new ParseOptions();
            parseOptions.setResolve(true);

            SwaggerParseResult parserResult = openApiParser.readLocation(specPath + file, null, parseOptions);
            io.swagger.v3.oas.models.OpenAPI swaggerOpenAPI = parserResult.getOpenAPI();

            org.eclipse.microprofile.openapi.models.OpenAPI openAPI = SwAdapter.toOpenAPI(swaggerOpenAPI);
            OpenApiSpecStyleValidator openApiSpecStyleValidator = new OpenApiSpecStyleValidator(openAPI);

            ValidatorParameters parameters = createValidatorParameters.get();
            //System.out.println(String.format("Validating with options: %s", parameters));
            List<StyleError> result = openApiSpecStyleValidator.validate(parameters);
            if (!result.isEmpty()) {
                result.stream().map(StyleError::toString).forEach(m -> System.out.println(String.format("\t%s", m)));
                throw new RuntimeException("OpenAPI Style validation failed");
            }
        };

        //Process
        var specPath = "../folder1/";
        Stream.of(new File(specPath).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .peek(System.out::println)
                .forEach(file -> validateOAS.accept(specPath, file));
    }
}