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
package io.vertigo.app.config.xml;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import io.vertigo.app.config.AppConfigBuilder;
import io.vertigo.app.config.ComponentConfigBuilder;
import io.vertigo.app.config.DefinitionProvider;
import io.vertigo.app.config.ModuleConfigBuilder;
import io.vertigo.app.config.PluginConfigBuilder;
import io.vertigo.core.component.aop.Aspect;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;
import io.vertigo.lang.Plugin;
import io.vertigo.util.ClassUtil;

/**
 * @author npiedeloup, pchretien
 */
final class XMLModulesHandler extends DefaultHandler {
	private final AppConfigBuilder appConfigBuilder;
	//Global Params
	private final XMLModulesParams params;

	private ModuleConfigBuilder moduleConfigBuilder;
	private ComponentConfigBuilder componentConfigBuilder;
	private PluginConfigBuilder pluginConfigBuilder;
	private TagName current;

	XMLModulesHandler(final AppConfigBuilder appConfigBuilder, final XMLModulesParams params) {
		Assertion.checkNotNull(appConfigBuilder);
		Assertion.checkNotNull(params);
		//-----
		this.appConfigBuilder = appConfigBuilder;
		this.params = params;
	}

	enum TagName {
		config,
		boot,
		module,
		init,
		//---
		definitions,
		resource,
		provider,
		//---
		component,
		plugin,
		param,
		aspect,
		//-----
		initializer
	}

	@Override
	public void endElement(final String namespaceURI, final String localName, final String qName) {
		switch (TagName.valueOf(qName)) {
			case boot:
				moduleConfigBuilder = null;
				break;
			case module:
				moduleConfigBuilder.endModule();
				moduleConfigBuilder = null;
				break;
			case component:
				componentConfigBuilder.endComponent();
				componentConfigBuilder = null;
				break;
			case plugin:
				pluginConfigBuilder.endPlugin();
				pluginConfigBuilder = null;
				break;
			case aspect:
			case param:
			case definitions:
			case provider:
			case resource:
			case config:
			case init:
			case initializer:
				//non géré
			default:
		}
	}

	@Override
	public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes attrs) {
		switch (TagName.valueOf(qName)) {
			case boot:
				final String locales = attrs.getValue("locales");
				moduleConfigBuilder = appConfigBuilder.beginBootModule(locales);
				break;
			case module:
				current = TagName.module;
				final String moduleName = attrs.getValue("name");
				final String api = attrs.getValue("api");
				moduleConfigBuilder = appConfigBuilder.beginModule(moduleName);
				if (api != null && !Boolean.parseBoolean(api)) {
					moduleConfigBuilder.withNoAPI();
				}
				break;
			case component:
				current = TagName.component;
				final String componentApi = attrs.getValue("api");
				final Class<? extends Component> componentImplClass = ClassUtil.classForName(attrs.getValue("class"), Component.class);
				if (componentApi != null) {
					final Class<?> componentClass = resolveInterface(componentApi, componentImplClass);
					componentConfigBuilder = moduleConfigBuilder.beginComponent((Class<? extends Component>) componentClass, componentImplClass);
				} else {
					componentConfigBuilder = moduleConfigBuilder.beginComponent(componentImplClass);
				}
				break;
			case initializer:
				final String initClass = attrs.getValue("class");
				if (initClass != null) {
					final Class componentInitialierClass = ClassUtil.classForName(initClass);
					appConfigBuilder.addInitializer(componentInitialierClass);
				}
				break;
			case plugin:
				current = TagName.plugin;
				final Class<? extends Plugin> pluginImplClass = ClassUtil.classForName(attrs.getValue("class"), Plugin.class);
				pluginConfigBuilder = moduleConfigBuilder.beginPlugin(pluginImplClass);
				break;
			case provider:
				final String definitionProviderClassName = attrs.getValue("className");
				final Class<? extends DefinitionProvider> definitionProviderClass = ClassUtil.classForName(definitionProviderClassName, DefinitionProvider.class);
				moduleConfigBuilder.addDefinitionProvider(definitionProviderClass);
				break;
			case resource:
				final String resourceType = attrs.getValue("type");
				final String resourcePath = attrs.getValue("path");
				moduleConfigBuilder.addDefinitionResource(resourceType, evalParamValue(resourcePath));
				break;
			case param:
				final String paramName = attrs.getValue("name");
				final String paramValue = attrs.getValue("value");
				if (current == TagName.plugin) {
					pluginConfigBuilder.addParam(paramName, evalParamValue(paramValue));
				} else {
					componentConfigBuilder.addParam(paramName, evalParamValue(paramValue));
				}
				break;
			case aspect:
				final String aspectImplClassStr = attrs.getValue("class");
				final Class<? extends Aspect> aspectImplClass = ClassUtil.classForName(aspectImplClassStr, Aspect.class);
				moduleConfigBuilder.addAspect(aspectImplClass);
				break;
			case definitions:
			case config:
			case init:
				//non géré
			default:
		}
	}

	//On recherche l'interface ayant le nom 'simpleName' dans l'arbre de la classe 'clazz'
	//Cette interface doit exister et être unique.
	private static Class<?> resolveInterface(final String simpleName, final Class<? extends Component> clazz) {
		Class<?> found = null;
		for (final Class<?> interfaceClazz : ClassUtil.getAllInterfaces(clazz)) {
			if (simpleName.equals(interfaceClazz.getSimpleName())) {
				Assertion.checkState(found == null, "Many interfaces of class '{0}' have the same simpleName {1}", clazz, simpleName);
				found = interfaceClazz;
			}
		}
		Assertion.checkNotNull(found, "No interface of class '{0}' have the simpleName '{1}'", clazz, simpleName);
		return found;
	}

	private String evalParamValue(final String paramValue) {
		if (paramValue.startsWith("${boot.") && paramValue.endsWith("}")) {
			final String property = paramValue.substring("${".length(), paramValue.length() - "}".length());
			return params.getParam(property);
		}
		return paramValue;
	}
}
