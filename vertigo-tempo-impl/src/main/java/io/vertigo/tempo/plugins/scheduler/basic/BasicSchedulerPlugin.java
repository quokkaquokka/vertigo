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
package io.vertigo.tempo.plugins.scheduler.basic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.tempo.impl.scheduler.SchedulerPlugin;
import io.vertigo.tempo.job.JobManager;
import io.vertigo.tempo.job.metamodel.JobDefinition;

/**
 * Implémentation de JobManager prévue pour un serveur sans cluster (une seule JVM).
 *
 * Cette implémentation est transactionnelle : une transaction est démarrée avant
 * l'appelle de Job.execute() et elle est commitée s'il n'y a pas d'erreur ou rollbackée sinon.
 *
 * Si ce JobManager est utilisé sur plusieurs JVM en parallèle (ferme ou cluster de serveur
 * d'application), les jobs seront exécutés en parallèle sur chaque instance de serveur.
 *
 * Pour éviter d'exécuter les jobs en parallèle, il vous est possible d'initialiser certains
 * jobs sur une seule jvm avec un paramètre dépendant de l'instance du serveur d'application
 * (un paramètre dans un des contextes tomcat ou une propriété système par exemple).
 *
 * Stratégie implémentée bloque l'exécution en parallèle de deux jobs de même nom
 * (attention de ne pas multiplier à l'infini les noms de job et donc les threads).
 *
 * Lorsque close() est appelé sur ce jobManager (undeploy de la webapp), les jobs
 * éventuellement en cours peuvent tester régulièrement Thread.currentThread().isInterrupted() pour
 * savoir s'il est préférable de s'arrêter promptement sans attendre la fin du job.
 *
 * @author evernat
 */
public final class BasicSchedulerPlugin implements SchedulerPlugin, Activeable {

	/**
	 * Pool de timers permettant l'exécution des Jobs.
	 */
	private final TimerPool timerPool = new TimerPool();
	private boolean active;
	private final JobManager jobManager;

	@Inject
	public BasicSchedulerPlugin(final JobManager jobManager) {
		Assertion.checkNotNull(jobManager);
		//-----
		this.jobManager = jobManager;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		active = true;
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		active = false;
		timerPool.close();
	}

	private void checkActive() {
		Assertion.checkArgument(active, "le manager n'est pas dans un état actif");
	}

	private static Logger getLogger(final String jobName) {
		return Logger.getLogger(jobName);
	}

	/** {@inheritDoc} */
	@Override
	public void scheduleEverySecondInterval(final JobDefinition jobDefinition, final int periodInSecond) {
		checkActive();
		Assertion.checkArgument(periodInSecond <= 7 * 24 * 60 * 60, "La période doit être inférieure à une semaine");
		//-----
		final TimerTask task = createTimerTask(jobDefinition);
		final int startDelay = periodInSecond;
		// on utilise schedule et non scheduleAtFixedRate car c'est la période inter-exécution
		// qui importe et non l'intervalle avec la référence de démarrage
		// (ainsi pas de rafales si une lenteur momentanée survient)
		timerPool.getTimer(jobDefinition.getName()).schedule(task, startDelay * 1000L, periodInSecond * 1000L);
		getLogger(jobDefinition.getName()).info("Job " + jobDefinition.getName() + " programmé toutes les " + periodInSecond + " s");

		//TODO : bug détecté sur u projet : La task s'arrête et n'est plus relancée :
		//- soit exception dans le run ? normalement protégé par le catch (Throwable)
		//- soit task plantée au milieu ? ajouter une notion de TimeOut, et une supervision des excutions, pas forcément de correction mais au moins une alerte
	}

	/** {@inheritDoc} */
	@Override
	public void scheduleEveryDayAtHourMinute(final JobDefinition jobDefinition, final int hour, final int minute) {
		checkActive();
		//a chaque exécution il est nécessaire de reprogrammer l'execution.
		final Date nextExecutionDate = getNextExecutionDate(hour, minute);
		scheduleAtDate(jobDefinition, nextExecutionDate);

		//a chaque exécution il est nécessaire de reprogrammer l'execution.
		final Date nextReschedulerDate = new Date(nextExecutionDate.getTime() + 1 * 60 * 1000); //on reprogramme à l'heure dite + 1min (comme on est sur le m^me timer elle passera après
		final TimerTask task = createRescheduledTimerTask(jobDefinition, hour, minute);
		timerPool.getTimer(jobDefinition.getName()).schedule(task, nextReschedulerDate);
		log("Tache de reprogrammation du Job ", jobDefinition, nextReschedulerDate);
	}

	private static void log(final String info, final JobDefinition jobDefinition, final Date date) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRANCE);
		getLogger(jobDefinition.getName()).info(info + jobDefinition.getName() + " programmé pour " + dateFormat.format(date));
	}

	/** {@inheritDoc} */
	@Override
	public void scheduleNow(final JobDefinition jobDefinition) {
		checkActive();
		final TimerTask task = createTimerTask(jobDefinition);
		timerPool.getTimer(jobDefinition.getName()).schedule(task, new Date());
	}

	/** {@inheritDoc} */
	@Override
	public void scheduleAtDate(final JobDefinition jobDefinition, final Date date) {
		checkActive();
		final TimerTask task = createTimerTask(jobDefinition);
		timerPool.getTimer(jobDefinition.getName()).schedule(task, date);
		log("Job ", jobDefinition, date);
	}

	private TimerTask createTimerTask(final JobDefinition jobDefinition) {
		return new BasicTimerTask(jobDefinition, jobManager);
	}

	private TimerTask createRescheduledTimerTask(final JobDefinition jobDefinition, final int hour, final int minute) {
		return new ReschedulerTimerTask(this, jobDefinition, hour, minute);
	}

	private static Date getNextExecutionDate(final int hour, final int minute) {
		Assertion.checkArgument(hour >= 0 && hour <= 23, "hour doit être compris entre 0 et 23");
		Assertion.checkArgument(minute >= 0 && minute <= 59, "minute doit être compris entre 0 et 59");
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
			// on utilise add et non roll pour ne pas tourner en boucle le 31/12
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		return calendar.getTime();
	}

	//=========================================================================
	//==================Gestion du rendu et des interactions===================
	//=========================================================================
	//
	//	/** {@inheritDoc} */
	//	@Override
	//	public void toHtml(final PrintStream out) throws Exception {
	//		timerPool.toHtml(out);
	//		out.println("<br/>");
	//		super.toHtml(out);
	//	}
	//
	//	/** {@inheritDoc} */
	//	public ManagerSummaryInfo getMainSummaryInfo() {
	//		final ManagerSummaryInfo managerSummaryInfo = new ManagerSummaryInfo();
	//		managerSummaryInfo.setValue(timerPool.getTimerCount());
	//		managerSummaryInfo.setValueTitle("jobs");
	//		return managerSummaryInfo;
	//	}
	//
	//	/** {@inheritDoc} */
	//	public List<ManagerSummaryInfo> getSummaryInfos() {
	//		final ManagerSummaryInfo managerSummaryInfo = new ManagerSummaryInfo();
	//		managerSummaryInfo.setInfoTitle("SingleJVMJobManager");
	//		return Collections.<ManagerSummaryInfo> singletonList(managerSummaryInfo);
	//	}

	//=========================================================================
	//=============================POOL de TIMER===============================
	//=========================================================================
	static class TimerPool {
		// cette implémentation est simplement basée sur la classe java.util.Timer du JDK
		private final Map<String, Timer> timerMap = new HashMap<>();

		// pour interrupt
		private final Map<String, Thread> threadMap = new HashMap<>();

		//		private int getTimerCount() {
		//			return timerMap.size();
		//		}

		synchronized Timer getTimer(final String jobName) {
			//Synchronized car appelée lors de la programation des Timers,
			//la plupart sont programmés dans lors de l'initialisation,
			//mais il est possible de programmer sur des evenements métiers.
			//Utilisé QUE lors des programmations, pas à l'exec.
			Timer timer = timerMap.get(jobName);
			if (timer == null) {
				// le timer est démon pour ne pas empêcher l'arrêt de la jvm,
				// timerName est utilisé comme nom du thread java
				timer = new Timer(jobName, true);
				timerMap.put(jobName, timer);
				final TimerTask registrerThreadTimerTask = new TimerTask() {
					/** {@inheritDoc} */
					@Override
					public void run() {
						registrerTimerThread(Thread.currentThread());
					}
				};
				timer.schedule(registrerThreadTimerTask, new Date());
			}
			return timer;
		}

		void registrerTimerThread(final Thread thread) {
			threadMap.put(thread.getName(), thread);
		}

		void close() {
			//La méthode close est appelée par le gestionnaire des managers.
			//Elle n'a pas besoin d'être synchronisée.
			// on cancel les timers pour qu'ils n'aient plus de schedule
			for (final Timer timer : timerMap.values()) {
				timer.cancel();
			}
			timerMap.clear();
			// on appelle interrupt() sur les threads pour qu'un job en cours
			// puisse tester Thread.currentThread().isInterrupted() et s'arrêter promptement
			for (final Thread thread : threadMap.values()) {
				thread.interrupt();
			}
			threadMap.clear();
		}

		//		void toHtml(final PrintStream out) throws Exception {
		//			out.print("\nNoms des jobs: ");
		//			String separator = "";
		//			for (final String jobName : timerMap.keySet()) {
		//				out.print(separator);
		//				out.print(jobName);
		//				separator = ", ";
		//			}
		//		}
	}

}
