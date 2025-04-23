package io.helidon.integrations.mcp.server;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.helidon.common.mapper.Mapper;
import io.helidon.common.mapper.spi.MapperProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * McpSchemaMapper convert Helidon MCP configuration into McpSchema.
 */
public class McpSchemaMapper implements MapperProvider {

	private static final Map<Pair, Mapper<?, ?>> MAPPERS;
	private static final ObjectMapper MAPPER = new ObjectMapper();

	static {
		Map<Pair, Mapper<?, ?>> map = new HashMap<>();

		addMapper(map, McpSchema.JSONRPCRequest.class, McpSchemaMapper::toRPCRequest);

		MAPPERS = Map.copyOf(map);
	}

	public McpSchemaMapper() {
	}

	static <T> void addMapper(Map<Pair, Mapper<?, ?>> mappers, Class<T> clazz, Function<String, T> mapper) {
		Mapper<String, T> map = mapper::apply;
		mappers.put(Pair.of(String.class, clazz), map);
	}

	static McpSchema.JSONRPCRequest toRPCRequest(String content) {
		return MAPPER.convertValue(content, McpSchema.JSONRPCRequest.class);
	}

	@Override
	public ProviderResponse mapper(Class<?> sourceClass, Class<?> targetClass, String qualifier) {
		if (!qualifier.isEmpty()) {
			return ProviderResponse.unsupported();
		}
		Mapper<?, ?> mapper = MAPPERS.get(Pair.of(sourceClass, targetClass));
		if (mapper != null) {
			return new ProviderResponse(Support.SUPPORTED, mapper);
		}
		if (targetClass.equals(String.class)) {
			return new ProviderResponse(Support.COMPATIBLE, String::valueOf);
		}
		return ProviderResponse.unsupported();
	}

	private record Pair(Class<?> source, Class<?> target) {
		static Pair of(Class<?> source, Class<?> target) {
			return new Pair(source, target);
		}
	}


	//TODO - useless now ?

	static McpSchema.InitializeResult initializeResult(String latestProtocolVersion,
													   ServerCapabilities capabilities,
													   Implementation implementation,
													   String instructions) {
		return new McpSchema.InitializeResult(
				latestProtocolVersion,
				serverCapabilities(capabilities),
				implementation(implementation),
				instructions);
	}

	private static McpSchema.Implementation implementation(Implementation implementation) {
		return new McpSchema.Implementation(implementation.name(), implementation.version());
	}

	static McpSchema.ServerCapabilities serverCapabilities(ServerCapabilities capabilities) {
		return new McpSchema.ServerCapabilities(
				capabilities.experimentation(),
				new McpSchema.ServerCapabilities.LoggingCapabilities(),
				prompts(capabilities.promts()),
				resource(capabilities.resource()),
				tools(capabilities.tools()));
	}

	private static McpSchema.ServerCapabilities.ToolCapabilities tools(ListChanged tools) {
		return new McpSchema.ServerCapabilities.ToolCapabilities(tools.listChanged());
	}

	private static McpSchema.ServerCapabilities.ResourceCapabilities resource(Resource resource) {
		return new McpSchema.ServerCapabilities.ResourceCapabilities(resource.subscribe(), resource.listChanged());
	}

	static McpSchema.ServerCapabilities.PromptCapabilities prompts(ListChanged promts) {
		return new McpSchema.ServerCapabilities.PromptCapabilities(promts.listChanged());
	}
}
