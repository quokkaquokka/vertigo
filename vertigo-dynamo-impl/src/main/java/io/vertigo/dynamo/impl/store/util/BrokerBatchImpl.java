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
/**
" * vertigo - simple java starter
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
/**
 *
 */
package io.vertigo.dynamo.impl.store.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.vertigo.app.Home;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionUtil;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamox.task.TaskEngineSelect;
import io.vertigo.lang.Assertion;

/**
 * Implémentation du broker fonctionnant par lot.
 *
 * @param <D> Type d'objet métier.
 * @param <P> Type de la clef primaire.
 * @author jmforhan
 */
final class BrokerBatchImpl<D extends DtObject, P> implements BrokerBatch<D, P> {

	private static final String DOMAIN_PREFIX = DefinitionUtil.getPrefix(Domain.class);
	private static final char SEPARATOR = Definition.SEPARATOR;
	private static final int GET_LIST_PAQUET_SIZE = 1000;
	private final TaskManager taskManager;

	/**
	 * Constructor.
	 */
	BrokerBatchImpl(final TaskManager taskManager) {
		Assertion.checkNotNull(taskManager);
		//-----
		this.taskManager = taskManager;
	}

	private static String getDtcName(final DtDefinition dtDef) {
		return "DTC_" + dtDef.getLocalName();
	}

	/** {@inheritDoc} */
	@Override
	public DtList<D> getList(final DtDefinition dtDefinition, final Collection<P> idList) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(idList);
		//-----
		final DtList<D> dtc = new DtList<>(dtDefinition);
		// On regarde s'il y a quelquechose à faire
		if (idList.isEmpty()) {
			// Rien à faire
			return dtc;
		}
		// On génère une DTC d'identifiant
		final DtField idField = dtDefinition.getIdField().get();
		for (final P id : idList) {
			Assertion.checkNotNull(id);
			final D dto = (D) DtObjectUtil.createDtObject(dtDefinition);
			idField.getDataAccessor().setValue(dto, id);
			dtc.add(dto);
		}
		return getList(dtDefinition, idField.getName(), dtc);
	}

	private DtList<D> getList(final DtDefinition dtDefinition, final String fieldName, final DtList<D> dtc) {
		// On splitte la collection par paquet
		final Set<DtList<D>> set = new HashSet<>();
		DtList<D> tmp = null;
		for (int i = 0; i < dtc.size(); i++) {
			if (i % GET_LIST_PAQUET_SIZE == 0) {
				tmp = new DtList<>(dtc.getDefinition());
				set.add(tmp);
			}
			if (tmp != null) {
				tmp.add(dtc.get(i));
			}
		}
		// On génère la requête
		// Corps de la requete
		// On génère maintenant la requête proc batch
		final DtDefinition dtDef = dtc.getDefinition();
		final String dtcName = getDtcName(dtDef);
		final String inDtcName = dtcName + "_IN";

		final String request = new StringBuilder("select * from ")
				.append(dtDef.getLocalName())
				.append(" where ")
				.append(fieldName)
				.append(" in (#")
				.append(inDtcName)
				.append(".ROWNUM.")
				.append(fieldName)
				.append("#)")
				.toString();

		// Exécution de la tache
		final Domain dtcDomain = Home.getApp().getDefinitionSpace().resolve(DOMAIN_PREFIX + SEPARATOR + dtDef.getName() + "_DTC", Domain.class);
		final String taskName = "TK_LOAD_BY_LST_" + fieldName + "_" + dtDef.getLocalName();

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)
				.withEngine(TaskEngineSelect.class)
				.withDataSpace(dtDef.getDataSpace())
				.withRequest(request)
				.addInAttribute(inDtcName, dtcDomain, true)
				.withOutAttribute("out", dtcDomain, true)
				.build();

		// On exécute par paquet
		final DtList<D> ret = new DtList<>(dtDefinition);
		for (final DtList<D> paq : set) {
			/* Création de la tache. */
			final TaskBuilder taskBuilder = new TaskBuilder(taskDefinition)
					.addValue(inDtcName, paq);
			// Exécution de la tache
			final DtList<D> result = taskManager
					.execute(taskBuilder.build())
					.getResult();
			ret.addAll(result);
		}
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public Map<P, D> getMap(final DtDefinition dtDefinition, final Collection<P> idList) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(idList);
		//-----
		final DtField idField = dtDefinition.getIdField().get();
		final Map<P, D> map = new HashMap<>();
		for (final D dto : getList(dtDefinition, idList)) {
			map.put((P) idField.getDataAccessor().getValue(dto), dto);
		}
		return map;
	}

	/** {@inheritDoc} */
	@Override
	public <O> DtList<D> getListByField(final DtDefinition dtDefinition, final String fieldName, final Collection<O> value) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(fieldName);
		//-----
		Assertion.checkNotNull(value);
		final DtList<D> dtc = new DtList<>(dtDefinition);
		// On regarde s'il y a quelquechose à faire
		if (value.isEmpty()) {
			// Rien à faire
			return dtc;
		}
		// On génère une DTC d'identifiant
		final DtField field = dtDefinition.getField(fieldName);
		for (final O sel : value) {
			Assertion.checkNotNull(sel);
			final D dto = (D) DtObjectUtil.createDtObject(dtDefinition);
			field.getDataAccessor().setValue(dto, sel);
			dtc.add(dto);
		}
		return getList(dtDefinition, field.getName(), dtc);
	}

	/** {@inheritDoc} */
	@Override
	public <O> Map<O, DtList<D>> getMapByField(final DtDefinition dtDefinition, final String fieldName, final Collection<O> value) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(fieldName);
		//-----
		final DtField field = dtDefinition.getField(fieldName);
		final Map<O, DtList<D>> map = new HashMap<>();
		for (final D dto : getListByField(dtDefinition, fieldName, value)) {
			final O key = (O) field.getDataAccessor().getValue(dto);
			if (!map.containsKey(key)) {
				final DtList<D> dtc = new DtList<>(dtDefinition);
				map.put(key, dtc);
			}
			map.get(key).add(dto);
		}
		return map;
	}
}
