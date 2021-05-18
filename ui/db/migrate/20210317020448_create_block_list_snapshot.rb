class CreateBlockListSnapshot < ActiveRecord::Migration[6.0]
  def change
    create_table :block_list_snapshot, id: :uuid, default: -> {"gen_random_uuid()"} do |t|
      t.uuid :block_list_id, null: false
      t.timestamps
    end

    create_table :blocked_name, id: :uuid, default: -> {"gen_random_uuid()"} do |t|
      t.string :name, null: false
      t.uuid :block_list_snapshot_id, null: false
      t.timestamps
    end

    create_table :block_list, id: :uuid, default: -> {"gen_random_uuid()"} do |t|
      t.string :name, null: false
      t.string :url, null: false
      t.boolean :auto_update, null: false, default: false
      t.string :update_frequency
      t.timestamps
    end

    create_table :server_block_list_state, id: :uuid, default: -> {"gen_random_uuid()"} do |t|
      t.uuid :server_id, null: false
      t.uuid :block_list_id, null: false
      t.uuid :block_list_snapshot_id, null: false
      t.timestamps
    end

    add_foreign_key :block_list_snapshot, :block_list, on_delete: :cascade
    add_foreign_key :blocked_name, :block_list_snapshot, on_delete: :cascade
    
    add_foreign_key :server_block_list_state, :server, on_delete: :cascade
    add_foreign_key :server_block_list_state, :block_list, on_delete: :cascade
    add_foreign_key :server_block_list_state, :block_list_snapshot, on_delete: :cascade
  end
end
