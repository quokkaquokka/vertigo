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
package io.vertigo.dynamo.plugins.environment.registries.file;

import java.util.Collections;
import java.util.List;

import io.vertigo.core.definition.dsl.entity.Entity;
import io.vertigo.core.definition.dsl.entity.EntityBuilder;
import io.vertigo.core.definition.dsl.entity.EntityGrammar;
import io.vertigo.core.definition.dsl.entity.EntityPropertyType;
import io.vertigo.dynamo.plugins.environment.KspProperty;

/**
 * @author npiedeloup
 */
final class FileGrammar implements EntityGrammar {

	/**Définition de tache.*/
	public static final Entity FILE_INFO_DEFINITION_ENTITY;

	static {
		FILE_INFO_DEFINITION_ENTITY = new EntityBuilder("FileInfo")
				.addField(KspProperty.DATA_SPACE, EntityPropertyType.String, true)
				.build();
	}

	@Override
	public List<Entity> getEntities() {
		return Collections.singletonList(FILE_INFO_DEFINITION_ENTITY);
	}
}
