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
/**
 *
 */
package io.vertigo.dynamo.plugins.store.filestore.fs;

import java.io.File;

import org.apache.log4j.Logger;

import io.vertigo.dynamo.transaction.VTransactionSynchronization;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;

/**
 * Classe de gestion de la sauvegarde d'un fichier.
 *
 * @author skerdudou
 */
final class FileActionDelete implements VTransactionSynchronization {
	private static final Logger LOG = Logger.getLogger(FileActionDelete.class.getName());

	private final File file;

	/**
	 * Constructeur.
	 *
	 * @param path le chemin de destination du fichier
	 */
	FileActionDelete(final String path) {
		Assertion.checkNotNull(path);
		//-----
		file = new File(path);

		if (!file.exists()) {
			LOG.error("Impossible de trouver le fichier pour suppression : " + file.getAbsolutePath());
			throw new VSystemException("Impossible de trouver le fichier à supprimer.");
		}
		if (!file.canWrite()) {
			LOG.error("Impossible de supprimer le fichier : " + file.getAbsolutePath());
			throw new VSystemException("Impossible de supprimer le fichier.");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void afterCompletion(final boolean txCommited) {
		if (txCommited) {
			// on supprime le fichier
			if (!file.delete()) {
				LOG.error("Impossible de supprimer le fichier " + file.getAbsolutePath());
				throw new VSystemException("Erreur fatale : Impossible de supprimer le fichier.");
			}
		}
	}

}
