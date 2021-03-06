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
package io.vertigo.lang;

import java.lang.reflect.InvocationTargetException;

import io.vertigo.util.StringUtil;

/**
 * Encapsulates some Exception inside a RuntimeException.
 * Inspired by gnu.mapping.WrappedException.
 *
 * @author npiedeloup
 */
public final class WrappedException extends RuntimeException {

	private static final long serialVersionUID = 8595187765435824071L;

	/**
	 * Constructor.
	 * @param cause Cause exception
	 */
	public WrappedException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 * @param message Context message
	 * @param cause Cause exception
	 */
	public WrappedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Coerce argument to a RuntimeException.
	 * Re-throw as a non-checked exception. This method never returns, in spite of the return type.
	 * This allows the call to be written as: throw WrappedExcepton.rethrow(th) so javac and the verifier can know the code doesn't return.
	 * @param th Cause exception
	 * @param msg Context message
	 * @param params Context message params
	 * @return RuntimeException runtime
	 */
	public static RuntimeException wrapIfNeeded(final java.lang.Throwable th, final String msg, final Object... params) {
		final Throwable t;
		if (th instanceof InvocationTargetException) {
			t = ((InvocationTargetException) th).getTargetException();
		} else {
			t = th;
		}

		if (t instanceof RuntimeException) {
			throw (RuntimeException) t;
		}
		if (t instanceof Error) {
			throw (Error) t;
		}
		final String message = msg != null ? StringUtil.format(msg, params) : null;
		throw new WrappedException(message, t);
	}

}
