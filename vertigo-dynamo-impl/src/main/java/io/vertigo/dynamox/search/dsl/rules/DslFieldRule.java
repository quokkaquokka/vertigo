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
package io.vertigo.dynamox.search.dsl.rules;

import java.util.List;

import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.dynamox.search.dsl.model.DslField;

/**
 * Parsing rule for Field.
 * (preField)(fieldName)(postField):
 * @author npiedeloup
 */
final class DslFieldRule extends AbstractRule<DslField, List<?>> {

	/** {@inheritDoc} */
	@Override
	protected Rule<List<?>> createMainRule() {
		return new SequenceRule(
				DslSyntaxRules.PRE_MODIFIER_VALUE,//0
				DslSyntaxRules.WORD, //1
				DslSyntaxRules.POST_MODIFIER_VALUE); //2
	}

	/** {@inheritDoc} */
	@Override
	protected DslField handle(final List<?> parsing) {
		final String preField = (String) parsing.get(0);
		final String fieldName = (String) parsing.get(1);
		final String postField = (String) parsing.get(2);
		return new DslField(preField, fieldName, postField);
	}

}
