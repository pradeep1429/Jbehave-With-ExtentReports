package com.mtaf.framework.core.jbehave;

import java.util.Arrays;

public class RootPackage {
    public RootPackage() {
    }

    public static String forPackage(Package testPackage) {
        String[] elements = testPackage.getName().split("\\.");
        if (elements.length == 1) {
            return elements[0];
        } else {
            elements = (String[]) Arrays.copyOfRange(elements, 0, elements.length - 1);
            return concatElements(elements);
        }
    }

    private static String concatElements(String[] subpaths) {
        StringBuilder builder = new StringBuilder();
        String[] arrPath = subpaths;

        for(int element = 0; element < subpaths.length; ++element) {
            String path = arrPath[element];
            builder.append(path).append(".");
        }
        return builder.toString().isEmpty() ? "" : builder.substring(0, builder.length() - 1);
    }
}
