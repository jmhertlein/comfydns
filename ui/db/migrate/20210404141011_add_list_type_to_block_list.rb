class AddListTypeToBlockList < ActiveRecord::Migration[6.0]
  def change
    add_column :block_list, :list_type, :string, null: false, default: "newline-list"
  end
end
