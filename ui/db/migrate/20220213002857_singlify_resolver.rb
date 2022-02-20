require 'rfc1035'

class SinglifyResolver < ActiveRecord::Migration[6.1]
  def change
    add_column :zone, :soa_rr_id, :uuid
    add_foreign_key :zone, :rr, column: :soa_rr_id

    reversible do |dir|
      Zone.all.each do |zone|
        dir.up do
          soa_record = RR.create(
            name: zone.name, 
            rrtype: DNS::RRTYPE_TO_VALUE["SOA"],
            rrclass: DNS::RRCLASS_TO_VALUE["IN"],
            ttl: 60,
            zone_id: zone.id,
            rdata: {
              "mname": "",
              "rname": "",
              "serial": 0,
              "refresh": 360,
              "retry": 60,
              "expire": 720,
              "minimum": 60,
            }
          )

          zone.soa_rr_id = soa_record.id
          zone.save
        end

        dir.down do
          tmp = zone.soa_rr_id
          zone.soa_rr_id = nil
          zone.save
          RR.delete tmp
        end
      end
    end
  end
end
