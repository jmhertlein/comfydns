class Task < ApplicationRecord

  def self.task_pending_for? action, server=nil
    return !Task.where(action: action, done: false, server_id: server).empty?
  end
end
