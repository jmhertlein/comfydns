class AddRCodeToCachedNegative < ActiveRecord::Migration[6.1]
  def change
    execute <<-SQL
      delete from cached_negative;
    SQL
    add_column :cached_negative, :r_rcode, :integer, null: false
  end
end
