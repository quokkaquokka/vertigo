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
package io.vertigo.struts2.core;

import io.vertigo.core.exception.VUserException;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.MessageText;
import io.vertigo.core.util.StringUtil;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

import java.util.ArrayList;
import java.util.List;

public final class ValidationUserException extends VUserException {
	private static final long serialVersionUID = 7214302356640340103L;

	private static final MessageText VALIDATE_ERROR_MESSAGE_TEXT = new MessageText("Il y a des erreurs, vous devez corriger votre saisie :", null);

	private final List<UiError> uiErrors = new ArrayList<>();

	ValidationUserException(final List<UiError> uiErrors) {
		super(VALIDATE_ERROR_MESSAGE_TEXT);
		this.uiErrors.addAll(uiErrors);
	}

	public ValidationUserException(final MessageText messageText, final DtObject dto, final String fieldName) {
		super(messageText);
		Assertion.checkNotNull(dto, "L'objet est obligatoire");
		Assertion.checkArgNotEmpty(fieldName, "Le champs est obligatoire");
		//---------------------------------------------------------------------
		final DtField dtField = DtObjectUtil.findDtDefinition(dto).getField(StringUtil.camelToConstCase(fieldName));
		uiErrors.add(new UiError(dto, dtField, messageText));
	}

	public List<UiError> getUiErrors() {
		return uiErrors;
	}

	/*	private DtField getDtField(final DtObject dto, final String fieldName) {
			return DtObjectUtil.findDtDefinition(dto).getField(StringUtil.camelToConstCase(fieldName));
		}
		*/
}