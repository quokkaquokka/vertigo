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
package io.vertigo.dynamo.impl.collections.functions.filter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.lang.VSystemException;
import io.vertigo.util.DateUtil;

/**
 * Parser des filtres utilisant une syntaxe définie.
 */
public final class DtListPatternFilterUtil {
	private static final String DATE_PATTERN = "dd/MM/yy";

	public enum FilterPattern {
		/** range. */
		Range("([A-Z_0-9]+):([\\[\\{\\]])(.*) TO (.*)([\\]\\}\\[])"), //[] : include, ][ or {} : exclude
		/** term. */
		Term("([A-Z_0-9]+):\"(.*)\"");

		private final Pattern pattern;

		FilterPattern(final String patternString) {
			pattern = Pattern.compile(patternString);
		}

		Pattern getPattern() {
			return pattern;
		}
	}

	/**
	 * Constructor.
	 */
	private DtListPatternFilterUtil() {
		//private constructor
	}

	static <D extends DtObject> DtListFilter<D> createDtListFilterForPattern(final FilterPattern filterPattern, final String[] parsedFilter, final DtDefinition dtDefinition) {
		Assertion.checkNotNull(filterPattern);
		Assertion.checkNotNull(parsedFilter);
		Assertion.checkNotNull(dtDefinition);
		//-----
		//Si on trouve un pattern, on passe sur du code spécifique
		final String fieldName = parsedFilter[1]; //attention parsedFilter[0] = filtre entier
		final DtField dtField = dtDefinition.getField(fieldName);
		final DataType dataType = dtField.getDomain().getDataType();

		switch (filterPattern) {
			case Range:
				return createDtListRangeFilter(parsedFilter, fieldName, dataType);
			case Term:
				return createDtListTermFilter(parsedFilter, fieldName, dataType);
			default:
				throw new VSystemException("La chaine de filtrage: {0} , ne respecte pas la syntaxe {1}.", parsedFilter[0], filterPattern.getPattern().pattern());
		}
	}

	/**
	 * Retourne les éléments parsés du filtre.
	 * index 0 : filtre d'origine.
	 * index 1 : nom du champs (par convention)
	 * ensuite dépend du pattern
	 **/
	public static Option<String[]> parseFilter(final String filterString, final Pattern parsingPattern) {
		Assertion.checkNotNull(filterString);
		Assertion.checkNotNull(parsingPattern);
		//-----
		final Matcher matcher = parsingPattern.matcher(filterString);
		if (!matcher.matches()) {
			return Option.empty();
		}

		final int nbGroup = matcher.groupCount() + 1;
		final String[] groups = new String[nbGroup];
		for (int i = 0; i < nbGroup; i++) {
			groups[i] = matcher.group(i);
		}
		return Option.of(groups);
	}

	private static <D extends DtObject> DtListFilter<D> createDtListTermFilter(final String[] parsedFilter, final String fieldName, final DataType dataType) {
		final Option<Comparable> filterValue = convertToComparable(parsedFilter[2], dataType, false);
		return new DtListValueFilter<>(fieldName, (Serializable) filterValue.orElse(null));
	}

	private static <D extends DtObject> DtListFilter<D> createDtListRangeFilter(final String[] parsedFilter, final String fieldName, final DataType dataType) {
		final boolean isMinInclude = "[".equals(parsedFilter[2]);
		final Option<Comparable> minValue = convertToComparable(parsedFilter[3], dataType, true);
		final Option<Comparable> maxValue = convertToComparable(parsedFilter[4], dataType, true);
		final boolean isMaxInclude = "]".equals(parsedFilter[5]);
		return new DtListRangeFilter<>(fieldName, minValue, maxValue, isMinInclude, isMaxInclude);
	}

	private static Option<Comparable> convertToComparable(final String valueToConvert, final DataType dataType, final boolean acceptJoker) {
		final String stringValue = valueToConvert.trim();
		if (acceptJoker && "*".equals(stringValue) || "".equals(stringValue)) {
			return Option.empty();//pas de test
		}
		//--
		final Comparable result = valueOf(dataType, stringValue);
		return Option.of(result);
	}

	private static Comparable valueOf(final DataType dataType, final String stringValue) {
		switch (dataType) {
			case Integer:
				return Integer.valueOf(stringValue);
			case Long:
				return Long.valueOf(stringValue);
			case BigDecimal:
				return new BigDecimal(stringValue);
			case Double:
				return Double.valueOf(stringValue);
			case Date:
				return DateUtil.parse(stringValue, DATE_PATTERN);
			case String:
				return stringValue;
			case Boolean:
			case DataStream:
			case DtObject:
			case DtList:
			default:
				throw new IllegalArgumentException("Type de données non comparable : " + dataType.name());
		}
	}
}
