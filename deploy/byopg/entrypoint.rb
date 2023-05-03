#!/usr/bin/env ruby

require 'subprocess'
require 'fileutils'
require 'pathname'

Dir.chdir("/app/ui")

rails_secret_pn = Pathname.new "/opt/comfydns/rails_secret.txt"

if rails_secret_pn.exist?
  puts "Using existing rails secret_key_base"
  skb = rails_secret_pn.read.strip
else
  puts "Generating new rails secret_key_base"
  skb = Subprocess.check_output(["rails", "secret"])
  rails_secret_pn.write skb
end

# Dir.chdir("/app/ui")
puts "db migrate"
new_env = ENV.to_h
new_env["SECRET_KEY_BASE"] = skb
Subprocess.check_call(["rails", "db:create", "db:migrate"], env: new_env)

Dir.chdir("/app")
puts "setup done, starting supervisor"
Subprocess.check_call(["/usr/bin/supervisord"], env: new_env)