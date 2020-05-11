/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.type;

import java.util.Map;
import java.util.Set;

/**
 * Interface that defines abstract access to the annotations of a specific
 * class, in a form that does not require that class to be loaded yet.
 *
 * @author Juergen Hoeller
 * @since 2.1
 * @see StandardAnnotationMetadata
 * @see org.springframework.core.type.asm.AnnotationMetadataReadingVisitor
 */
public interface AnnotationMetadata extends ClassMetadata {

	/**
	 * Return the names of all annotation types defined on the underlying class.
	 * @return the annotation type names
	 */
	Set<String> getAnnotationTypes();

	/**
	 * Determine whether the underlying class has an annotation of the given
	 * type defined.
	 * @param annotationType the annotation type to look for
	 * @return whether a matching annotation is defined
	 */
	boolean hasAnnotation(String annotationType);

	/**
	 * Retrieve the attributes of the annotation of the given type,
	 * if any (i.e. if defined on the underlying class).
	 * @param annotationType the annotation type to look for
	 * @return a Map of attributes, with the attribute name as key
	 * (e.g. "value") and the defined attribute value as Map value.
	 * This return value will be <code>null</code> if no matching
	 * annotation is defined.
	 */
	Map<String, Object> getAnnotationAttributes(String annotationType);

}
