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
package io.vertigo.core.param;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;

/**
 * @author pchretien
 */
public abstract class AbstractParamManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private ParamManager paramManager;

	@Test
	public void test1() {
		final String value = paramManager.getStringValue("server.host");
		Assert.assertEquals("wiki", value);
	}

	@Test(expected = Exception.class)
	public void test2() {
		paramManager.getStringValue("server.wrong");
	}

	@Test
	public void test3() {
		final int value = paramManager.getIntValue("server.port");
		Assert.assertEquals(8080, value);
	}

	@Test(expected = Exception.class)
	public void test4() {
		paramManager.getIntValue("server.active");
	}

	@Test
	public void test5() {
		final boolean value = paramManager.getBooleanValue("server.active");
		Assert.assertTrue(value);
	}

	@Test
	public void test6() {
		final boolean value = paramManager.getBooleanValue("server.verbose");
		Assert.assertFalse(value);
	}

	@Test(expected = Exception.class)
	public void test7() {
		paramManager.getBooleanValue("server.port");
	}
}
