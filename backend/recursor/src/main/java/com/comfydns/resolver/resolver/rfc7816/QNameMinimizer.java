package com.comfydns.resolver.resolver.rfc7816;

import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.service.search.SList;
import com.comfydns.resolver.resolver.rfc1035.service.search.SearchContext;

public class QNameMinimizer {
    private QNameMinimizer() {}

    public static Question buildMinimizedQNameQuery(SearchContext sCtx) {
        Question q = sCtx.getCurrentQuestion();
        String sName = sCtx.getSName();
        String sListZone = sCtx.getSList().getZone();

        if(shouldSendActualQuestion(sName, sListZone)) {
            return new Question(sName, q.getqType(), q.getqClass());
        } else {
            return new Question(minimizeQName(sName, sListZone),
                    KnownRRType.NS, q.getqClass()
            );
        }
    }

    public static boolean shouldSendActualQuestion(String sName, String sListZone) {
        String sNameMinusSListZone = sName.substring(0, sName.length() - sListZone.length());
        String[] split = sNameMinusSListZone.split("\\.");
        return split.length == 1;
    }

    public static String minimizeQName(String sName, String sListZone) {
        String sNameMinusSListZone = sName.substring(0, sName.length() - sListZone.length());
        String[] split = sNameMinusSListZone.split("\\.");
        String oneMoreLabel = split[split.length-1];
        if(sListZone.isEmpty()) {
            return oneMoreLabel;
        } else {
            return String.join(".", oneMoreLabel, sListZone);
        }
    }
}
