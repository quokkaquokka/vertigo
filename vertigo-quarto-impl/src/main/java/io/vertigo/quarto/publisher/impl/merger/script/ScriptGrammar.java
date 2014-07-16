/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.quarto.publisher.impl.merger.script;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Grammaire des �ditions.
 * Offre un langage simple et de haut niveau permettant d'utiliser une syntaxe non java
 * afin par exemple de constituer des �ditions.
 *
 * Une grammaire est constitu�e de mots cl�s (Keyword) en nombre fini.
 *
 * @author oboitel, pchretien
 * @version $Id: ScriptGrammar.java,v 1.5 2014/02/27 10:33:07 pchretien Exp $
 */
public final class ScriptGrammar {
	// Serializable pour CC.
	private static final class StringLengthComparator implements Comparator<String>, Serializable {
		private static final long serialVersionUID = -4771988169106042448L;

		StringLengthComparator() {
			//pour visibilit�
		}

		/** {@inheritDoc} */
		public int compare(final String o1, final String o2) {
			return o2.length() - o1.length();
		}
	}

	private static final String END_PREFIX = "end";
	private final Map<String, ScriptTagDefinition> tagDefinitionBySyntax = new HashMap<>();
	private final List<String> orderedParsingTagList = new ArrayList<>();

	/**
	 * Permet d'engegistrer un tag KScript suppl�mentaire dans la grammaire.
	 * @param name name nom du tag (nom de la balise)
	 * @param classTag class du Handler pour g�rer le tag
	 * @param hasBody indique si ce tag poss�de un body ou non
	 */
	public void registerScriptTag(final String name, final Class<? extends ScriptTag> classTag, final boolean hasBody) {
		tagDefinitionBySyntax.put(name, new ScriptTagDefinition(name, classTag, hasBody ? Boolean.TRUE : null));
		if (hasBody) {
			tagDefinitionBySyntax.put(END_PREFIX + name.trim(), new ScriptTagDefinition(name, classTag, Boolean.FALSE));
		}
		orderedParsingTagList.clear();
		orderedParsingTagList.addAll(tagDefinitionBySyntax.keySet());
		//On tri pour que les tag les plus long soient test� en premier, ainsi il n'y a pas de pb de recouvrement
		Collections.sort(orderedParsingTagList, new StringLengthComparator());
	}

	/**
	 * R�cup�rer un mot cl� de la grammaire � partir d'une chaine de caract�res.
	 * @param str Chaine repr�sentant un mot cl� de la grammaire
	 * @return Mot cl� de la grammaire
	 */
	private ScriptTagDefinition getDefinition(final String str) {
		Assertion.checkNotNull(str);
		// ------------------------------------------------
		return tagDefinitionBySyntax.get(str);
	}

	ScriptTagContent parseTag(final String tagValue) {
		Assertion.checkNotNull(tagValue);
		// ---------------------------------------------------------------------
		final String value = tagValue.trim();
		for (final String key : orderedParsingTagList) {
			if (value.startsWith(key)) {
				String attribute = value.substring(key.length()).trim();
				attribute = attribute.replace("  ", " ");//On retire les espaces superflux pour eviter des pb de parsing
				if ("".equals(attribute)) {
					attribute = null; //Si il n'y a pas d'attribut on passe null
				}
				return new ScriptTagContent(getDefinition(key), attribute);
			}
		}
		throw new RuntimeException(StringUtil.format("{0} n'appartient pas a la grammaire : {1}", value, orderedParsingTagList));
	}
}
