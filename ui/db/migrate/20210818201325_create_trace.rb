class CreateTrace < ActiveRecord::Migration[6.1]
  def change
    create_table :trace, id: :uuid, default: -> {"gen_random_uuid()"}  do |t|
      t.uuid :task_id, null: false
      t.string :qname, null: false
      t.integer :qtype, null: false
      t.integer :qclass, null: false
      t.timestamps
    end

    add_foreign_key :trace, :task, on_delete: :cascade
  end
end
