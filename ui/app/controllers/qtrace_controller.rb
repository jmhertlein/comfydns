require 'rfc1035'

class QtraceController < ApplicationController
  def index
    @supported_qtypes = DNS::RRTYPE_TO_VALUE.each
      .select{|name, value| DNS::SUPPORTED_RRTYPES.include?(name)}
      .map{|l,r| [l, l]}
  end

  def qtrace
    raw_qtype = params["qtype"]
    unless DNS::RRTYPE_TO_VALUE.has_key? raw_qtype
      redirect_to "/qtrace/", alert: "No such qtype: #{raw_qtype}"
      return
    end
    qtype = DNS::RRTYPE_TO_VALUE[qtype]

    qname = params["qname"]
    unless /^[a-zA-Z0-9\-.]+$/.match? qname
      redirect_to "/qtrace/", alert: "Invalid domain name: #{qname}"
      return
    end
    
    Task.create!(action: "TRACE_QUERY", args: {qname: qname, qtype: qtype})
  end
end
