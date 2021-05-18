package com.comfydns.util.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class IdFile {
    private final Path path;

    public IdFile(Path path) {
        this.path = path;
    }

    public UUID read() throws IOException {
        return UUID.fromString(Files.readString(path).strip());
    }

    public UUID readOrGenerateAndRead() throws IOException {
        if(Files.exists(path)) {
            return read();
        } else {
            UUID ret = UUID.randomUUID();
            Files.writeString(path, ret.toString());
            return ret;
        }
    }
}
