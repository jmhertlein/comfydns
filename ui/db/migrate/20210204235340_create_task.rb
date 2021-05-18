class CreateTask < ActiveRecord::Migration[6.0]
  def change
    create_table :task, id: :uuid, default: -> {"gen_random_uuid()"} do |t|
      t.string :action, null: false
      t.uuid :server_id
      t.boolean :started, default: false
      t.boolean :done, default: false
      t.boolean :failed, default: false
      t.jsonb :args
      t.timestamps
    end

    add_foreign_key :task, :server, on_delete: :cascade
  end
end
