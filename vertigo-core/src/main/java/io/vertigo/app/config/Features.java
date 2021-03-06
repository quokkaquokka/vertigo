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
package io.vertigo.app.config;

import io.vertigo.lang.Assertion;

/**
 * Defines a module by its features.
 * @author pchretien
 */
public abstract class Features {
	private final String name;
	private ModuleConfigBuilder moduleConfigBuilder;

	protected Features(final String name) {
		Assertion.checkArgNotEmpty(name);
		//-----
		this.name = name;
	}

	public final void init(final AppConfigBuilder appConfigBuilder) {
		Assertion.checkNotNull(appConfigBuilder);
		Assertion.checkState(moduleConfigBuilder == null, "appConfigBuilder is alreay defined");
		//---
		moduleConfigBuilder = appConfigBuilder.beginModule(name);
		setUp();
	}

	protected abstract void setUp();

	public ModuleConfigBuilder getModuleConfigBuilder() {
		return moduleConfigBuilder;
	}

	protected void buildFeatures() {
		//overrided if needed
	}

	public final AppConfigBuilder endModule() {
		buildFeatures();
		return moduleConfigBuilder.endModule();
	}

}
