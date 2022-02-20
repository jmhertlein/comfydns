class DropServer < ActiveRecord::Migration[6.1]
  def change
    remove_column :task, :server_id
    drop_table :server_authority_state
    drop_table :server
  end
end
