class DropStartOfAuthority < ActiveRecord::Migration[6.1]
  def change
    drop_table :start_of_authority
  end
end
