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
package io.vertigo.dynamo.impl.database.statementhandler;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Type de sortie du prepareStatement.
 * Permet de créer des types de sortie dynamiques.
 * 
 * @author  pchretien
 */
interface SqlResultMetaData {
	DtObject createDtObject();

	/***
	 * Récupération de la DtDefinition du type de retour du PrepareStatement.
	 * @return DtDefinition du type de retour du PrepareStatement
	 */
	DtDefinition getDtDefinition();

	/***
	 * @return Si le type de sortie est un DTO.
	 */
	boolean isDtObject();
}
