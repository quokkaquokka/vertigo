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
package io.vertigo.core.spaces.definiton;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import io.vertigo.app.Home;
import io.vertigo.lang.Assertion;

/**
 * Référence SERIALIZABLE vers les definitions.
 * @author pchretien
 * @param <D> Type de la définition
 */
public final class DefinitionReference<D extends Definition> implements Serializable {
	private static final long serialVersionUID = 1L;
	/** Nom de la Définition. */
	private String definitionName;
	private transient D definition;

	/**
	 * Constructeur.
	 * @param definition Définition
	 */
	public DefinitionReference(final D definition) {
		Assertion.checkNotNull(definition);
		//-----
		this.definition = definition;
		definitionName = definition.getName();
	}

	/**
	 * @return Objet référencé
	 */
	public D get() {
		return definition;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		if (o instanceof DefinitionReference<?>) {
			return definitionName.equals(DefinitionReference.class.cast(o).definitionName);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return definitionName.hashCode();
	}

	private void writeObject(final ObjectOutputStream oos) throws IOException {
		//On écrit que le nom de la définition
		oos.writeObject(definitionName);
	}

	private void readObject(final ObjectInputStream ois) throws ClassNotFoundException, IOException {
		//On récupère le nom de la définition
		definitionName = (String) ois.readObject();
		definition = (D) Home.getApp().getDefinitionSpace().resolve(definitionName);
	}
}
