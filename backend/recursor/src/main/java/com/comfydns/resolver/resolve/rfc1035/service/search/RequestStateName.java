package com.comfydns.resolver.resolve.rfc1035.service.search;

public enum RequestStateName {
    TRY_TO_ANSWER_WITH_LOCAL_INFORMATION,
    FIND_BEST_SERVER_TO_ASK,
    SEND_SERVER_QUERY,
    HANDLE_RESPONSE_TO_ZONE_QUERY,
    HANDLE_RESPONSE_TO_NSDNAME_LOOKUP,
    SEND_NSDNAME_LOOKUP,
    IMMEDIATE_DEATH,
    INITIAL_CHECKING,
    SNAME_CHECKING_STATE,
    DOUBLE_CHECK_SEND_STATE,
    DOUBLE_CHECK_RESULT_STATE,
    SEND_RESPONSE_STATE,
    ZONE_TRANSFER_STATE,
    RESPONSE_READY_STATE,
    ;
}
