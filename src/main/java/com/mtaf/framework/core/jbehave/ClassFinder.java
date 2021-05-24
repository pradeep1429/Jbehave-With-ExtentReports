package com.mtaf.framework.core.jbehave;

import com.google.common.collect.ImmutableList;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClassFinder {
    private final ClassLoader classLoader;
    private List<Class<? extends Annotation>> expectedAnnotations;

    public ClassFinder(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public static ClassFinder loadClasses() {
        return new ClassFinder(getDefaultClassLoader());
    }

    public ClassFinder withClassLoader(ClassLoader classLoader) {
        return new ClassFinder(classLoader);
    }

    public List<Class<?>> fromPackage(String packageName) {
        return this.expectedAnnotations == null ? this.allClassesInPackage(packageName) : this.annotatedClassesInPackage(packageName);
    }

    private List<Class<?>> allClassesInPackage(String packageName) {
        try {
            String path = packageName.replace('.', '/');
            if (packageName.isEmpty()) {
                packageName = "/";
            }

            Enumeration<URL> resources = this.classResourcesOn(path);
            ArrayList dirs = new ArrayList();

            while(resources.hasMoreElements()) {
                URL resource = (URL)resources.nextElement();
                dirs.add(resource.toURI());
            }

            List<Class<?>> classes = new ArrayList();
            Iterator var6 = dirs.iterator();

            while(var6.hasNext()) {
                URI directory = (URI)var6.next();
                classes.addAll(this.findClasses(directory, packageName));
            }

            return classes;
        } catch (Exception var8) {
            throw new RuntimeException("failed to find all classes in package [" + packageName + "]", var8);
        }
    }

    public ClassFinder annotatedWith(Class<? extends Annotation>... someAnnotations) {
        this.expectedAnnotations = ImmutableList.copyOf(someAnnotations);
        return this;
    }

    public List<Class<?>> annotatedClassesInPackage(String packageName) {
        Reflections reflections = new Reflections(new Object[]{packageName, new SubTypesScanner(), new TypeAnnotationsScanner(), new MethodAnnotationsScanner(), new ResourcesScanner(), this.getClassLoader()});
        Set<Class<?>> matchingClasses = new HashSet();
        Iterator var4 = this.expectedAnnotations.iterator();

        while(var4.hasNext()) {
            Class<? extends Annotation> expectedAnnotation = (Class)var4.next();
            matchingClasses.addAll(reflections.getTypesAnnotatedWith(expectedAnnotation));
            matchingClasses.addAll(this.classesFrom(reflections.getMethodsAnnotatedWith(expectedAnnotation)));
        }

        return ImmutableList.copyOf(matchingClasses);
    }

    private Collection<Class<?>> classesFrom(Set< Method > annotatedMethods) {
        return (Collection)annotatedMethods.stream().map(Method::getDeclaringClass).collect(Collectors.toList());
    }

    private Enumeration<URL> classResourcesOn(String path) {
        try {
            return this.getClassLoader().getResources(path);
        } catch (IOException var3) {
            throw new IllegalArgumentException("Could not access class path at " + path, var3);
        }
    }

    private List<Class<?>> findClasses(URI directory, String packageName) {
        try {
            String scheme = directory.getScheme();
            String schemeSpecificPart = directory.getSchemeSpecificPart();
            if (scheme.equals("jar") && schemeSpecificPart.contains("!")) {
                return this.findClassesInJar(directory, packageName);
            } else if (scheme.equals("file")) {
                return this.findClassesInFileSystemDirectory(directory, packageName);
            } else {
                throw new IllegalArgumentException("cannot handle URI with scheme [" + scheme + "]");
            }
        } catch (Exception var5) {
            throw new RuntimeException("failed to find classesin directory=[" + directory + "], with packageName=[" + packageName + "]", var5);
        }
    }

    private List<Class<?>> findClassesInJar(URI jarDirectory, String packageName) throws IOException {
        String schemeSpecificPart = jarDirectory.getSchemeSpecificPart();
        List<Class<?>> classes = new ArrayList();
        String[] split = schemeSpecificPart.split("!");
        URL jar = new URL(split[0]);
        ZipInputStream zip = new ZipInputStream(jar.openStream());
        Throwable var8 = null;

        try {
            ZipEntry entry;
            try {
                while((entry = zip.getNextEntry()) != null) {
                    if (entry.getName().endsWith(".class")) {
                        String className = classNameFor(entry);
                        if (className.startsWith(packageName) && this.isNotAnInnerClass(className)) {
                            this.loadClassWithName(className).ifPresent(classes::add);
                        }
                    }
                }
            } catch (Throwable var18) {
                var8 = var18;
                throw var18;
            }
        } finally {
            if (zip != null) {
                if (var8 != null) {
                    try {
                        zip.close();
                    } catch (Throwable var17) {
                        var8.addSuppressed(var17);
                    }
                } else {
                    zip.close();
                }
            }

        }

        return classes;
    }

    private List<Class<?>> findClassesInFileSystemDirectory(URI jarDirectory, String packageName) {
        List<Class<?>> classes = new ArrayList();
        File directory = new File(jarDirectory);
        if (!directory.exists()) {
            return classes;
        } else {
            File[] files = directory.listFiles();
            if (files != null) {
                File[] var6 = files;
                int var7 = files.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    File file = var6[var8];
                    if (file.isDirectory()) {
                        classes.addAll(this.findClasses(file.toURI(), packageName + "." + file.getName()));
                    } else if (file.getName().endsWith(".class") && this.isNotAnInnerClass(file.getName())) {
                        this.correspondingClass(packageName, file).ifPresent(classes::add);
                    }
                }
            }

            return classes;
        }
    }

    private static String classNameFor(ZipEntry entry) {
        return entry.getName().replaceAll("[$].*", "").replaceAll("[.]class", "").replace('/', '.');
    }

    private Optional<? extends Class<?>> loadClassWithName(String className) {
        try {
            return Optional.of(this.getClassLoader().loadClass(className));
        } catch (ClassNotFoundException var3) {
            return Optional.empty();
        } catch (NoClassDefFoundError var4) {
            return Optional.empty();
        }
    }

    private Optional<? extends Class<?>> correspondingClass(String packageName, File file) {
        String fullyQualifiedClassName = this.packagePrefixFor(packageName) + this.simpleClassNameOf(file);
        return this.loadClassWithName(fullyQualifiedClassName);
    }

    private String packagePrefixFor(String packageName) {
        return !packageName.isEmpty() && !packageName.equals("/") ? packageName + '.' : "";
    }

    private static ClassLoader getDefaultClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private String simpleClassNameOf(File file) {
        return file.getName().substring(0, file.getName().length() - 6);
    }

    private boolean isNotAnInnerClass(String className) {
        return !className.contains("$");
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }
}

