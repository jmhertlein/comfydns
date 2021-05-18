class CreateStartOfAuthority < ActiveRecord::Migration[6.0]
  def change
    create_table :start_of_authority, id: :uuid, default: -> {"gen_random_uuid()"} do |t|
      t.string :mname
      t.string :rname
      t.integer :serial, default: 0
      t.integer :refresh, default: 360
      t.integer :retry, default: 60
      t.integer :expire, default: 720
      t.integer :minimum, default: 60
      t.uuid :zone_id
      t.timestamps
    end

    add_foreign_key :start_of_authority, :zone, on_delete: :cascade
  end
end