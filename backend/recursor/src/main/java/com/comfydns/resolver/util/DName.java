package com.comfydns.resolver.util;

public class DName {
    private DName() {}

    public static String chop(String dname, int numDots) {
        String[] split = dname.split("\\.");
        int toCopy = Math.min(numDots+1, split.length);

        String[] out = new String[toCopy];
        int srcPos = split.length - toCopy;
        System.arraycopy(split, srcPos, out, 0, toCopy);

        return String.join(".", out);
    }
}
