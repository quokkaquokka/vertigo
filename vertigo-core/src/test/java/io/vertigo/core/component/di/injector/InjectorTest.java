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
package io.vertigo.core.component.di.injector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.core.component.di.DIException;
import io.vertigo.core.component.di.data.A;
import io.vertigo.core.component.di.data.B;
import io.vertigo.core.component.di.data.B2;
import io.vertigo.core.component.di.data.E;
import io.vertigo.core.component.di.data.F;
import io.vertigo.core.component.di.data.P;
import io.vertigo.core.component.di.data.P2;
import io.vertigo.core.component.di.data.P3;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Container;

/**
 * Voir sur reactor pour l'arbre des dépendances des objets A==>F.
 * @author pchretien
 */
public final class InjectorTest {
	private static class MyContainer implements Container {
		private final Map<String, Object> map = new HashMap<>();

		@Override
		public boolean contains(final String id) {
			Assertion.checkArgNotEmpty(id);
			//-----
			return map.containsKey(id);
		}

		public void put(final String id, final Object object) {
			Assertion.checkArgNotEmpty(id);
			Assertion.checkNotNull(object);
			//-----
			map.put(id, object);
		}

		@Override
		public <C> C resolve(final String id, final Class<C> componentClass) {
			Assertion.checkArgNotEmpty(id);
			Assertion.checkNotNull(componentClass);
			//-----
			final Object object = map.get(id);
			Assertion.checkNotNull(object, "{0} not found", id);
			return componentClass.cast(object);
		}

		@Override
		public Set<String> keySet() {
			return map.keySet();
		}
	}

	private static void nop(final Object o) {
		//NOP
	}

	@Test
	public void testA() {
		final A a = Injector.newInstance(A.class, new Container() {

			@Override
			public boolean contains(final String id) {
				return false;
			}

			@Override
			public <T> T resolve(final String id, final Class<T> componentClass) {
				return null;
			}

			@Override
			public Set<String> keySet() {
				return Collections.EMPTY_SET;
			}
		});
		nop(a);
	}

	@Test(expected = DIException.class)
	public void testBFail() {
		final B b = Injector.newInstance(B.class, new Container() {

			@Override
			public boolean contains(final String id) {
				return false;
			}

			@Override
			public <T> T resolve(final String id, final Class<T> componentClass) {
				return null;
			}

			@Override
			public Set<String> keySet() {
				return Collections.EMPTY_SET;
			}
		});
		nop(b);
	}

	@Test(expected = DIException.class)
	public void testB2() {
		final MyContainer container = new MyContainer();
		final A a = Injector.newInstance(A.class, container);
		container.put("a", a);
		final B2 b2 = Injector.newInstance(B2.class, container);
		nop(b2);
		Assert.fail();
	}

	@Test
	public void testB() {
		final MyContainer container = new MyContainer();
		final A a = Injector.newInstance(A.class, container);
		container.put("a", a);
		final B b = Injector.newInstance(B.class, container);
		Assert.assertEquals(a, b.getA());
	}

	@Test
	public void testE() {
		final MyContainer container = new MyContainer();
		final A a = Injector.newInstance(A.class, container);
		container.put("a", a);
		container.put("p3", new P3());
		E e = Injector.newInstance(E.class, container);
		Assert.assertTrue(e.getA().isPresent());
		Assert.assertEquals(a, e.getA().get());
		Assert.assertFalse(e.getB().isPresent());
		Assert.assertEquals(0, e.getPPlugins().size());
		Assert.assertEquals(0, e.getP2Plugins().size());
		//-----
		container.put("p", new P());
		container.put("p#1", new P());
		container.put("pen", new P2());
		container.put("pen#1", new P2());
		container.put("pen#2", new P2());
		e = Injector.newInstance(E.class, container);
		Assert.assertTrue(e.getA().isPresent());
		Assert.assertEquals(a, e.getA().get());
		Assert.assertFalse(e.getB().isPresent());
		Assert.assertEquals(2, e.getPPlugins().size());
		Assert.assertEquals(3, e.getP2Plugins().size());
	}

	@Test
	public void testF() {
		final MyContainer container = new MyContainer();
		final A a = Injector.newInstance(A.class, container);
		container.put("a", a);
		container.put("param1", "test1");
		container.put("param2", "test2");
		container.put("param3", "test3");
		final F f = Injector.newInstance(F.class, container);
		Assert.assertEquals(f.getA(), a);
		Assert.assertEquals(f.getParam1(), "test1");
		Assert.assertEquals(f.getParam2(), "test2");
		Assert.assertTrue(f.getParam3().isPresent());
		Assert.assertEquals(f.getParam3().get(), "test3");
		Assert.assertFalse(f.getParam4().isPresent());
	}
}
