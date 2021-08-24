/*
 * Copyright 2017-2020 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.gaffer.operation.impl.compare;

import uk.gov.gchq.gaffer.commonutil.pair.Pair;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.data.element.comparison.ElementComparator;
import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.gaffer.operation.serialisation.TypeReferenceImpl;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * An {@code ElementComparison} operation is an operation which is used
 * to make comparisons between elements. It is required to have an array of
 * {@link Comparator}s of {@link Element}s
 */
public final class ElementComparisonUtil {

    public static final String KEY_COMPARATORS = "comparators";
    public static final AbstractMap.SimpleImmutableEntry<String, Class> entryComparators = new AbstractMap.SimpleImmutableEntry<>(KEY_COMPARATORS, List.class);

    private ElementComparisonUtil() {
    }

    public static List<Comparator<Element>> getComparators(final Operation operation) {
        return (List<Comparator<Element>>) operation.get(KEY_COMPARATORS);
    }

    /**
     * Combine all currently registered comparators into a single {@link Comparator}
     * object.
     *
     * @return the combined comparator
     */
    public static Comparator<Element> getCombinedComparator(Operation operation) {
        final List<Comparator<Element>> comparators = getComparators(operation);
        Comparator<Element> combinedComparator = null;
        if (null != comparators && !comparators.isEmpty()) {
            for (final Comparator<Element> comparator : comparators) {
                if (null == combinedComparator) {
                    combinedComparator = comparator;
                } else if (null != comparator) {
                    combinedComparator = combinedComparator.thenComparing(comparator);
                }
            }
        }

        return combinedComparator;
    }

    /**
     * Get all of the Group-Property pairs which implement the {@link Comparable}
     * interface.
     *
     * @return a {@link Set} containing the pairs
     */
    public static Set<Pair<String, String>> getComparableGroupPropertyPairs(Operation operation) {
        final List<Comparator<Element>> comparators = getComparators(operation);
        if (null != comparators && !comparators.isEmpty()) {
            final Set<Pair<String, String>> pairs = new HashSet<>();
            for (final Comparator<Element> comparator : comparators) {
                if (null != comparator && comparator instanceof ElementComparator) {
                    pairs.addAll(((ElementComparator) comparator).getComparableGroupPropertyPairs());
                }
            }
            return pairs;
        }

        return Collections.emptySet();
    }

}
