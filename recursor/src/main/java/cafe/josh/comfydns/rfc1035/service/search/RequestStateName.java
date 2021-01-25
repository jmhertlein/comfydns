package cafe.josh.comfydns.rfc1035.service.search;

public enum RequestStateName {
    TRY_TO_ANSWER_WITH_LOCAL_INFORMATION,
    FIND_BEST_SERVER_TO_ASK,
    SEND_SERVER_QUERY,
    HANDLE_RESPONSE_TO_ZONE_QUERY,
    HANDLE_RESPONSE_TO_NSDNAME_LOOKUP;
}