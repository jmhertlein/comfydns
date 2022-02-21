package com.comfydns.resolver.util;


import com.comfydns.resolver.resolver.rfc1035.cache.AuthorityRRSource;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.SOARData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.IntStream;

public class DatabaseUtils {
    private static final Logger log = LoggerFactory.getLogger(DatabaseUtils.class);

    private DatabaseUtils() {}

}
