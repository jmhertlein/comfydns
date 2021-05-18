package com.comfydns.resolver.resolver.block;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public class BlockList {
    private final UUID id;
    private final String name;
    private final URL url;
    private final BlockListType listType;
    private final boolean autoUpdate;
    private final Duration updateFrequency;

    public BlockList(ResultSet rs) throws SQLException, MalformedURLException, DateTimeParseException {
        id = rs.getObject("id", UUID.class);
        name = rs.getString("name");
        url = new URL(rs.getString("url"));
        autoUpdate = rs.getBoolean("auto_update");
        updateFrequency = Duration.parse(rs.getString("update_frequency"));
        listType = BlockListType.match(rs.getString("list_type"));
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public Duration getUpdateFrequency() {
        return updateFrequency;
    }

    public BlockListType getListType() {
        return listType;
    }
}
