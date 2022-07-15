require "test_helper"

class SoaControllerTest < ActionDispatch::IntegrationTest
  test "should get show" do
    get soa_show_url
    assert_response :success
  end

  test "should get edit" do
    get soa_edit_url
    assert_response :success
  end
end
