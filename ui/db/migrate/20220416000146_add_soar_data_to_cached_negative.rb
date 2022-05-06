class AddSoarDataToCachedNegative < ActiveRecord::Migration[6.1]
  def change
    rename_column :cached_negative, :name, :qname
    rename_column :cached_negative, :ttl, :r_ttl


    add_column :cached_negative, :r_name, :text
    add_column :cached_negative, :r_class, :integer
    add_column :cached_negative, :r_mname, :text
    add_column :cached_negative, :r_rname, :text
    add_column :cached_negative, :r_serial, :bigint
    add_column :cached_negative, :r_refresh, :bigint
    add_column :cached_negative, :r_retry, :bigint
    add_column :cached_negative, :r_expire, :bigint
    add_column :cached_negative, :r_minimum, :bigint
  end
end
