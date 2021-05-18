class AddIndexToCachedNegative < ActiveRecord::Migration[6.0]
  def up
    execute <<-SQL
      create index cached_negative_name_idx on cached_negative (name);
    SQL
  end

  def down
    execute <<-SQL
      drop index cached_negative_name_idx;
    SQL
  end
end
