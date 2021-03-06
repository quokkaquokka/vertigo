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
package io.vertigo.tempo.impl.scheduler;

import java.util.Date;

import io.vertigo.lang.Plugin;
import io.vertigo.tempo.job.metamodel.JobDefinition;

/**
 * Plugin de lancement des Jobs.
 * @author pchretien
 */
public interface SchedulerPlugin extends Plugin {
	/**
	 * Programme un job pour exécution à une fréquence donnée en secondes.
	 * @param periodInSecond Fréquence d'exécution en secondes
	 */
	void scheduleEverySecondInterval(final JobDefinition jobDefinition, int periodInSecond);

	/**
	 * Programme un job pour exécution chaque jour à heure et minute fixe.
	 * <br/>Si il y a besoin de programmer un job pour exécution à jour fixe dans la semaine
	 * ou dans le mois, il peut être programmé un job chaque puis conditioner l'exécution selon la
	 * date courante en utilisant la classe Calendar.
	 * @param hour Heure fixe d'exécution
	 * @param minute Minute fixe d'exécution
	 */
	void scheduleEveryDayAtHourMinute(final JobDefinition jobDefinition, int hour, int minute);

	/**
	 * Programme un job pour une seul exécution à une date donnée.
	 * @param date Date d'exécution
	 */
	void scheduleAtDate(final JobDefinition jobDefinition, Date date);

	/**
	 * Programme un job pour une seul exécution immédiate.
	 */
	void scheduleNow(final JobDefinition jobDefinition);
}
