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
package io.vertigo.dynamo.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamock.domain.famille.Famille;
import io.vertigo.lang.Option;

/**
 *
 * @author dchallas
 */
public abstract class AbstractCollectionsManagerTest extends AbstractTestCaseJU4 {
	private static final String Ba = "Ba";
	private static final String aaa = "aaa";
	private static final String bb = "bb";
	private DtDefinition dtDefinitionFamille;
	@Inject
	private CollectionsManager collectionsManager;

	/** {@inheritDoc} */
	@Override
	protected void doSetUp() {
		dtDefinitionFamille = DtObjectUtil.findDtDefinition(Famille.class);
	}

	/**
	 * Description.
	 */
	@Test
	public void testDescription() {
		testDescription(collectionsManager);
	}

	/**
	 * @see DtListProcessor#sort
	 */
	@Test
	public void testCreateSortState() {
		final DtListProcessor sortStateAsc = collectionsManager.createDtListProcessor()
				.sort("LIBELLE", false);
		Assert.assertNotNull(sortStateAsc);
	}

	/**
	 * @see DtListProcessor#sort
	 */
	@Test
	public void testHeavySort() {
		// final DtList<Famille> sortDtc;
		final DtList<Famille> dtc = createFamilles();
		//
		for (int i = 0; i < 50000; i++) {
			final Famille mocka = new Famille();
			mocka.setLibelle(String.valueOf(i % 100));
			dtc.add(mocka);
		}
		final DtListProcessor sortState = collectionsManager.createDtListProcessor()
				.sort("LIBELLE", false);
		final DtList<Famille> sortedDtc = sortState.apply(dtc);
		nop(sortedDtc);

	}

	/**
	 * @see DtListProcessor#sort
	 */
	@Test
	public void testSort() {
		DtList<Famille> sortDtc;
		final DtList<Famille> dtc = createFamilles();
		final String[] indexDtc = indexId(dtc);

		// Cas de base.
		// ======================== Ascendant
		// =================================== nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager
				.createDtListProcessor()
				.sort("LIBELLE", false)
				.apply(dtc);

		assertEquals(indexDtc, indexId(dtc));
		assertEquals(new String[] { aaa, Ba, bb, null }, indexId(sortDtc));

		// ======================== Descendant
		// =================================== not nullLast
		// ================================================ ignoreCase
		sortDtc = collectionsManager.createDtListProcessor()
				.sort("LIBELLE", true)
				.apply(dtc);
		assertEquals(indexDtc, indexId(dtc));
		assertEquals(new String[] { null, bb, Ba, aaa }, indexId(sortDtc));
	}

	/**
	 * @see DtListProcessor#filterByValue
	 */
	@Test
	public void testCreateValueFilter() {
		final DtListProcessor filter = collectionsManager.createDtListProcessor()
				.filterByValue("LIBELLE", "a");
		Assert.assertNotNull(filter);
	}

	/**
	 * @see DtListProcessor#filterByValue
	 */
	@Test
	public void testCreateTwoValuesFilter() {
		final DtListProcessor filter = collectionsManager.createDtListProcessor()
				.filterByValue("LIBELLE", "a")
				.filterByValue("FAM_ID", 1L);
		Assert.assertNotNull(filter);
	}

	/**
	 * @see DtListProcessor#filterByValue
	 */
	@Test
	public void testFilter() {
		final DtList<Famille> result = collectionsManager.createDtListProcessor()
				.filterByValue("LIBELLE", "aaa")
				.apply(createFamilles());
		Assert.assertEquals(1, result.size());
	}

	/**
	 * @see DtListProcessor#filterByValue
	 */
	@Test
	public void testFilterTwoValues() {
		final DtList<Famille> result = collectionsManager.createDtListProcessor()
				.filterByValue("LIBELLE", "aaa")
				.filterByValue("FAM_ID", 13L)
				.apply(createFamillesForRangeTest());
		Assert.assertEquals(1, result.size());
	}

	/**
	 * @see DtListProcessor#filter
	 */
	@Test
	public void testFilterFullText() {
		final DtList<Famille> result = collectionsManager.<Famille> createIndexDtListFunctionBuilder()
				.filter("aa", 1000, dtDefinitionFamille.getFields())
				.build()
				.apply(createFamilles());
		Assert.assertEquals(1, result.size(), 0);

	}

	/**
	 * @see DtListProcessor#filter
	 */
	@Test
	public void testFilterFullTextTokenizer() {
		final DtList<Famille> dtc = createFamilles();
		final Collection<DtField> searchedDtFields = dtDefinitionFamille.getFields();
		final Famille mock1 = new Famille();
		mock1.setFamId(seqFamId++);
		mock1.setLibelle("Agence de l'Ouest");
		dtc.add(mock1);

		final Famille mock2 = new Famille();
		mock2.setFamId(seqFamId++);
		mock2.setLibelle("Hôpital et autres accents çava où ãpied");
		dtc.add(mock2);

		Assert.assertTrue("La recherche n'est pas case insensitive", filter(dtc, "agence", 1000, searchedDtFields).size() == 1);//majuscule/minuscule
		Assert.assertTrue("La recherche n'est pas plain text", filter(dtc, "l'ouest", 1000, searchedDtFields).size() == 1);//tokenizer
		Assert.assertTrue("La recherche ne supporte pas les accents", filter(dtc, "hopital", 1000, searchedDtFields).size() == 1);//accents
		Assert.assertTrue("La recherche ne supporte pas les caractères spéciaux fr (ç)", filter(dtc, "cava", 1000, searchedDtFields).size() == 1); //accents fr (ç)
		Assert.assertTrue("La recherche ne supporte pas les caractères spéciaux latin1 (ã)", filter(dtc, "apied", 1000, searchedDtFields).size() == 1); //accents autre (ã)
		Assert.assertTrue("La recherche ne supporte pas la recherche par préfix", filter(dtc, "apie", 1000, searchedDtFields).size() == 1);//prefix
	}

	private List<Famille> filter(final DtList<Famille> dtc, final String query, final int nbRows, final Collection<DtField> searchedDtFields) {
		return collectionsManager.<Famille> createIndexDtListFunctionBuilder()
				.filter(query, nbRows, searchedDtFields)
				.build()
				.apply(dtc);
	}

	/**
	 * @see DtListProcessor#filter
	 */
	@Test
	public void testFilterFullTextElision() {
		final DtList<Famille> dtc = createFamilles();
		final Collection<DtField> searchedDtFields = dtDefinitionFamille.getFields();

		final Famille mock1 = new Famille();
		mock1.setFamId(seqFamId++);
		mock1.setLibelle("Agence de l'Ouest");
		dtc.add(mock1);

		final Famille mock2 = new Famille();
		mock2.setFamId(seqFamId++);
		mock2.setLibelle("Hôpital et autres accents çava où àpied");
		dtc.add(mock2);

		Assert.assertTrue("La recherche ne supporte pas l'elision", filter(dtc, "ouest", 1000, searchedDtFields).size() == 1);
	}

	/**
	 * @see DtListProcessor#filter
	 */
	@Test
	public void testFilterFullTextMultiKeyword() {
		final DtList<Famille> dtc = createFamilles();
		final Collection<DtField> searchedDtFields = dtDefinitionFamille.getFields();

		final Famille mock1 = new Famille();
		mock1.setFamId(seqFamId++);
		mock1.setLibelle("Agence de l'Ouest");
		dtc.add(mock1);

		final Famille mock2 = new Famille();
		mock2.setFamId(seqFamId++);
		mock2.setLibelle("Hôpital et autres accents çava où ãpied");
		dtc.add(mock2);

		Assert.assertTrue("La recherche ne supporte pas l'espace", filter(dtc, "agence de", 1000, searchedDtFields).size() == 1);//mots proches
		Assert.assertTrue("La recherche ne supporte pas l'utilisation de plusieurs mots", filter(dtc, "hopital accent", 1000, searchedDtFields).size() == 1);//mots séparés
		Assert.assertTrue("La recherche ne supporte pas l'inversion des mots", filter(dtc, "accent hopital", 1000, searchedDtFields).size() == 1);//inversés
		Assert.assertTrue("Les mots clés ne sont pas en 'ET'", filter(dtc, "agence hopital", 1000, searchedDtFields).size() == 0);//multi doc
	}

	/**
	 * Vérifie le comportement quand la recherche en commence par addresse trop de term du dictionnaire.
	 * Par défaut Lucene envoi une erreur TooMany...., le collectionsManager limite aux premiers terms.
	 * @see DtListProcessor#filter
	 */
	@Test
	public void testFilterFullTextBigList() {
		final DtListFunction<Famille> filterFunction = collectionsManager.<Famille> createIndexDtListFunctionBuilder()
				.filter("a", 2000, dtDefinitionFamille.getFields())
				.build();
		Assert.assertNotNull(filterFunction);
		final DtList<Famille> bigFamillyList = new DtList<>(Famille.class);
		for (int i = 0; i < 50000; i++) {
			final Famille mocka = new Famille();
			mocka.setFamId(seqFamId++);
			mocka.setLibelle("blabla a" + (char) ('a' + i % 26) + String.valueOf(i % 100));
			bigFamillyList.add(mocka);
		}
		final DtList<Famille> result = filterFunction.apply(bigFamillyList);
		Assert.assertEquals(2000, result.size(), 0);
	}

	/**
	 * @see DtListProcessor#sort
	 */
	@Test
	public void testSortWithIndex() {
		DtList<Famille> sortDtc;
		final DtList<Famille> dtc = createFamilles();
		final String[] indexDtc = indexId(dtc);

		// Cas de base.
		// ======================== Ascendant
		sortDtc = collectionsManager.<Famille> createIndexDtListFunctionBuilder()
				.sort("LIBELLE", false)
				.build()
				.apply(dtc);

		assertEquals(indexDtc, indexId(dtc));
		assertEquals(new String[] { aaa, Ba, bb, null }, indexId(sortDtc));

		// ======================== Descendant
		sortDtc = collectionsManager.<Famille> createIndexDtListFunctionBuilder()
				.sort("LIBELLE", true)
				.build()
				.apply(dtc);
		assertEquals(indexDtc, indexId(dtc));
		assertEquals(new String[] { null, bb, Ba, aaa }, indexId(sortDtc));
	}

	/**
	 * @see DtListProcessor#filterSubList
	 */
	@Test
	public void testSubListWithIndex() {
		// on test une implémentation de référence ArrayList
		final List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		Assert.assertEquals(0, list.subList(0, 0).size());
		Assert.assertEquals(2, list.subList(0, 2).size()); // >0, 1
		Assert.assertEquals(1, list.subList(1, 2).size()); // >1
		Assert.assertEquals(0, list.subList(2, 2).size());
		// on teste notre implémentation
		//can't test subList(0,0) : illegal argument
		Assert.assertEquals(2, subListWithIndex(createFamilles(), 0, 2).size());
		Assert.assertEquals(1, subListWithIndex(createFamilles(), 1, 2).size());
		//can't test subList(2,2) : illegal argument
	}

	private DtList<Famille> subListWithIndex(final DtList<Famille> dtc, final int start, final int end) {
		return collectionsManager.<Famille> createIndexDtListFunctionBuilder()
				.filterSubList(start, end)
				.build()
				.apply(dtc);
	}

	/**
	 * @see DtListProcessor#filterSubList
	 */
	@Test
	public void testSubList() {
		// on test une implémentation de référence ArrayList
		final List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		Assert.assertEquals(0, list.subList(0, 0).size());
		Assert.assertEquals(2, list.subList(0, 2).size()); // >0, 1
		Assert.assertEquals(1, list.subList(1, 2).size()); // >1
		Assert.assertEquals(0, list.subList(2, 2).size());
		// on teste notre implémentation
		Assert.assertEquals(0, subList(createFamilles(), 0, 0).size());
		Assert.assertEquals(2, subList(createFamilles(), 0, 2).size());
		Assert.assertEquals(1, subList(createFamilles(), 1, 2).size());
		Assert.assertEquals(0, subList(createFamilles(), 2, 2).size());
	}

	private DtList<Famille> subList(final DtList<Famille> dtc, final int start, final int end) {
		return collectionsManager.createDtListProcessor()
				.filterSubList(start, end)
				.apply(dtc);
	}

	/**
	 * @see DtListProcessor#filterSubList
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testSubListFail1() {
		// On teste les dépassements.
		subList(createFamilles(), 5, 5);
		// "[Assertion.precondition] IndexOutOfBoundException....
	}

	/**
	 * @see DtListProcessor#filterSubList
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testSubListFail2() {
		// On teste les dépassements.
		subList(createFamilles(), 1, 20);
		// "[Assertion.precondition] IndexOutOfBoundException....
	}

	/**
	 * combiner sort/filter ; filter/sort ; sublist/sort ; filter/sublist.
	 *
	 */
	@Test
	public void testChainFilterSortSubList() {

		final DtList<Famille> dtc = createFamilles();
		final String[] indexDtc = indexId(dtc);

		final DtListProcessor filter = collectionsManager.createDtListProcessor()
				.filterByValue("LIBELLE", "aaa");
		final DtListProcessor sortState = collectionsManager.createDtListProcessor()
				.sort("LIBELLE", false);

		final int sizeDtc = dtc.size();

		DtList<Famille> sortDtc, filterDtc, subList;
		// ======================== sort/filter
		sortDtc = sortState.apply(dtc);
		assertEquals(new String[] { aaa, Ba, bb, null }, indexId(sortDtc));
		filterDtc = filter.apply(sortDtc);
		assertEquals(new String[] { aaa }, indexId(filterDtc));

		// ======================== sort/sublist
		sortDtc = sortState.apply(dtc);
		assertEquals(new String[] { aaa, Ba, bb, null }, indexId(sortDtc));
		subList = subList(sortDtc, 0, sizeDtc - 1);
		assertEquals(new String[] { aaa }, indexId(filterDtc));

		// ======================== filter/sort
		filterDtc = filter.apply(dtc);
		assertEquals(new String[] { aaa }, indexId(filterDtc));
		sortDtc = sortState.apply(filterDtc);
		assertEquals(new String[] { aaa }, indexId(filterDtc));

		// ======================== filter/sublist
		filterDtc = filter.apply(dtc);
		assertEquals(new String[] { aaa }, indexId(filterDtc));
		subList = subList(filterDtc, 0, filterDtc.size() - 1);
		assertEquals(new String[] { aaa }, indexId(filterDtc));

		// ======================== sublist/sort
		subList = subList(dtc, 0, sizeDtc - 1);
		assertEquals(new String[] { Ba, null, aaa }, indexId(subList));
		sortDtc = sortState.apply(subList);
		assertEquals(new String[] { aaa }, indexId(filterDtc));

		// ======================== sublist/filter
		subList = subList(dtc, 0, sizeDtc - 1);
		assertEquals(new String[] { Ba, null, aaa }, indexId(subList));
		filterDtc = filter.apply(subList);
		assertEquals(new String[] { aaa }, indexId(filterDtc));

		// === dtc non modifié
		assertEquals(indexDtc, indexId(dtc));

	}

	/**
	 * @see DtListProcessor#filter
	 */
	@Test
	public void testCreateFilterForValue() {
		final DtListProcessor filter = collectionsManager.createDtListProcessor()
				.filter(new ListFilter("LIBELLE" + ":\"aaa\""));
		Assert.assertNotNull(filter);
	}

	/**
	 * @see DtListProcessor#filter
	 */
	@Test
	public void testTermFilterString() {
		testTermFilter("LIBELLE:\"aaa\"", 3);
		testTermFilter("LIBELLE:\"aaab\"", 1);
	}

	/**
	 * @see DtListProcessor#filter
	 */
	@Test
	public void testTermFilterLong() {
		testTermFilter("FAM_ID:\"1\"", 1);
		testTermFilter("FAM_ID:\"11\"", 1);
		testTermFilter("FAM_ID:\"2\"", 0);
	}

	/**
	 * @see DtListProcessor#filterByRange
	 */
	@Test
	public void testCreateFilterByRange() {
		final DtListProcessor filter = collectionsManager.createDtListProcessor()
				.filterByRange("LIBELLE", Option.ofNullable("a"), Option.ofNullable("b"));
		Assert.assertNotNull(filter);
	}

	/**
	 * @see DtListProcessor#filter
	 */
	@Test
	public void testCreateFilter() {
		final DtListProcessor filter = collectionsManager.createDtListProcessor()
				.filter(new ListFilter("LIBELLE" + ":[a TO b]"));
		Assert.assertNotNull(filter);
	}

	/**
	 * @see DtListProcessor#add
	 */
	@Test
	public void testAddDtListFunction() {
		final DtList<Famille> familles = collectionsManager.createDtListProcessor()
				.add(new DtListFunction<Famille>() {

					/** {@inheritDoc} */
					@Override
					public DtList<Famille> apply(final DtList<Famille> input) {
						final DtList<Famille> result = new DtList<>(Famille.class);
						for (final Famille family : input) {
							if (family.getFamId() != null && family.getFamId() == 3L) {
								result.add(family);
							}
						}
						return result;
					}
				}).apply(createFamillesForRangeTest());
		Assert.assertEquals(1L, familles.size());
	}

	/**
	 * @see DtListProcessor#filter
	 */
	@Test
	public void testRangeFilter() {
		testRangeFilter("LIBELLE" + ":[a TO b]", 5);
	}

	/**
	 * @see DtListProcessor#filter
	 */
	@Test
	public void testRangeFilterLong() {
		testRangeFilter("FAM_ID:[1 TO 10]", 3);
		testRangeFilter("FAM_ID:[1 TO 10[", 2);
		testRangeFilter("FAM_ID:]1 TO 10]", 2);
		testRangeFilter("FAM_ID:]1 TO 10[", 1);
		testRangeFilter("FAM_ID:]1 TO *[", 9);
		testRangeFilter("FAM_ID:[* TO *[", 10);
	}

	/**
	 * @see DtListProcessor#filter
	 */
	@Test
	public void testRangeFilterString() {
		testRangeFilter("LIBELLE:[a TO b]", 5);
		testRangeFilter("LIBELLE:[* TO c[", 7);
		testRangeFilter("LIBELLE:[* TO c]", 8);
		testRangeFilter("LIBELLE:[* TO cb]", 9);
		testRangeFilter("LIBELLE:[aaab TO aaac]", 2);
		testRangeFilter("LIBELLE:[aaab TO aaac[", 1);
	}

	private void testTermFilter(final String filterString, final int countEspected) {
		final DtList<Famille> result = collectionsManager.createDtListProcessor()
				.filter(new ListFilter(filterString))
				.apply(createFamillesForRangeTest());
		Assert.assertEquals(countEspected, result.size());
	}

	private void testRangeFilter(final String filterString, final int countEspected) {
		final DtListProcessor filter = collectionsManager.createDtListProcessor()
				.filter(new ListFilter(filterString));
		Assert.assertNotNull(filter);
		final DtList<Famille> result = filter.apply(createFamillesForRangeTest());
		Assert.assertEquals(countEspected, result.size());
	}

	private static DtList<Famille> createFamillesForRangeTest() {
		final DtList<Famille> dtc = createFamilles();

		final Famille mock1 = new Famille();
		mock1.setFamId(1L);
		mock1.setLibelle("aaab");
		dtc.add(mock1);

		final Famille mock2 = new Famille();
		mock2.setFamId(10L);
		mock2.setLibelle("aaac");
		dtc.add(mock2);

		final Famille mock3 = new Famille();
		mock3.setFamId(11L);
		mock3.setLibelle("caaa");
		dtc.add(mock3);

		final Famille mock4 = new Famille();
		mock4.setFamId(12L);
		mock4.setLibelle("aaa");
		dtc.add(mock4);

		final Famille mock5 = new Famille();
		mock5.setFamId(13L);
		mock5.setLibelle("aaa");
		dtc.add(mock5);

		final Famille mock6 = new Famille();
		mock6.setFamId(3L);
		mock6.setLibelle("c");
		dtc.add(mock6);

		return dtc;
	}

	/**
	 * Asserts that two booleans are equal.
	 *
	 */
	private static void assertEquals(final String[] expected, final String[] actual) {
		Assert.assertEquals(Arrays.toString(expected), Arrays.toString(actual));
	}

	private static String[] indexId(final DtList<Famille> dtc) {
		final String[] index = new String[dtc.size()];
		for (int i = 0; i < dtc.size(); i++) {
			index[i] = dtc.get(i).getLibelle();
		}
		return index;
	}

	private static long seqFamId = 100;

	private static DtList<Famille> createFamilles() {
		final DtList<Famille> dtc = new DtList<>(Famille.class);
		// les index sont données par ordre alpha > null à la fin >
		final Famille mockB = new Famille();
		mockB.setFamId(seqFamId++);
		mockB.setLibelle(Ba);
		dtc.add(mockB);

		final Famille mockNull = new Famille();
		mockNull.setFamId(seqFamId++);
		// On ne renseigne pas le libelle > null
		dtc.add(mockNull);

		final Famille mocka = new Famille();
		mocka.setFamId(seqFamId++);
		mocka.setLibelle(aaa);
		dtc.add(mocka);

		final Famille mockb = new Famille();
		mockb.setFamId(seqFamId++);
		mockb.setLibelle(bb);
		dtc.add(mockb);

		// On crée et on supprimme un élément dans la liste pour vérifier
		// l'intégrité de la liste (Par rapport aux null).
		final Famille mockRemoved = new Famille();
		mockRemoved.setFamId(seqFamId++);
		mockRemoved.setLibelle("mockRemoved");
		dtc.add(mockRemoved);

		dtc.remove(mockRemoved);
		return dtc;
	}
}
