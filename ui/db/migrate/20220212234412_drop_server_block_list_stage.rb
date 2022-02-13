class DropServerBlockListStage < ActiveRecord::Migration[6.1]
  def change
    drop_table :server_block_list_state
  end
end
