package io.helidon.integrations.mcp.server;

import io.helidon.common.config.Config;
import io.helidon.service.registry.Services;
import io.helidon.webserver.WebServer;

public class Temp {
//
//    public static void main(String[] args) {
//        var config = Services.get(Config.class);
//
//        WebServer.builder()
//                .config(config.get("server"))
//                .routing(routing -> routing.addFeature(McpHttpFeature.create(new McpWeatherServer())))
//                .build()
//                .start();
//    }
//
//    static class McpWeatherServer implements McpServerAPI {
//
//        @Override
//        public McpServerInfo info() {
//            return McpServerInfo.create("mcp-server", "0.0.1", CapabilitiesTemp.TOOL_LIST_CHANGED);
//        }
//
//        @Override
//        public void setup(McpRouting.Builder routing) {
//            routing.register(new WeatherTool())
//                    .register(new MyResource())
//                    .register(new WeatherPrompt());
//        }
//    }
//
//    static class WeatherTool implements Tool {
//
//        @Override
//        public ToolInfo info() {
//            return ToolInfo.builder()
//                    .name("tool-weater")
//                    .description("Get the weather in a specific town")
//                    .requiredProperties("town")
//                    .properties("town", "string")
//                    .build();
//        }
//
//        @Override
//        public Handler process() {
//            return parameter -> "It is sunny in " + parameter.get("town");
//        }
//    }
//
//    static class WeatherPrompt implements PromptTemp {
//
//        @Override
//        public PromptInfo info() {
//            return PromptInfo.builder()
//                    .name("prompt-weather")
//                    .description("Get the weather in a specific town")
//                    .arguments(PromptArgument.create("town", "The name of the town"))
//                    .build();
//        }
//
//        @Override
//        public Handler prompt() {
//            return parameter -> "It is sunny in " + parameter.get("town");
//        }
//    }
//
//    static class MyResource implements ResourceTemp {
//
//        @Override
//        public ResourceInfo info() {
//            return ResourceInfo.builder()
//                    .uri("file:///tmp/")
//                    .name("temp-file")
//                    .description("This is a temporary file")
//                    .build();
//        }
//
//        @Override
//        public Handler read() {
//            return null;
//        }
//    }
}
