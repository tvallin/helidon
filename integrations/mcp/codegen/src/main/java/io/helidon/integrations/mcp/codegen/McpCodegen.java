/*
 * Copyright (c) 2025 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.integrations.mcp.codegen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.helidon.codegen.CodegenContext;
import io.helidon.codegen.CodegenException;
import io.helidon.codegen.CodegenUtil;
import io.helidon.codegen.RoundContext;
import io.helidon.codegen.classmodel.ClassModel;
import io.helidon.codegen.classmodel.Field;
import io.helidon.codegen.classmodel.Method;
import io.helidon.codegen.spi.CodegenExtension;
import io.helidon.common.types.AccessModifier;
import io.helidon.common.types.Annotation;
import io.helidon.common.types.Annotations;
import io.helidon.common.types.ElementKind;
import io.helidon.common.types.TypeInfo;
import io.helidon.common.types.TypeName;
import io.helidon.common.types.TypeNames;
import io.helidon.common.types.TypedElementInfo;

import static io.helidon.integrations.mcp.codegen.McpTypes.MCPSERVER;
import static io.helidon.integrations.mcp.codegen.McpTypes.MCP_NOTIFICATION;
import static io.helidon.integrations.mcp.codegen.McpTypes.MCP_PROMPT;
import static io.helidon.integrations.mcp.codegen.McpTypes.MCP_PROMPT_COMPONENT;
import static io.helidon.integrations.mcp.codegen.McpTypes.MCP_PROMT_PARAM;
import static io.helidon.integrations.mcp.codegen.McpTypes.MCP_RESOURCE;
import static io.helidon.integrations.mcp.codegen.McpTypes.MCP_RESOURCE_COMPONENT;
import static io.helidon.integrations.mcp.codegen.McpTypes.MCP_SERVER;
import static io.helidon.integrations.mcp.codegen.McpTypes.MCP_SUBSCRIBE;
import static io.helidon.integrations.mcp.codegen.McpTypes.MCP_TOOL;
import static io.helidon.integrations.mcp.codegen.McpTypes.MCP_TOOL_COMPONENT;
import static io.helidon.service.codegen.ServiceCodegenTypes.SERVICE_ANNOTATION_SINGLETON;

class McpCodegen implements CodegenExtension {

    private static final TypeName GENERATOR = TypeName.create(McpCodegen.class);

    private final System.Logger LOGGER = System.getLogger(this.getClass().getName());
    private final CodegenContext context;

    McpCodegen(CodegenContext context) {
        this.context = context;
    }

    @Override
    public void process(RoundContext roundContext) {
        LOGGER.log(System.Logger.Level.INFO, "Processing mcp codegen extension with context "
                + roundContext.types().stream().map(Object::toString).collect(Collectors.joining()));
        Collection<TypeInfo> types = roundContext.annotatedTypes(MCP_SERVER);
        for (TypeInfo type : types) {
            process(roundContext, type);
        }
    }

    private void process(RoundContext roundCtx, TypeInfo type) {
        if (type.kind() != ElementKind.CLASS && type.kind() != ElementKind.INTERFACE) {
            throw new CodegenException("Type annotated with " + MCP_SERVER.fqName() + " must be a class or an interface.",
                    type.originatingElementValue());
        }

        TypeName mcpFactoryType = type.typeName();
        TypeName generatedType = generatedTypeName(mcpFactoryType, "McpFactory");

        var classModel = ClassModel.builder()
                .type(generatedType)
                .copyright(CodegenUtil.copyright(GENERATOR,
                        mcpFactoryType,
                        generatedType))
                .addAnnotation(CodegenUtil.generatedAnnotation(GENERATOR,
                        mcpFactoryType,
                        generatedType,
                        "1",
                        ""))
                .accessModifier(AccessModifier.PACKAGE_PRIVATE)
                .addInterface(supplierType(MCPSERVER))
                .addAnnotation(Annotation.create(SERVICE_ANNOTATION_SINGLETON));

        classModel.addImport("java.util.List");
        classModel.addImport(TypeName.create("io.helidon.integrations.mcp.server.InputSchema"));
        classModel.addImport(TypeName.create("io.modelcontextprotocol.spec.McpSchema"));

        classModel.addField(Field.builder()
                .accessModifier(AccessModifier.PRIVATE)
                .isFinal(true)
                .name("delegate")
                .type(type.typeName())
                .addContent("new " + type.typeName().className() + "()")
                .build());

        Method.Builder builder = Method.builder()
                .accessModifier(AccessModifier.PUBLIC)
                .addAnnotation(Annotations.OVERRIDE)
                .returnType(MCPSERVER)
                .name("get")
                .addContentLine("McpServer.Builder builder = McpServer.fluentBuilder();");

        generateServerConfig(builder, type);
        generateTools(classModel, builder, type);
        generateResources(classModel, builder, type);
        generatePrompts(classModel, builder, type);

        builder.addContentLine("return builder.build();");
        classModel.addMethod(builder);

        roundCtx.addGeneratedType(generatedType, classModel, mcpFactoryType, type.originatingElementValue());
    }

    private void generateServerConfig(Method.Builder builder, TypeInfo type) {
        if (type.hasAnnotation(MCP_SERVER)) {
            var map = type.annotation(MCP_SERVER).values();
            var name = map.get("name").toString();
            var version = map.get("version").toString();
            builder.addContentLine("builder.name(" + quoted(name) + ");");
            builder.addContentLine("builder.version(" + quoted(version) + ");");
        }

        if (type.hasAnnotation(MCP_NOTIFICATION)) {
            var map = type.annotation(MCP_NOTIFICATION).values();
            var array = (List<String>) map.get("value");
            for (String value : array) {
                if ("tool".equals(value)) {
                    builder.addContentLine("builder.toolChange(true);");
                }
                if ("resource".equals(value)) {
                    builder.addContentLine("builder.resourceChange(true);");
                }
                if ("prompt".equals(value)) {
                    builder.addContentLine("builder.promptChange(true);");
                }
            }
        }

        if (type.hasAnnotation(MCP_SUBSCRIBE)) {
            var map = type.annotation(MCP_NOTIFICATION).values();
            var array = (List<String>) map.get("value");
            if ("resource".equals(array.getFirst())) {
                builder.addContentLine("builder.resourceSubscribe(true);");
            }
        }
    }


    private void generateTools(ClassModel.Builder classModel, Method.Builder method, TypeInfo type) {
        List<TypedElementInfo> elements = getElementsWithAnnotation(type, MCP_TOOL);
        for (TypedElementInfo element : elements) {
            generateToolMethod(classModel, element);
            method.addContentLine("builder.addTool(" + element.elementName() + "());");
        }
    }

    private void generateToolMethod(ClassModel.Builder classModel, TypedElementInfo element) {
        String methodName = element.elementName();
        Method.Builder builder = Method.builder()
                .accessModifier(AccessModifier.PACKAGE_PRIVATE)
                .returnType(MCP_TOOL_COMPONENT)
                .name(methodName);
        Map<String, Object> annotation = element.annotations().getFirst().values();
        String name = quoted(annotation.get("name").toString());
        String description = quoted(annotation.get("description").toString());
        String schema = createSchema(element);
        String handler = createHandler(element);
        builder.content("""
                return ToolComponent.builder()
                                .name(%s)
                                .description(%s)
                                .schema(%s)
                                .handler(arguments -> {
                                    %s
                                })
                                .build();
                """.formatted(name, description, schema, handler));
        classModel.addMethod(builder);
    }


    private void generateResources(ClassModel.Builder classModel, Method.Builder method, TypeInfo type) {
        List<TypedElementInfo> elements = getElementsWithAnnotation(type, MCP_RESOURCE);
        for (TypedElementInfo element : elements) {
            generateResourceMethod(classModel, element);
            method.addContentLine("builder.addResource(" + element.elementName() + "());");
        }
    }

    private void generateResourceMethod(ClassModel.Builder classModel, TypedElementInfo element) {
        String methodName = element.elementName();
        Method.Builder builder = Method.builder()
                .accessModifier(AccessModifier.PACKAGE_PRIVATE)
                .returnType(MCP_RESOURCE_COMPONENT)
                .name(methodName);
        Map<String, Object> annotation = element.annotations().getFirst().values();
        String uri = quoted(annotation.get("uri").toString());
        String name = quoted(annotation.get("name").toString());
        String description = quoted(annotation.get("description").toString());
        builder.content("""
                return ResourceComponent.builder()
                                .uri(%s)
                                .name(%s)
                                .description(%s)
                                .build();
                """.formatted(uri, name, description));
        classModel.addMethod(builder);
    }

    private void generatePrompts(ClassModel.Builder classModel, Method.Builder method, TypeInfo type) {
        List<TypedElementInfo> elements = getElementsWithAnnotation(type, MCP_PROMPT);
        for (TypedElementInfo element : elements) {
            generatePromptMethod(classModel, element);
            method.addContentLine("builder.addPrompt(" + element.elementName() + "());");
        }
    }

    private void generatePromptMethod(ClassModel.Builder classModel, TypedElementInfo element) {
        String methodName = element.elementName();
        Method.Builder builder = Method.builder()
                .accessModifier(AccessModifier.PACKAGE_PRIVATE)
                .returnType(MCP_PROMPT_COMPONENT)
                .name(methodName);
        Map<String, Object> annotation = element.annotations().getFirst().values();
        String name = quoted(annotation.get("name").toString());
        String description = quoted(annotation.get("description").toString());
        String handler = createHandler(element);
        builder.addContentLine("var builder = PromptComponent.builder();");
        createPromptArguments(builder, element.parameterArguments());
        builder.addContentLine("""
                return builder.name(%s)
                            .description(%s)
                            .handler(arguments -> {
                                %s
                            })
                            .build();
                """.formatted(name, description, handler));
        classModel.addMethod(builder);
    }

    private String createHandler(TypedElementInfo elementInfo) {
        List<TypedElementInfo> parameters = elementInfo.parameterArguments();
        List<String> required = parameters.stream()
                .map(param -> param.elementName())
                .toList();
        List<String> variables = new ArrayList<>();
        for (TypedElementInfo param : parameters) {
            variables.add("String " + param.elementName() + " = arguments.get(" + quoted(param.elementName()) + ").toString();");
        }
        variables.add("                    return delegate." + elementInfo.elementName() + "(" + String.join(", ", required) + ");");
        return String.join(System.lineSeparator(), variables);
    }

    private String createSchema(TypedElementInfo tool) {
        List<TypedElementInfo> parameters = tool.parameterArguments();
        List<String> required = parameters.stream()
                .map(param -> quoted(param.elementName()))
                .toList();
        List<String> properties = parameters.stream()
                .map(param -> quoted(param.elementName())
                        + ", "
                        + quoted(param.enclosingType().orElse(TypeNames.STRING).className().toLowerCase()))
                .toList();
        return String.format("InputSchema.builder().required(%s).properties(%s)",
                String.join(",", required),
                String.join(",", properties));
    }

    private void createPromptArguments(Method.Builder it, List<TypedElementInfo> elements) {
        String description = "none";
        List<TypedElementInfo> promptParam = elements.stream()
                .filter(this::hasPromptAnnotation)
                .toList();
        for (TypedElementInfo param : promptParam) {
            if (param.hasAnnotation(MCP_PROMT_PARAM)) {
                Annotation annotation = param.annotation(MCP_PROMT_PARAM);
                description = annotation.value().orElse("none");
            }
            it.addContentLine("builder.promptArgument(new McpSchema.PromptArgument("
                    + quoted(param.elementName()) + ", " + quoted(description) + ", true));");
        }
    }

    private boolean hasPromptAnnotation(TypedElementInfo element) {
        return element.annotations().stream()
                .anyMatch(annotation -> MCP_PROMT_PARAM.name().equals(annotation.typeName().name()));
    }

    private Optional<TypedElementInfo> getElementWithAnnotation(TypeInfo type, TypeName target) {
        return type.elementInfo().stream()
                .filter(element -> element.hasAnnotation(target))
                .findFirst();
    }

    private Optional<Annotation> annotation(TypeInfo type, String name) {
        return type.annotations().stream()
                .filter(annotation -> name.equals(annotation.typeName().name()))
                .findFirst();
    }

    private List<TypedElementInfo> getElementsWithAnnotation(TypeInfo type, TypeName target) {
        return type.elementInfo().stream()
                .filter(element -> element.hasAnnotation(target))
                .collect(Collectors.toList());
    }

    private TypeName generatedTypeName(TypeName factoryTypeName, String suffix) {
        return TypeName.builder()
                .packageName(factoryTypeName.packageName())
                .className(factoryTypeName.classNameWithEnclosingNames().replace('.', '_') + "__" + suffix)
                .build();
    }

    private TypeName supplierType(TypeName suppliedType) {
        return TypeName.builder(TypeNames.SUPPLIER)
                .addTypeArgument(suppliedType)
                .build();
    }

    private String quoted(String value) {
        return "\"" + value + "\"";
    }
}
