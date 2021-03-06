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
package io.vertigo.dynamo.database.vendor;

import java.sql.SQLException;

import io.vertigo.dynamo.database.statement.SqlPreparedStatement;

/**
 * Handler des exceptions SQL qui peuvent survenir lors de l'exécution d'une requête.
 * @author npiedeloup
 */
public interface SqlExceptionHandler {

	/**
	 * Gestion des erreurs SQL => Transformation en erreurs KSystemException et KUserException
	 * selon la plage de l'erreur.
	 * @param sqle Exception survenue
	 * @param statement Statement SQL (i.e. requête SQL)
	 */
	void handleSQLException(SQLException sqle, SqlPreparedStatement statement);
}
