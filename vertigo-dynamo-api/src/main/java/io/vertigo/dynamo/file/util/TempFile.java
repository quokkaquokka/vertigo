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
package io.vertigo.dynamo.file.util;

import java.io.File;
import java.io.IOException;

/**
 * Fichier temporaire supprimé automatiquement après utilisation.
 * @author npiedeloup
 */
public final class TempFile extends File {

	private static final long serialVersionUID = 1947509935178818002L;
	/**
	 * Vertigo Temp directory path.
	 */
	public static final String VERTIGO_TMP_DIR_PATH;
	static {
		final File vertigoTmpDir = new File(System.getProperty("java.io.tmpdir"), "vertigo");
		vertigoTmpDir.mkdirs();
		VERTIGO_TMP_DIR_PATH = vertigoTmpDir.getAbsolutePath();
	}

	/**
	 * Crée un fichier temporaire qui sera supprimé lorsqu'il ne sera plus référencé.
	 * @param prefix Prefix du nom de fichier
	 * @param suffix Suffix du nom de fichier
	 * @param subDirectory Sous-répertoire des fichiers temporaires (null = répertoire temporaire de vertigo = ${java.io.tmpdir}/vertigo)
	 * @throws IOException Exception IO
	 */
	public TempFile(final String prefix, final String suffix, final String subDirectory) throws IOException {
		super(File.createTempFile(prefix, suffix, new File(VERTIGO_TMP_DIR_PATH, subDirectory)).getAbsolutePath());
		deleteOnExit();
	}

	/**
	 * Crée un fichier temporaire qui sera supprimé lorsqu'il ne sera plus référencé.
	 * @param prefix Prefix du nom de fichier
	 * @param suffix Suffix du nom de fichier
	 * @throws IOException Exception IO
	 */
	public TempFile(final String prefix, final String suffix) throws IOException {
		super(File.createTempFile(prefix, suffix, new File(VERTIGO_TMP_DIR_PATH)).getAbsolutePath());
		deleteOnExit();
	}

	/** {@inheritDoc} */
	@Override
	protected void finalize() throws Throwable {
		if (exists() && !delete()) {
			deleteOnExit();
		}
		super.finalize();
	}
}
