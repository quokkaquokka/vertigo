/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.environment.splittedmodules;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.app.AutoCloseableApp;
import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.AppConfigBuilder;
import io.vertigo.app.config.LogConfig;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.plugins.environment.loaders.java.AnnotationLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.KprLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainDynamicRegistryPlugin;
import io.vertigo.dynamock.domain.DtDefinitions;

/**
 * Test de l'implémentation standard.
 *
 * @author npiedeloup
 */
public final class SplittedModulesEnvironmentManagerTest {

	//
	//<module name="test-2"><!--  this moduleReference -->
	//	<resource type ="classes" path="io.vertigo.dynamock.domain.DtDefinitions"/>
	//</module>

	@Test
	public void testFirstModule() {
		final AppConfig appConfig = prepareDefaultAppConfigBuilder()
				.beginModule("myApp").addDefinitionResource("kpr", "io/vertigo/dynamock/execution.kpr").endModule()
				.build();

		try (final AutoCloseableApp app = new AutoCloseableApp(appConfig)) {
			final Domain doString = app.getDefinitionSpace().resolve("DO_STRING", Domain.class);
			Assert.assertNotNull(doString);
		}
	}

	@Test
	public void testMergedModules() {
		// @formatter:off
		final AppConfig appConfig = prepareDefaultAppConfigBuilder()
				.beginModule("myApp")
					.addDefinitionResource("kpr", "io/vertigo/dynamock/execution.kpr")
					.addDefinitionResource("classes", DtDefinitions.class.getCanonicalName())
				.endModule()
			.build();
		// @formatter:on

		try (final AutoCloseableApp app = new AutoCloseableApp(appConfig)) {
			final Domain doString = app.getDefinitionSpace().resolve("DO_STRING", Domain.class);
			Assert.assertNotNull(doString);
			final DtDefinition dtFamille = app.getDefinitionSpace().resolve("DT_FAMILLE", DtDefinition.class);
			Assert.assertNotNull(dtFamille);
		}
	}

	@Test
	public void testSplittedModules() {
		// @formatter:off
		final AppConfig appConfig = prepareDefaultAppConfigBuilder()
				.beginModule("myApp")
					.addDefinitionResource("kpr", "io/vertigo/dynamock/execution.kpr")
					.addDefinitionResource("classes", DtDefinitions.class.getCanonicalName())
				.endModule()
			.build();
		// @formatter:on

		try (final AutoCloseableApp app = new AutoCloseableApp(appConfig)) {
			final Domain doString = app.getDefinitionSpace().resolve("DO_STRING", Domain.class);
			Assert.assertNotNull(doString);
			final DtDefinition dtFamille = app.getDefinitionSpace().resolve("DT_FAMILLE", DtDefinition.class);
			Assert.assertNotNull(dtFamille);
		}
	}

	private static AppConfigBuilder prepareDefaultAppConfigBuilder() {
		// @formatter:off

		return
			new AppConfigBuilder()
			.beginBoot()
				.withLogConfig(new LogConfig("/log4j.xml"))
			.endBoot()
			.beginBootModule("fr")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.addPlugin(KprLoaderPlugin.class)
				.addPlugin(AnnotationLoaderPlugin.class)
				.addPlugin(DomainDynamicRegistryPlugin.class)
			.endModule();
		// @formatter:on
	}
}
