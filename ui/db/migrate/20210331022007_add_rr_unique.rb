class AddRRUnique < ActiveRecord::Migration[6.0]
  def up
    execute <<-SQL
      truncate table cached_rr;
      alter table cached_rr add unique (name, rrtype, rrclass, rdata);
    SQL
  end

  def down
    execute <<-SQL
      alter table cached_rr drop constraint cached_rr_name_rrtype_rrclass_rdata_key;
    SQL
  end
end
