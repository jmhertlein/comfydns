require 'rfc1035'

class QtraceController < ApplicationController
  def index
    @supported_qtypes = DNS::RRTYPE_TO_VALUE.each
      .select{|name, value| DNS::SUPPORTED_RRTYPES.include?(name)}
      .map{|l,r| [l, l]}

    @traces = Trace.all.order(created_at: :desc)
  end

  def qtrace
    raw_qtype = params["qtype"]
    unless DNS::RRTYPE_TO_VALUE.has_key? raw_qtype
      redirect_to "/qtrace/", alert: "No such qtype: #{raw_qtype}"
      return
    end
    qtype = DNS::RRTYPE_TO_VALUE[raw_qtype]

    qname = params["qname"]
    unless /^[a-zA-Z0-9\-.]+$/.match? qname
      redirect_to "/qtrace/", alert: "Invalid domain name: #{qname}"
      return
    end
    
    Task.create!(action: "TRACE_QUERY", args: {qname: qname, qtype: qtype})

    redirect_to "/qtrace/", notice: "Trace submitted: #{qname} #{raw_qtype} IN."
  end

  def destroy
    id = params['id']
    Trace.find(id).delete
    redirect_to "/qtrace/", notice: "Trace deleted."
  end

  def view
    id = params['id']
    @trace = Trace.find(id)

    if @trace.nil?
      redirect_to "/qtrace", alert: "No such trace with id #{id}"
      return
    end

    @events = TraceEvent.where(trace_id: @trace.id).order(event_index: :asc)
  end
end
