package io.vertigo.rest.validation;

import io.vertigo.dynamo.domain.metamodel.ConstraintException;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.MessageText;
import io.vertigo.kernel.util.StringUtil;

import java.util.Date;
import java.util.Set;

/**
 * Objet de validation d'un DtObject.
 * @author npiedeloup
 * @version $Id: UiObjectValidator.java,v 1.6 2014/02/26 17:43:46 npiedeloup Exp $
 * @param <O> Type d'objet
 */
public class UiObjectValidator<O extends DtObject> {

	/**
	 * Effectue les validations prévu d'un objet.
	 * @param input Objet à tester
	 * @param modifiedFieldNameSet Liste des champs modifiés
	 * @param uiObjectErrors Pile des erreurs
	 */
	public void validate(final UiObject<O> input, final Set<String> modifiedFieldNameSet, final UiObjectErrors uiObjectErrors) {
		for (final String fieldName : modifiedFieldNameSet) {
			final DtField dtField = input.getDtDefinition().getField(fieldName);
			defaultCheckMonoFieldConstraints(input, dtField, uiObjectErrors);
			checkMonoFieldConstraints(input, dtField, uiObjectErrors);
		}
		checkMultiFieldConstraints(input, modifiedFieldNameSet, uiObjectErrors);
	}

	/**
	 * Effectue des controles multichamps spécifiques.
	 * @param input Objet à tester
	 * @param modifiedFieldNameSet Liste des champs modifiés
	 * @param uiObjectErrors Pile des erreurs
	 */
	protected void checkMultiFieldConstraints(final UiObject<O> input, final Set<String> modifiedFieldNameSet, final UiObjectErrors uiObjectErrors) {
		//enrichissable pour un type d'objet particulier
		//ex: input.addError(e.getMessageText());
	}

	/**
	 * Effectue des controles monochamps spécifiques.
	 * @param input Objet à tester
	 * @param dtField Champs à tester
	 * @param uiObjectErrors Pile des erreurs
	 */
	protected void checkMonoFieldConstraints(final UiObject<O> input, final DtField dtField, final UiObjectErrors uiObjectErrors) {
		//enrichissable pour un type d'objet particulier
		//ex: input.addError(e.getMessageText());
	}

	private final void defaultCheckMonoFieldConstraints(final UiObject<O> input, final DtField dtField, final UiObjectErrors uiObjectErrors) {
		if (!input.hasFormatError(dtField)) {
			final Object value = input.getTypedValue(dtField);
			//pas d'assertion notNull, car le champs n'est pas forcément obligatoire
			if (value == null && dtField.isNotNull()) {
				uiObjectErrors.addError(dtField, new MessageText("le champ doit être renseigné", null));
			}
			try {
				// Le typage est OK
				// On vérifie la validité de la valeur par rapport au champ/domaine.
				dtField.getDomain().checkValue(value);
			} catch (final ConstraintException e) {
				// Erreur lors du check de la valeur,
				// la valeur est toutefois correctement typée.
				uiObjectErrors.addError(dtField, e.getMessageText());
			}
		}
	}

	/**
	 * @param input Object a tester
	 * @param modifiedFieldNameSet Liste des champs modifiés
	 * @param fieldNames Nom des champs à tester
	 * @return si le champ a été modifié et ne contient pas d'erreur de format
	 */
	protected final boolean shouldCheck(final UiObject<O> input, final Set<String> modifiedFieldNameSet, final String... fieldNames) {
		for (final String fieldName : fieldNames) {
			final String constFieldName = StringUtil.camelToConstCase(fieldName);
			if (!modifiedFieldNameSet.contains(constFieldName)) {
				return false;
			}
			if (input.hasFormatError(getDtField(fieldName, input))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Vérifie l'égalité des champs.
	 * @param input Object a tester
	 * @param fieldName1 Champs 1 
	 * @param fieldName2 Champs 2
	 * @param uiObjectErrors Pile des erreurs
	 * @param messageText Message à appliquer si erreur
	 */
	protected final void checkFieldEquals(final UiObject<O> input, final String fieldName1, final String fieldName2, final UiObjectErrors uiObjectErrors, final MessageText messageText) {
		final Object value1 = input.get(fieldName1); //la valeur formattée n'est jamais null : affichage "" si null
		final Object value2 = input.get(fieldName2);
		if (!value1.equals(value2)) {
			uiObjectErrors.addError(getDtField(fieldName2, input), messageText);
		}
	}

	/**
	 * Vérifie que la date du champ 2 est après (strictement) la date du champ 1. 
	 * @param input Object a tester
	 * @param fieldName1 Champs 1 
	 * @param fieldName2 Champs 2 
	 * @param uiObjectErrors Pile des erreurs
	 * @param messageText Message à appliquer si erreur
	 */
	protected final void checkFieldDateAfter(final UiObject<O> input, final String fieldName1, final String fieldName2, final UiObjectErrors uiObjectErrors, final MessageText messageText) {
		final Date value1 = input.getDate(fieldName1); //la valeur typée peut être null
		final Date value2 = input.getDate(fieldName2);
		if (value1 != null && value2 != null && !value2.after(value1)) {
			uiObjectErrors.addError(getDtField(fieldName2, input), messageText);
		}
	}

	/**
	 * Vérifie que le Long du champ 2 est après (strictement) le Long du champ 1. 
	 * @param input Object a tester
	 * @param fieldName1 Champs 1 
	 * @param fieldName2 Champs 2 
	 * @param uiObjectErrors Pile des erreurs
	 * @param messageText Message à appliquer si erreur
	 */
	protected final void checkFieldLongAfter(final UiObject<O> input, final String fieldName1, final String fieldName2, final UiObjectErrors uiObjectErrors, final MessageText messageText) {
		final Long value1 = input.getLong(fieldName1); //la valeur typée peut être null
		final Long value2 = input.getLong(fieldName2);
		if (value1 != null && value2 != null && !(value2.compareTo(value1) > 0)) {
			uiObjectErrors.addError(getDtField(fieldName2, input), messageText);
		}
	}

	/**
	 * Vérifie que le champ est renseigner. 
	 * @param input Object a tester
	 * @param fieldName Champs 
	 * @param uiObjectErrors Pile des erreurs
	 * @param messageText Message à appliquer si erreur
	 */
	protected final void checkFieldNotNull(final UiObject<O> input, final String fieldName, final UiObjectErrors uiObjectErrors, final MessageText messageText) {
		final String value = (String) input.get(fieldName); //la valeur formattée n'est jamais null
		if (value.isEmpty()) {
			uiObjectErrors.addError(getDtField(fieldName, input), messageText);
		}
	}

	/**
	 * Vérifie qu'au moins l'un des champs est renseigné.
	 * @param input Object a tester
	 * @param uiObjectErrors Pile des erreurs
	 * @param messageText Message à appliquer si erreur
	 * @param fieldNames Champs...
	 */
	protected final void checkOneOrMoreFieldNotNull(final UiObject<O> input, final UiObjectErrors uiObjectErrors, final MessageText messageText, final String... fieldNames) {
		boolean oneNotEmpty = false;
		for (final String fieldName : fieldNames) {
			final String value = (String) input.get(fieldName); //la valeur formattée n'est jamais null
			if (!value.isEmpty()) { //Si on en a un renseigné, la règle est respectée et on quitte.
				oneNotEmpty = true;
				break;
			}
		}
		if (!oneNotEmpty) {
			uiObjectErrors.addError(messageText);
		}
	}

	/**
	 * Vérifie qu'au plus un des champs est renseigné.
	 * @param input Object a tester
	 * @param uiObjectErrors Pile des erreurs
	 * @param messageText Message à appliquer si erreur
	 * @param fieldNames Champs...
	 */
	protected final void checkOneAndOnlyOneFieldNotNull(final UiObject<O> input, final UiObjectErrors uiObjectErrors, final MessageText messageText, final String... fieldNames) {
		boolean oneNotEmpty = false;
		for (final String fieldName : fieldNames) {
			final String value = (String) input.get(fieldName); //la valeur formattée n'est jamais null
			if (!value.isEmpty()) {
				if (oneNotEmpty) { //Si on en a déjà un renseigné, la règle n'est pas respectée et on quitte.
					oneNotEmpty = false;
					break;
				}
				oneNotEmpty = true;
			}
		}
		if (!oneNotEmpty) {
			uiObjectErrors.addError(messageText);
		}
	}

	/**
	 * @param fieldName Nom du champ
	 * @param input Objet portant le champ
	 * @return DtField.
	 */
	protected final DtField getDtField(final String fieldName, final UiObject<O> input) {
		return input.getDtDefinition().getField(StringUtil.camelToConstCase(fieldName));
	}

}
