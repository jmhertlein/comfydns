class AddBlsUniqConstraint < ActiveRecord::Migration[6.0]
  def up
    execute <<-SQL
      alter table block_list_snapshot add unique (block_list_id);
      alter table block_list add unique (url);
      create index blocked_name_name_idx on blocked_name (name);
    SQL
  end

  def down
    execute <<-SQL
      alter table block_list_snapshot drop constraint block_list_snapshot_block_list_id_key;
      alter table block_list drop constraint block_list_url_key;
      drop index blocked_name_name_idx;
    SQL
  end
end
