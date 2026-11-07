package org.robolectric.nativeruntime;

import org.robolectric.pluginapi.NativeRuntimeLoader;

/**
 * Shadow DefaultNativeRuntimeLoader on Linux (aarch64) to allow Robolectric JVM tests
 * to execute without failing on missing prebuilt native libraries.
 */
public class DefaultNativeRuntimeLoader implements NativeRuntimeLoader {
    public static void injectAndLoad() {
        // No-op for non-native Robolectric tests on aarch64
    }

    @Override
    public void ensureLoaded() {
        // No-op for non-native Robolectric tests on aarch64
    }
}
