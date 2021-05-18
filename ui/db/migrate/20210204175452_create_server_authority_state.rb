class CreateServerAuthorityState < ActiveRecord::Migration[6.0]
  def change
    create_table :server_authority_state, id: :uuid, default: -> {"gen_random_uuid()"} do |t|
      t.string :soa_name
      t.bigint :soa_serial
      t.uuid :server_id
      t.timestamps
    end

    add_foreign_key :server_authority_state, :server, on_delete: :cascade
  end
end
