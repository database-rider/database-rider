package com.github.database.rider.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.runner.Description;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetImpl;

/**
 * Adpated from JUnit5 AnnotationUtils:
 * https://github.com/junit-team/junit5/blob/master/junit-platform-commons/src/main/java/org/junit/platform/commons/util/AnnotationUtils.java
 */
public final class AnnotationUtils {

    ///CLOVER:OFF
    private AnnotationUtils() {
        /* no-op */
    }
    ///CLOVER:ON

    private static final Map<AnnotationCacheKey, Annotation> annotationCache = new ConcurrentHashMap<>(256);

    /**
     * @param element annotated method or class
     * @param annotationType annotationType
     *
     * Determine if an annotation of {@code annotationType} is either
     * <em>present</em> or <em>meta-present</em> on the supplied
     * {@code element}.
     *
     * @return true element is annotated
     */
    public static boolean isAnnotated(AnnotatedElement element, Class<? extends Annotation> annotationType) {
        return findAnnotation(element, annotationType) != null;
    }


    public static <A extends Annotation> A findAnnotation(AnnotatedElement element, Class<A> annotationType) {
        return findAnnotation(element, annotationType, new HashSet<Annotation>());
    }


    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A findAnnotation(AnnotatedElement element, Class<A> annotationType,
            Set<Annotation> visited) {

        if (annotationType == null) {
            throw new RuntimeException("annotationType must not be null");
        }

        if (element == null) {
            return null;
        }

        // Cached?
        AnnotationCacheKey key = new AnnotationCacheKey(element, annotationType);
        A annotation = (A) annotationCache.get(key);
        if (annotation != null) {
            return annotation;
        }

        // Directly present?
        annotation = getDeclaredAnnotation(element, annotationType);
        if (annotation != null) {
            annotationCache.put(key, annotation);
            return annotation;
        }

        // Meta-present on directly present annotations?
        A directMetaAnnotation = findMetaAnnotation(annotationType, element.getDeclaredAnnotations(), key,
                visited);
        if (directMetaAnnotation != null) {
            return directMetaAnnotation;
        }

        // Search on interfaces
        if (element instanceof Class) {
            Class<?> clazz = (Class<?>) element;
            for (Class<?> ifc : clazz.getInterfaces()) {
                if (ifc != Annotation.class) {
                    A annotationOnInterface = findAnnotation(ifc, annotationType, visited);
                    if (annotationOnInterface != null) {
                        return annotationOnInterface;
                    }
                }
            }
        }

        // Indirectly present?
        annotation = element.getAnnotation(annotationType);
        if (annotation != null) {
            annotationCache.put(key, annotation);
            return annotation;
        }

        // Meta-present on indirectly present annotations?
        return findMetaAnnotation(annotationType, element.getAnnotations(), key, visited);
    }

    private static <A extends Annotation> A getDeclaredAnnotation(AnnotatedElement element, Class<A> annotationType) {
        for (Annotation annotation: element.getAnnotations()) {
            if(annotation.annotationType().equals(annotationType)) {
                return (A) annotation;
            }
        }
        return null;
    }


    private static <A extends Annotation> A findMetaAnnotation(Class<A> annotationType,
            Annotation[] candidates, AnnotationCacheKey key, Set<Annotation> visited) {

        for (Annotation candidateAnnotation : candidates) {
            Class<? extends Annotation> candidateAnnotationType = candidateAnnotation.annotationType();
            if (!isInJavaLangAnnotationPackage(candidateAnnotationType) && visited.add(candidateAnnotation)) {
                A metaAnnotation = findAnnotation(candidateAnnotationType, annotationType, visited);
                if (metaAnnotation != null) {
                    annotationCache.put(key, metaAnnotation);
                    return metaAnnotation;
                }
            }
        }
        return null;
    }

    private static boolean isInJavaLangAnnotationPackage(Class<? extends Annotation> annotationType) {
        return (annotationType != null && annotationType.getName().startsWith("java.lang.annotation"));
    }

    private static class AnnotationCacheKey {

        private final AnnotatedElement element;
        private final Class<? extends Annotation> annotationType;

        AnnotationCacheKey(AnnotatedElement element, Class<? extends Annotation> annotationType) {
            this.element = element;
            this.annotationType = annotationType;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AnnotationCacheKey) {
                AnnotationCacheKey that = (AnnotationCacheKey) obj;
                return Objects.equals(this.element, that.element)
                        && Objects.equals(this.annotationType, that.annotationType);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.element, this.annotationType);
        }

    }

    public static <A extends Annotation> A findAnnotation(Description description, Class<A> class1) {
        try {
            Method method = description.getTestClass().getMethod(description.getMethodName());
            return findAnnotation(method, class1);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(String.format("Could not find method %s on test class %s ", description.getMethodName(), description.getTestClass().getName()));
        }
    }

    public static DataSet mergeDataSetAnnotations(DataSet classLevelDataSet, DataSet methodLevelDataSet) {

        //only the lists are merged
        String[] value = joinArray(classLevelDataSet.value(), methodLevelDataSet.value());
        String[] tableOrdering = joinArray(classLevelDataSet.tableOrdering(), methodLevelDataSet.tableOrdering());
        String[] executeStatementsBefore = joinArray(classLevelDataSet.executeStatementsBefore(), methodLevelDataSet.executeStatementsBefore());
        String[] executeStatementsAfter = joinArray(classLevelDataSet.executeStatementsAfter(), methodLevelDataSet.executeStatementsAfter());
        String[] executeScriptsAfter = joinArray(classLevelDataSet.executeScriptsAfter(), methodLevelDataSet.executeScriptsAfter());
        String[] executeScriptsBefore = joinArray(classLevelDataSet.executeScriptsBefore(), methodLevelDataSet.executeScriptsBefore());

        DataSet mergedDataSet = new DataSetImpl(value, methodLevelDataSet.executorId(), methodLevelDataSet.strategy(), methodLevelDataSet.useSequenceFiltering(), tableOrdering, 
                methodLevelDataSet.disableConstraints(), methodLevelDataSet.fillIdentityColumns(), executeStatementsBefore, executeScriptsAfter, executeScriptsBefore, executeStatementsAfter,
                methodLevelDataSet.cleanBefore(), methodLevelDataSet.cleanAfter(), methodLevelDataSet.transactional(), methodLevelDataSet.skipCleaningFor());
        return mergedDataSet;
    }

    private static String[] joinArray(String[]... arrays) {
        int length = 0;
        for (String[] array : arrays) {
            if(array != null) {
                length += array.length;
            }
        }

        final String[] result = new String[length];

        int offset = 0;
        for (String[] array : arrays) {
            if(array != null){
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }

        return result;
    }

}
