Rails.application.routes.draw do
  # For details on the DSL available within this file, see https://guides.rubyonrails.org/routing.html
  get "/", to: 'index#index'

  post "/api/usage_report/:fingerprint", to: 'usage_report#usage_report'
end
