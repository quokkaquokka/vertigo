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
package io.vertigo.util;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public final class MapBuilderTest {
	@Test
	public void testMap() {
		final Map<String, Integer> map = new MapBuilder<String, Integer>()
				.put("one", 1)
				.put("two", 2)
				.put("three", 3)
				.unmodifiable()
				.build();

		Assert.assertEquals(3, map.size());
		Assert.assertEquals(2, map.get("two").intValue());
	}

	@Test
	public void testModifiableMap() {
		final Map<String, Integer> map = new MapBuilder<String, Integer>()
				.put("one", 1)
				.put("two", 2)
				.put("three", 3)
				.build();

		map.put("nine", 9);
		Assert.assertEquals(4, map.size());
	}

	@Test(expected = Exception.class)
	public void testUnmodifiableMap() {
		final Map<String, Integer> map = new MapBuilder<String, Integer>()
				.put("one", 1)
				.put("two", 2)
				.put("three", 3)
				.unmodifiable()
				.build();

		map.put("nine", 9);
	}
}
