class CreateTraceEvent < ActiveRecord::Migration[6.1]
  def change
    create_table :trace_event, id: :uuid, default: -> {"gen_random_uuid()"}  do |t|
      t.integer :event_index, null: false
      t.uuid :trace_id, null: false
      t.string :event_type, null: false
      t.jsonb :event, null: false
      t.timestamps
    end

    add_foreign_key :trace_event, :trace, on_delete: :cascade
  end
end
