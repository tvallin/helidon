/*
 * Copyright (c) 2019, 2024 Oracle and/or its affiliates.
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

/**
 * Helidon Common Mapper.
 */
module io.helidon.common.mapper {
    requires transitive io.helidon.common;
    requires transitive io.helidon.builder.api;

    requires io.helidon.common.types;

    requires io.helidon.service.registry;

    exports io.helidon.common.mapper;
    exports io.helidon.common.mapper.spi;

    uses io.helidon.common.mapper.spi.MapperProvider;

}
