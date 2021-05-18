class IndexController < ApplicationController
  def index
    @servers_need_refresh = []
    server_states = ServerAuthorityState.all.group_by{|ss| ss.server_id}
    
    Zone.all.each do |z|
      Server.all.each do |s|
        s_id = s.id
        records = server_states[s_id]
        records = [] if records.nil?
        records = records.select{|r| r.soa_name.eql? z.name}
        if records.empty?
          @servers_need_refresh << [s_id, z.name, z.start_of_authority.serial, "n/a"] 
        else
          @servers_need_refresh << [s_id, z.name, z.start_of_authority.serial, records[0].soa_serial] unless z.start_of_authority.serial == records[0].soa_serial
        end
      end
    end

    zones = Zone.all.map{|z| z.name}

    server_states.each do |s_id, records|
      records.each do |r|
        if !zones.include? r.soa_name
          @servers_need_refresh << [s_id, r.soa_name, "n/a", r.soa_serial] 
        end
      end
    end
  end
end
