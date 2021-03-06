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
package io.vertigo.dynamo.impl.store.datastore;

import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.impl.store.StoreEvent;
import io.vertigo.dynamo.impl.store.datastore.cache.CacheDataStore;
import io.vertigo.dynamo.impl.store.datastore.logical.LogicalDataStoreConfig;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.datastore.DataStore;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionSynchronization;
import io.vertigo.lang.Assertion;

/**
 * Implementation of DataStore.
 * @author pchretien
 */
public final class DataStoreImpl implements DataStore {
	/** Le store est le point d'accès unique à la base (sql, xml, fichier plat...). */
	private final CacheDataStore cacheDataStore;
	private final LogicalDataStoreConfig logicalStoreConfig;
	private final EventBusManager eventBusManager;
	private final VTransactionManager transactionManager;

	/**
	 * Constructor.
	 * @param storeManager Store manager
	 * @param transactionManager Transaction manager
	 * @param eventBusManager Event bus manager
	 * @param dataStoreConfig config of the dataStore
	 */
	public DataStoreImpl(final StoreManager storeManager, final VTransactionManager transactionManager, final EventBusManager eventBusManager, final DataStoreConfigImpl dataStoreConfig) {
		Assertion.checkNotNull(storeManager);
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(eventBusManager);
		Assertion.checkNotNull(dataStoreConfig);
		//-----
		logicalStoreConfig = dataStoreConfig.getLogicalStoreConfig();
		cacheDataStore = new CacheDataStore(storeManager, eventBusManager, dataStoreConfig);
		this.eventBusManager = eventBusManager;
		this.transactionManager = transactionManager;
	}

	private DataStorePlugin getPhysicalStore(final DtDefinition dtDefinition) {
		return logicalStoreConfig.getPhysicalDataStore(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> D readForUpdate(final URI<D> uri) {
		Assertion.checkNotNull(uri);
		//-----
		final DtDefinition dtDefinition = uri.getDefinition();
		final D value = getPhysicalStore(dtDefinition).<D> readForUpdate(dtDefinition, uri);
		//-----
		fireAfterCommit(StoreEvent.Type.Update, uri);
		return value;
	}

	private void fireAfterCommit(final StoreEvent.Type evenType, final URI<?> uri) {
		transactionManager.getCurrentTransaction().addAfterCompletion(new VTransactionSynchronization() {
			@Override
			public void afterCompletion(final boolean txCommitted) {
				if (txCommitted) {//send event only is tx successful
					eventBusManager.post(new StoreEvent(evenType, uri));
				}
			}
		});
	}

	//--- Transactionnal Event

	/** {@inheritDoc} */
	@Override
	public void create(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		getPhysicalStore(dtDefinition).create(dtDefinition, dto);
		//-----
		fireAfterCommit(StoreEvent.Type.Create, new URI(dtDefinition, DtObjectUtil.getId(dto)));
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
	}

	/** {@inheritDoc} */
	@Override
	public void update(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		getPhysicalStore(dtDefinition).update(dtDefinition, dto);
		//-----
		fireAfterCommit(StoreEvent.Type.Update, new URI(dtDefinition, DtObjectUtil.getId(dto)));
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
	}

	/** {@inheritDoc} */
	@Override
	public void merge(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		getPhysicalStore(dtDefinition).merge(dtDefinition, dto);
		//-----
		fireAfterCommit(StoreEvent.Type.Update, new URI(dtDefinition, DtObjectUtil.getId(dto)));
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final URI<? extends DtObject> uri) {
		Assertion.checkNotNull(uri);
		//-----
		final DtDefinition dtDefinition = uri.getDefinition();
		getPhysicalStore(dtDefinition).delete(dtDefinition, uri);
		//-----
		fireAfterCommit(StoreEvent.Type.Delete, uri);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> D read(final URI<D> uri) {
		Assertion.checkNotNull(uri);
		//-----
		final D dto = cacheDataStore.<D> load(uri);
		//-----
		Assertion.checkNotNull(dto, "L''objet {0} n''a pas été trouvé", uri);
		return dto;
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> DtList<D> findAll(final DtListURI uri) {
		Assertion.checkNotNull(uri);
		//-----
		final DtList<D> dtc = cacheDataStore.loadList(uri);
		//-----
		Assertion.checkNotNull(dtc);
		return dtc;
	}

	/** {@inheritDoc} */
	@Override
	public int count(final DtDefinition dtDefinition) {
		return getPhysicalStore(dtDefinition).count(dtDefinition);
	}
}
