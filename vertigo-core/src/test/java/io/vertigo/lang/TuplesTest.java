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
package io.vertigo.lang;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tuples.
 *
 * @author pchretien
 */
public final class TuplesTest {
	@Test
	public void testTuple2() {
		final Tuples.Tuple2<String, String> homer = new Tuples.Tuple2<>("homer", "simpson");
		final Tuples.Tuple2<String, String> homer2 = new Tuples.Tuple2<>("homer", "simpson");
		final Tuples.Tuple2<String, String> marge = new Tuples.Tuple2<>("marge", "simpson");

		Assert.assertEquals(homer, homer);
		Assert.assertTrue(homer.equals(homer));
		Assert.assertEquals(homer.hashCode(), homer2.hashCode());
		Assert.assertEquals(homer, homer2);
		Assert.assertNotEquals(homer, marge);
		Assert.assertFalse(marge.equals(null));

		Assert.assertEquals("homer", homer.getVal1());
		Assert.assertEquals("simpson", homer.getVal2());

		Assert.assertEquals("marge", marge.getVal1());
		Assert.assertEquals("simpson", marge.getVal2());
	}

	@Test
	public void testTuple3() {
		final Tuples.Tuple3<String, String, String> homer = new Tuples.Tuple3<>("homer", "simpson", "M");
		final Tuples.Tuple3<String, String, String> homer2 = new Tuples.Tuple3<>("homer", "simpson", "M");
		final Tuples.Tuple3<String, String, String> marge = new Tuples.Tuple3<>("marge", "simpson", "F");

		Assert.assertEquals(homer, homer);
		Assert.assertTrue(homer.equals(homer));
		Assert.assertEquals(homer.hashCode(), homer2.hashCode());
		Assert.assertEquals(homer, homer2);
		Assert.assertNotEquals(homer, marge);
		Assert.assertFalse(marge.equals(null));

		Assert.assertEquals(homer, homer2);
		Assert.assertEquals(homer, homer);
		Assert.assertNotEquals(homer, marge);

		Assert.assertEquals("homer", homer.getVal1());
		Assert.assertEquals("simpson", homer.getVal2());
		Assert.assertEquals("M", homer.getVal3());

		Assert.assertEquals("marge", marge.getVal1());
		Assert.assertEquals("simpson", marge.getVal2());
		Assert.assertEquals("F", marge.getVal3());
	}
}
