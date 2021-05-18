class AddIndexToClientCfg < ActiveRecord::Migration[6.0]
  def up
    execute <<-SQL
      create index ad_block_client_config_ip_idx on ad_block_client_config (ip);
    SQL
  end

  def down
    execute <<-SQL
      drop index ad_block_client_config_ip_idx;
    SQL
  end
end
