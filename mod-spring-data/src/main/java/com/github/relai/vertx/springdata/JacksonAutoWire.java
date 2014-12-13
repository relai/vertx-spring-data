package com.github.relai.vertx.springdata;

/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;


/**
 * Internal class for auto wiring Jackson. 
 * 
 * <p>This class is a clone of Spring Boot 1.1.x 
 * <code>org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration</code>. 
 * In Spring boot 1.2.0, the auto configuration is conditional on the presence 
 * of <code>Jackson2ObjectMapperBuilder</code>, which is part of the Spring Web
 * module and is therefore is unlikely to be present in a Vert.x project. This cloned class
 * ensures that our mod can continue to work with a later version of Spring Boot.
 * 
 * <p>The following auto-configuration will get applied:
 * <ul>
 * <li>an {@link ObjectMapper} in case none is already configured.</li>
 * <li>the {@link JodaModule} registered if it's on the classpath.</li>
 * <li>auto-registration for all {@link Module} beans with all {@link ObjectMapper} beans
 * (including the defaulted ones).</li>
 * </ul>
 *
 */
@Configuration
@ConditionalOnClass(ObjectMapper.class)
class JacksonAutoWire {

	@Autowired
	private ListableBeanFactory beanFactory;

	@PostConstruct
	private void registerModulesWithObjectMappers() {
		Collection<Module> modules = getBeans(Module.class);
		for (ObjectMapper objectMapper : getBeans(ObjectMapper.class)) {
			objectMapper.registerModules(modules);
		}
	}

	private <T> Collection<T> getBeans(Class<T> type) {
		return BeanFactoryUtils.beansOfTypeIncludingAncestors(this.beanFactory, type)
				.values();
	}

	@Configuration
	@ConditionalOnClass(ObjectMapper.class)
	static class JacksonObjectMapperAutoConfiguration {

		@Bean
		@Primary
		@ConditionalOnMissingBean
		public ObjectMapper jacksonObjectMapper() {
			ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

			return objectMapper;
		}

	}

	@Configuration
	@ConditionalOnClass(JodaModule.class)
	static class JodaModuleAutoConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public JodaModule jacksonJodaModule() {
			return new JodaModule();
		}

	}

}
