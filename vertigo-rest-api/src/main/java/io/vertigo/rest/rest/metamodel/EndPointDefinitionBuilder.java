package io.vertigo.rest.rest.metamodel;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;
import io.vertigo.kernel.util.StringUtil;
import io.vertigo.rest.rest.metamodel.EndPointDefinition.Verb;
import io.vertigo.rest.rest.metamodel.EndPointParam.ImplicitParam;
import io.vertigo.rest.rest.metamodel.EndPointParam.RestParamType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 
 * EndPointDefinition Builder.
 *  
 * @author npiedeloup
 */
public final class EndPointDefinitionBuilder implements Builder<EndPointDefinition> {
	private final Method method;
	private Verb verb;
	private String path;
	private final String acceptType = "application/json"; //default
	private boolean needSession = true;
	private boolean sessionInvalidate;
	private boolean needAuthentication = true;
	private final Set<String> includedFields = new LinkedHashSet<>();
	private final Set<String> excludedFields = new LinkedHashSet<>();
	private boolean accessTokenPublish;
	private boolean accessTokenMandatory;
	private boolean accessTokenConsume;
	private boolean serverSideSave;
	private boolean autoSortAndPagination;
	private String doc = "";
	private final List<EndPointParam> endPointParams = new ArrayList<>();

	/**
	 * Constructeur.
	 */
	public EndPointDefinitionBuilder(final Method method) {
		Assertion.checkNotNull(method);
		//---------------------------------------------------------------------
		this.method = method;
	}

	public EndPointDefinition build() {
		return new EndPointDefinition(//
				//"EP_" + StringUtil.camelToConstCase(restFullServiceClass.getSimpleName()) + "_" + StringUtil.camelToConstCase(method.getName()), //
				"EP_" + verb + "_" + StringUtil.camelToConstCase(path.replaceAll("[//{}]", "_")), //
				verb, //
				path, //
				acceptType, //
				method, //
				needSession, //
				sessionInvalidate, //
				needAuthentication, //
				accessTokenPublish,//
				accessTokenMandatory,//
				accessTokenConsume,//
				serverSideSave,//
				autoSortAndPagination,//
				includedFields, //
				excludedFields, //
				endPointParams, //
				doc);
	}

	public void with(final Verb newVerb, final String newPath) {
		Assertion.checkState(verb == null, "A verb is already specified on {0} ({1})", method.getName(), verb);
		Assertion.checkArgNotEmpty(newPath, "Route path must be specified on {0}", method.getName());
		verb = newVerb;
		path = newPath;
	}

	public boolean hasVerb() {
		return verb != null;
	}

	public void withAccessTokenConsume(final boolean accessTokenConsume) {
		this.accessTokenConsume = accessTokenConsume;
	}

	public void withNeedAuthentication(final boolean needAuthentication) {
		this.needAuthentication = needAuthentication;
	}

	public void withNeedSession(final boolean needSession) {
		this.needSession = needSession;
	}

	public void withSessionInvalidate(final boolean sessionInvalidate) {
		this.sessionInvalidate = sessionInvalidate;
	}

	public void withExcludedFields(final String[] excludedFields) {
		this.excludedFields.addAll(Arrays.asList(excludedFields));
	}

	public void withIncludedFields(final String[] includedFields) {
		this.includedFields.addAll(Arrays.asList(includedFields));
	}

	public void withAccessTokenPublish(final boolean accessTokenPublish) {
		this.accessTokenPublish = accessTokenPublish;
	}

	public void withAccessTokenMandatory(final boolean accessTokenMandatory) {
		this.accessTokenMandatory = accessTokenMandatory;
	}

	public void withServerSideSave(final boolean serverSideSave) {
		this.serverSideSave = serverSideSave;
	}

	public void withAutoSortAndPagination(final boolean autoSortAndPagination) {
		this.autoSortAndPagination = autoSortAndPagination;

		//autoSortAndPagination must keep the list serverSide but not the input one, its the full one, so we don't use serverSideSave marker
		//autoSortAndPagination use a Implicit UiListState, this one must be show in API, so we add it to endPointParams
		//autoSortAndPaginationHandler will use it
		if (autoSortAndPagination) {
			withEndPointParam(new EndPointParamBuilder(UiListState.class) //
					.with(RestParamType.Implicit, ImplicitParam.UiListState.name()).build());
		}
	}

	public void withDoc(final String doc) {
		this.doc = doc;
	}

	public void withEndPointParam(final EndPointParam endPointParam) {
		endPointParams.add(endPointParam);
	}
}