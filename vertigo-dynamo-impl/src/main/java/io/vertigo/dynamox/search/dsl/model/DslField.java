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
package io.vertigo.dynamox.search.dsl.model;

import io.vertigo.lang.Assertion;

/**
 * Single field definition.
 * (preBody)(fieldName)(postBody)
 * @author npiedeloup
 */
public final class DslField {
	private final String preBody;
	private final String fieldName;
	private final String postBody;

	/**
	 * @param preBody String before body
	 * @param fieldName Index's fieldName
	 * @param postBody String after body
	 */
	public DslField(final String preBody, final String fieldName, final String postBody) {
		Assertion.checkNotNull(preBody);
		Assertion.checkNotNull(fieldName);
		Assertion.checkNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.fieldName = fieldName;
		this.postBody = postBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return preBody + fieldName + postBody;
	}

	/**
	 * @return preBody
	 */
	public String getPreBody() {
		return preBody;
	}

	/**
	 * @return fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @return postBody
	 */
	public String getPostBody() {
		return postBody;
	}

}
