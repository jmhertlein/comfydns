class AddIndexToCachedRR < ActiveRecord::Migration[6.0]
  def up
    execute <<-SQL
      create index cached_rr_name_idx on cached_rr (name);
    SQL
  end

  def down
    execute <<-SQL
      drop index cached_rr_name_idx;
    SQL
  end
end
