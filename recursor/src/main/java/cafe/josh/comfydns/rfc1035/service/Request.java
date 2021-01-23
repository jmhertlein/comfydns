package cafe.josh.comfydns.rfc1035.service;

import cafe.josh.comfydns.rfc1035.message.struct.Header;
import cafe.josh.comfydns.rfc1035.message.struct.Message;

public interface Request {
    public Message getMessage();
    public void answer(Message m);
}
