package com.github.database.rider.core.configuration;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * A Helper to create {@link Yaml} instance that is pre-configured to not restrict types which can be instantiated
 * during deserialization. Works with both SnakeYaml 1.x and 2.x.
 * <p>
 * SnakeYaml 2.0 solved the
 * <a href="https://www.cve.org/CVERecord?id=CVE-2022-1471">unsafe deserialization vulnerability</a> by changing the
 * default behavior of constructed {@link Yaml} instance to restrict types which can be instantiated during
 * deserialization. This behavior made impossible to define custom DataSet replacers in YAML as they must be
 * instantiated during deserialization. This helper uses reflection to conditionally configure {@link Yaml} instance
 * to accept any types during deserialization if SnakeYaml 2.x is used.
 */
public class SnakeYamlHelper {

    public static Yaml createYaml() {
        LoaderOptions loaderOptions = createLoaderOptions();
        return new Yaml(loaderOptions);
    }

    private static LoaderOptions createLoaderOptions() {
        LoaderOptions loaderOptions = new LoaderOptions();

        try {
            // If Snakeyaml 2.x is used, loaderOptions.setTagInspector(tag -> true) must be called to allow
            // to instantiate any classes during deserialization (e.g. properties.replacers in dbunit.yml).
            // There is no such method in Snakeyaml 1.x, so the reflection is used to try to call the method
            // to support both Snakeyaml 1.x and 2.x.
            Class<?> tagInspectorClass = Class.forName("org.yaml.snakeyaml.inspector.TagInspector");
            Method setTagInspector = loaderOptions.getClass().getMethod("setTagInspector", tagInspectorClass);
            Object isGlobalTagAllowedLambda = createIsGlobalTagAllowedLambda(tagInspectorClass);
            setTagInspector.invoke(loaderOptions, isGlobalTagAllowedLambda);
        } catch (ClassNotFoundException e) {
            // Do nothing as Snakeyaml 1.x is used that allows any classes to be created by default
        } catch (Throwable e) {
            throw new IllegalStateException("Unable to create SnakeYaml LoaderOptions", e);
        }

        return loaderOptions;
    }

    private static Object createIsGlobalTagAllowedLambda(Class<?> tagInspectorClass) throws Throwable {
        MethodType methodType = MethodType.methodType(boolean.class, Tag.class);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle handle = lookup.findStatic(SnakeYamlHelper.class, "isGlobalTagAllowed", methodType);
        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,
                "isGlobalTagAllowed",
                MethodType.methodType(tagInspectorClass),
                methodType,
                handle,
                methodType
        );
        MethodHandle target = callSite.getTarget();
        return target.invoke();
    }

    private static boolean isGlobalTagAllowed(Tag tag) {
        return true;
    }

}
