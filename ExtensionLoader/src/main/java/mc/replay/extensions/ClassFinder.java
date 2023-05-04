package mc.replay.extensions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

final class ClassFinder {

    private final Map<String, ReentrantReadWriteLock> classLoadLock = new HashMap<>();
    private final Map<String, Integer> classLoadLockCount = new HashMap<>();

    private final JavaExtensionLoader loader;

    ClassFinder(JavaExtensionLoader loader) {
        this.loader = loader;
    }

    Class<?> getClassByName(String name, boolean resolve, JavaExtensionClassLoader requester) {
        ReentrantReadWriteLock lock;
        synchronized (this.classLoadLock) {
            lock = this.classLoadLock.computeIfAbsent(name, (x) -> new ReentrantReadWriteLock());
            this.classLoadLockCount.compute(name, (x, prev) -> (prev == null) ? 1 : prev + 1);
        }

        lock.writeLock().lock();

        try {
            if (requester != null) {
                try {
                    return requester.loadClass0(name, false, false);
                } catch (ClassNotFoundException ignored) {
                }
            }

            for (JavaExtensionClassLoader loader : this.loader.loaders.values()) {
                try {
                    return loader.loadClass0(name, resolve, false);
                } catch (ClassNotFoundException ignored) {
                }
            }
        } finally {
            synchronized (this.classLoadLock) {
                lock.writeLock().unlock();

                if (this.classLoadLockCount.get(name) == 1) {
                    this.classLoadLock.remove(name);
                    this.classLoadLockCount.remove(name);
                } else {
                    this.classLoadLockCount.computeIfPresent(name, (x, prev) -> prev - 1);
                }
            }
        }

        return null;
    }
}