Rails.application.routes.draw do
  # For details on the DSL available within this file, see https://guides.rubyonrails.org/routing.html
  get "/", to: 'index#index'
  
  resources :domain
  post "/domain/:id", to: "domain#create_record"
  delete "/domain/:zid/:rrid", to: "domain#destroy_record"

  get "/server", to: "server#index"
  get "/server/:id", to: "server#edit"
  post "/server/refresh_all", to: "server#refresh_all"
  patch "/server/:id", to: "server#update"
  post "/server/:id/refresh", to: "server#refresh"

  get "/cache", to: "cache#index"
  delete "/cache/:cache_type/:id", to: "cache#destroy"

  devise_for :user, controllers: {
    registrations: 'user/registrations',
    passwords: 'user/passwords',
    sessions: 'user/sessions',
  }

  get "/block_list", to: "block_list#index"
  post "/block_list", to: "block_list#create"
  delete "/block_list/:id", to: "block_list#destroy"
  post "/block_list/:id/refresh", to: "block_list#refresh"

  post "/block_list_client_config", to: "block_list#create_client_config"
  delete "/block_list_client_config/:id", to: "block_list#destroy_client_config"

  post "/flag/:flag", to: "block_list#set_flag"

  get "/qtrace", to: "qtrace#index"
  post "/qtrace", to: "qtrace#qtrace"
end
