require 'test_helper'
require 'rfc1035'

class DomainControllerTest < ActionDispatch::IntegrationTest
  # test "the truth" do
  #   assert true
  # end

  test "ip_to_in_addr works" do
    assert_equal "4.4.8.8.in-addr.arpa", DNS::ip_to_in_addr("8.8.4.4")
    assert_equal "4.1.168.192.in-addr.arpa", DNS::ip_to_in_addr("192.168.1.4")
  end
end
