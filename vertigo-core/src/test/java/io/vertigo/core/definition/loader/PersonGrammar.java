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
package io.vertigo.core.definition.loader;

import static io.vertigo.core.definition.dsl.entity.EntityPropertyType.Boolean;
import static io.vertigo.core.definition.dsl.entity.EntityPropertyType.Double;
import static io.vertigo.core.definition.dsl.entity.EntityPropertyType.Integer;
import static io.vertigo.core.definition.dsl.entity.EntityPropertyType.String;

import java.util.List;

import io.vertigo.core.definition.dsl.entity.Entity;
import io.vertigo.core.definition.dsl.entity.EntityBuilder;
import io.vertigo.core.definition.dsl.entity.EntityGrammar;
import io.vertigo.util.ListBuilder;

/**
 * @author npiedeloup
 */
public final class PersonGrammar implements EntityGrammar {

	static final String NAME = "name";
	static final String FIRST_NAME = "firstName";
	static final String AGE = "age"; //,
	static final String HEIGHT = "height"; //.Double);
	static final String MALE = "male";
	static final Entity PERSON_ENTITY;

	static final String MAIN_ADDRESS = "mainAddress";
	static final String SECOND_ADDRESS = "secondaryAddress";
	static final String STREET = "street";
	static final String POSTAL_CODE = "postalCode";
	static final String CITY = "city";
	static final Entity ADDRESS_ENTITY;

	static {
		ADDRESS_ENTITY = new EntityBuilder("address")
				.addField(STREET, String, true)
				.addField(POSTAL_CODE, String, false)
				.addField(CITY, String, false)
				.build();
		PERSON_ENTITY = new EntityBuilder("person")
				.addField(NAME, String, true)
				.addField(FIRST_NAME, String, true)
				.addField(AGE, Integer, false)
				.addField(HEIGHT, Double, false)
				.addField(MALE, Boolean, true)
				.addField(MAIN_ADDRESS, ADDRESS_ENTITY.getLink(), true)
				.addField("secondaryAddress", ADDRESS_ENTITY.getLink(), false)
				.build();
	}

	@Override
	public List<Entity> getEntities() {
		return new ListBuilder<Entity>()
				.add(ADDRESS_ENTITY)
				.add(PERSON_ENTITY)
				.unmodifiable()
				.build();
	}
}
