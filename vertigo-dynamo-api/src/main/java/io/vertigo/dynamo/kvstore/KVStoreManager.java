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
package io.vertigo.dynamo.kvstore;

import java.util.List;

import io.vertigo.lang.Manager;
import io.vertigo.lang.Option;

/**
* Key Value Store.
*
* A store is composed of multiple KVStorePlugins.
*
* +---store
*     +---KVStorePlugin : {name:plants  type:BerkeleyDB}
*         +---collection : flowers
*         +---collection : trees
*         +---collection : fungi
*     +---KVStorePlugin : {name:UISecurityStore  type:DelayedBerkeleyDB}
*         +---collection : sessions
*
* @author pchretien
*/
public interface KVStoreManager extends Manager {

	/**
	 * @param collection the collection
	 * @return count of elements into collection
	 */
	int count(String collection);

	/**
	 * Adds an element defined by an id in a collection.
	 *
	 * @param collection the collection
	 * @param id the id
	 * @param element the element
	 */
	void put(String collection, String id, Object element);

	/**
	 * Removes an element defined by an id from a collection.
	 * If the collection doesn't contain the is then a exception is thrown.
	 *
	 * @param collection the collection
	 * @param id the id
	 */
	void remove(String collection, String id);

	/**
	 * Removes all elements from a collection.
	 * @param collection the collection
	 */
	void clear(String collection);

	/**
	 * Finds the optional element to which the id is mapped inside the specified collection.
	 * If the element is not found then an empty option is returned.
	 * @param <C> Element type
	 * @param collection the collection
	 * @param id the id
	 * @param clazz the type of the searched element
	 * @return the option
	 */
	<C> Option<C> find(String collection, String id, Class<C> clazz);

	/**
	 * Finds all elements contained inside the specified collection.	 *
	 * @param <C> Element type
	 * @param collection the collection
	 * @param skip the position from which the elements are returned
	 * @param limit the limit size of elements
	 * @param clazz the type of the searched element
	 * @return the list of elements.
	 */
	<C> List<C> findAll(String collection, int skip, Integer limit, Class<C> clazz);
}
