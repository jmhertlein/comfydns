package com.comfydns.resolver.resolve.block;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum BlockListType {
    NEWLINE_LIST,
    DNSMASQ,
    ;

    private static final Logger log = LoggerFactory.getLogger(BlockListType.class);
    private static final Pattern dnsmasqFormat = Pattern.compile("^address=/(?<name>[^/]+)/.+$");

    public static BlockListType match(String n) {
        return BlockListType.valueOf(n.replace('-', '_').toUpperCase());
    }

    public Optional<String> extractNameFromLine(String line) throws IllegalArgumentException {
        switch (this) {
            case NEWLINE_LIST:
                return Optional.of(line);
            case DNSMASQ:
                if(line.strip().startsWith("#")) {
                    return Optional.empty();
                }
                Matcher m = dnsmasqFormat.matcher(line);
                if(m.matches()) {
                    return Optional.of(m.group("name"));
                } else {
                    log.error("Invalid dnsmasq-formatted line: {}", line);
                    throw new IllegalArgumentException("Invalid dnsmasq-formatted line.");
                }
            default:
                throw new RuntimeException("You forgot to handle a case: " + this.name());
        }
    }
}
