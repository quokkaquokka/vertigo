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
package io.vertigo.commons.analytics;

import io.vertigo.lang.Manager;

/**
 * Main access to all analytics functions.
 *
 * @author pchretien, npiedeloup
 */
public interface AnalyticsManager extends Manager {

	/**
	 * @return collect agent
	 */
	AnalyticsAgent getAgent();

	/**
	 * Start process (may be a sub-process with its own metrics).
	 * @param processType process type
	 * @param category process category
	 * @return collect tracker
	 */
	AnalyticsTracker startTracker(final String processType, final String category);

	/**
	 * Start process logging (no subProcess, only local metrics).
	 * @param processType process type
	 * @param category process category
	 * @return collect tracker
	 */
	AnalyticsTracker startLogTracker(final String processType, final String category);

}
