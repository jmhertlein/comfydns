class SoaController < ApplicationController
  def show
    zid = params[:zid]

    @zone = Zone.find(zid)
    if @zone.nil?
      redirect_to "/domain", alert: "No such zone with id #{zid}"
      return
    end

    @soa = RR.find(@zone.soa_rr_id)

    if @soa.nil?
      redirect_to "/domain", alert: "No SOA for zone with id #{zid}"
      return
    end
  end

  def edit
    zid = params[:zid]
    @zone = Zone.find(zid)
    if @zone.nil?
      redirect_to "/domain", alert: "No such zone with id #{zid}"
      return
    end

    @soa = RR.find(@zone.soa_rr_id)

    if @soa.nil?
      redirect_to "/domain", alert: "No SOA for zone with id #{zid}"
      return
    end
  end

  def update
    zid = params[:zid]

    @zone = Zone.find(zid)
    if @zone.nil?
      redirect_to "/domain", alert: "No such zone with id #{zid}"
      return
    end

    @soa = RR.find(@zone.soa_rr_id)

    if @soa.nil?
      redirect_to "/domain", alert: "No SOA for zone with id #{zid}"
      return
    end

    if ["ttl", "refresh", "retry", "expire", "minimum"].reject{|f| /\d+/.match? params[f]}.size > 0
      redirect_to "/soa/#{zid}/edit", alert: "Invalid format"
      return
    end

    @soa.ttl = params["ttl"].to_i
    @soa.rdata["refresh"] = params["refresh"].to_i
    @soa.rdata["retry"] = params["retry"]
    @soa.rdata["expire"] = params["expire"]
    @soa.rdata["minimum"] = params["minimum"]
    @soa.rdata["mname"] = params["mname"]
    @soa.rdata["rname"] = params["rname"]

    @soa.rdata["serial"] = @soa.rdata["serial"] + 1

    @soa.save!

    redirect_to "/soa/#{zid}", notice: "Updated!"
  end
end
