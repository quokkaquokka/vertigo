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
package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.app.Home;
import io.vertigo.core.spaces.definiton.DefinitionReference;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.JsonExclude;
import io.vertigo.lang.MessageText;

/**
 * This class defines the structure of a field. 
 *
 * A field represents a named and typed data
 *
 * A field
 *   - has a name
 *   - has a domain
 *   - has a label 
 *   - can be required 
 *   - can be persistent 
 *   - can be dynamic
 *
 * @author  fconstantin, pchretien , npiedeloup
 */
public final class DtField implements DtFieldName {
	/** Field definition Prefix. */
	public static final String PREFIX = "FLD_";

	/**
	 * This enum lists all types that can be used by a field.
	 * The most common types are ID and DATA
	 */
	public enum FieldType {
		/**
		 * identity 
		 */
		ID,

		/**
		 * a simple data field 
		 */
		DATA,

		/**
		 * a link towards an other object
		 */
		FOREIGN_KEY,

		/**
		 * a compute field
		 */
		COMPUTED
	}

	private final String name;
	private final FieldType type;
	private final boolean required;
	private final DefinitionReference<Domain> domainRef;
	private final MessageText label;
	private final boolean persistent;

	/** Cas des FK ; référence à une FK. */
	private final String fkDtDefinitionName;

	/** ComputedExpression des champs Computed. */
	private final ComputedExpression computedExpression;

	private final String id;

	private final boolean dynamic;
	@JsonExclude
	private final DataAccessor dataAccessor;
	private final boolean sort;
	private final boolean display;

	/**
	 * Constructor.
	 * 
	 * @param id the ID of the field 
	 * @param fieldName the name of the field
	 * @param type the type of the field 
	 * @param domain the domain of the field 
	 * @param label the label of the field 
	 * @param required if the field is required
	 * @param persistent if the field is persistent
	 * @param fkDtDefinitionName Nom de la DtDefinition de la FK (noNull si type=FK)
	 * @param computedExpression Expression du computed (noNull si type=Computed)
	 * @param dynamic if the field is dynamic
	 * @param sort if this field is used for sorting
	 * @param display if this field is used for display
	 */
	DtField(final String id,
			final String fieldName,
			final FieldType type,
			final Domain domain,
			final MessageText label,
			final boolean required,
			final boolean persistent,
			final String fkDtDefinitionName,
			final ComputedExpression computedExpression,
			final boolean dynamic,
			final boolean sort,
			final boolean display) {
		Assertion.checkArgNotEmpty(id);
		Assertion.checkNotNull(type);
		Assertion.checkNotNull(domain);
		Assertion.checkNotNull(type);
		//-----
		this.id = id;
		domainRef = new DefinitionReference<>(domain);
		this.type = type;
		this.required = required;
		//-----
		Assertion.checkNotNull(fieldName);
		Assertion.checkArgument(fieldName.length() <= 30, "the name of the field {0} has a limit size of 30", fieldName);
		Assertion.checkArgument(fieldName.toUpperCase().equals(fieldName), "the name of the field {0} must be in upperCase", fieldName);
		name = fieldName;
		//-----
		Assertion.checkNotNull(label);
		this.label = label;
		//-----
		Assertion.checkArgument(!(getType() == FieldType.COMPUTED && persistent), "a computed field can't be persistent");
		this.persistent = persistent;
		//-----
		if (getType() == FieldType.FOREIGN_KEY) {
			Assertion.checkNotNull(fkDtDefinitionName, "Le champ {0} de type clé étrangère doit référencer une définition ", fieldName);
		} else {
			Assertion.checkState(fkDtDefinitionName == null, "Le champ {0} n''est pas une clé étrangère", fieldName);
		}
		this.fkDtDefinitionName = fkDtDefinitionName;
		//-----
		if (getType() == DtField.FieldType.COMPUTED) {
			Assertion.checkNotNull(computedExpression, "the field {0}, declared as computed, must have an expression", fieldName);
		} else {
			Assertion.checkState(computedExpression == null, "the field {0}, not declared as computed, must have an empty expression", fieldName);
		}
		this.computedExpression = computedExpression;
		//-----
		this.dynamic = dynamic;
		this.sort = sort;
		this.display = display;
		//-----
		dataAccessor = new DataAccessor(this);
	}

	/**
	 * @return the key of the resource (i18n)
	 */
	public String getResourceKey() {
		return id;
	}

	/** {@inheritDoc} */
	@Override
	public String name() {
		return name;
	}

	/**
	 * @return the name of the field
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return if the field is required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @return the type of the field
	 */
	public FieldType getType() {
		return type;
	}

	/**
	 * @return the domain of the field
	 */
	public Domain getDomain() {
		return domainRef.get();
	}

	/**
	 * @return the label of the field
	 */
	public MessageText getLabel() {
		return label;
	}

	/**
	 * Gestion de la persistance.
	 * @return Si le champ est persisté.
	 */
	public boolean isPersistent() {
		return persistent;
	}

	/**
	 *  @return DtDefinition de la ForeignKey (caractère obligatoire lié au type)
	 */
	//Todo changer le nom
	public DtDefinition getFkDtDefinition() {
		Assertion.checkNotNull(fkDtDefinitionName);
		//-----
		return Home.getApp().getDefinitionSpace().resolve(fkDtDefinitionName, DtDefinition.class);
	}

	/**
	 * Expression dans le cas d'un champ calculé.
	 *  @return ComputedExpression du champs calculé (caractère obligatoire lié au type)
	 */
	public ComputedExpression getComputedExpression() {
		Assertion.checkNotNull(computedExpression);
		//-----
		return computedExpression;
	}

	/**
	 * Returns the way to access the data.
	 * @return the data accessor.
	 */
	public DataAccessor getDataAccessor() {
		return dataAccessor;
	}

	/**
	 * @return if the field is dynamic
	 */
	public boolean isDynamic() {
		return dynamic;
	}

	/**
	 * @return if this field is used for sorting
	 */
	public boolean isSort() {
		return sort;
	}

	/**
	 * @return if this field is used for display
	 */
	public boolean isDisplay() {
		return display;
	}
}
