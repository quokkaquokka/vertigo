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
package io.vertigo.dynamo.environment.plugins.loaders.kpr.definition;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.commons.parser.NotFoundException;
import io.vertigo.commons.parser.Parser;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslPackageRule;

public final class DslPackageRuleTest {
	private static final DslPackageRule packageRule = new DslPackageRule();

	@Test
	public void testExpression() throws NotFoundException {
		Parser<String> parser = packageRule.createParser();
		int index = parser.parse("package io.vertigo  xxxx", 0);
		Assert.assertEquals("io.vertigo", parser.get());
		Assert.assertEquals("package io.vertigo  ".length(), index);
	}

	@Test(expected = Exception.class)
	public void testMalFormedExpression() throws NotFoundException {
		Parser<String> parser = packageRule.createParser();
		parser.parse("packageio.vertigo", 0);
		Assert.fail("package : " + parser.get());
	}

	@Test(expected = Exception.class)
	public void testMalFormedExpression2() throws NotFoundException {
		Parser<String> parser = packageRule.createParser();
		parser.parse("  packageio.vertigo", 0);
		Assert.fail("package : " + parser.get());
	}
}
