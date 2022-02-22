require 'rfc1035'

class DomainController < ApplicationController
  def index
    @zones = Zone.all

    @zone_host_counts = RR.group(:zone_id).count()
  end

  def create
    unless /[a-zA-Z0-9\-_]+./.match? params[:name]
      redirect_to "/domain", alert: "Invalid name: #{params[:name]}"
      return
    end

    zone = Zone.create name: params[:name]
    soa_record = RR.create(
            name: zone.name, 
            rrtype: DNS::RRTYPE_TO_VALUE["SOA"],
            rrclass: DNS::RRCLASS_TO_VALUE["IN"],
            ttl: 60,
            zone_id: zone.id,
            rdata: {
              "mname": "",
              "rname": "",
              "serial": 1,
              "refresh": 360,
              "retry": 60,
              "expire": 720,
              "minimum": 60,
            }
          )
    zone.soa_rr_id = soa_record.id
    zone.save!

    redirect_to domain_path(zone)
  end

  def show
    @zone = Zone.find(params[:id])
    @host_records = RR.where(zone_id: @zone.id, rrclass: DNS::RRCLASS_TO_VALUE["IN"], rrtype: DNS::RRTYPE_TO_VALUE["A"])
    @host_records = [] if @host_records.nil?
  end

  def destroy
    @zone = Zone.find(params[:id])
    @zone.delete
    redirect_to "/domain"
  end

  def create_record
    zone = Zone.find_by(id: params[:id])
    if zone.nil?
      redirect_to "/domain", alert: "No such zone with id #{params[:id]}"
      return
    end

    unless DNS::RRTYPE_TO_VALUE.has_key? params[:rrtype]
      redirect_to "/domain/#{params[:id]}", alert: "No such rrtype: #{params[:rrtype]}"
      return
    end

    unless DNS::RRCLASS_TO_VALUE.has_key? params[:rrclass]
      redirect_to "/domain/#{params[:id]}", alert: "No such rrclass: #{params[:rrclass]}"
      return
    end

    unless /\d+/.match? params[:ttl]
      redirect_to "/domain/#{params[:id]}", alert: "Invalid TTL: #{params[:ttl]}"
      return
    end

    unless /\d+\.\d+\.\d+\.\d+/.match? params[:ip_address]
      redirect_to "/domain/#{params[:id]}", alert: "Invalid IP address: #{params[:ip_address]}"
      return
    end

    unless /^[a-zA-Z0-9\-_]+$/.match? params[:hostname]
      redirect_to "/domain/#{params[:id]}", alert: "Invalid host name: #{params[:hostname]}"
      return
    end

    hostname = nil
    if params[:hostname].end_with? ".#{zone.name}"
      hostname = params[:hostname]
    else
      hostname = "#{params[:hostname]}.#{zone.name}"
    end

    RR.transaction do
      record = RR.create(
        name: hostname, 
        rrtype: DNS::RRTYPE_TO_VALUE[params[:rrtype]],
        rrclass: DNS::RRCLASS_TO_VALUE[params[:rrclass]],
        ttl: params[:ttl].to_i,
        zone_id: zone.id,
        rdata: {"address": params[:ip_address]}
      )
      if params[:rrtype] == "A" && zone.gen_ptrs
        ptr_record = RR.create(
          name: DNS::ip_to_in_addr(params[:ip_address]),
          rrtype: DNS::RRTYPE_TO_VALUE["PTR"],
          rrclass: DNS::RRCLASS_TO_VALUE["IN"],
          ttl: params[:ttl].to_i,
          zone_id: zone.id,
          rdata: {"ptrdname": hostname}
        )
      end

      soa_rr = RR.find zone.soa_rr_id
      soa_rr.rdata["serial"] = soa_rr.rdata["serial"] + 1
      soa_rr.save!
      
      CachedNegative.delete_by('name=:hname or name like :wcard', :hname => hostname, :wcard => "%."+hostname)
    end

    redirect_to "/domain/#{zone.id}", notice: "Record added!"
  end

  def destroy_record
    zone = Zone.find_by(id: params[:zid])
    if zone.nil?
      redirect_to "/domain", alert: "No such zone with id #{params[:zid]}"
      return
    end
    
    rr = RR.find(params[:rrid])
    if zone.nil?
      redirect_to "/domain/#{zone.id}", alert: "No such record with id #{params[:rrid]}"
      return
    end

    rr.delete

    soa_rr = RR.find zone.soa_rr_id
    soa_rr.rdata["serial"] = soa_rr.rdata["serial"] + 1
    soa_rr.save!

    redirect_to "/domain/#{zone.id}", notice: "Record deleted!"
  end
end
