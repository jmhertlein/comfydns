# frozen_string_literal: true

class User::PasswordsController < Devise::PasswordsController
  # GET /resource/password/new
  # def new
  #   super
  # end

  # POST /resource/password
  # def create
  #   super
  # end

  # GET /resource/password/edit?reset_password_token=abcdef
  # def edit
  #   super
  # end

  # PUT /resource/password
  def update
    if ENV["COMFYDNS_UI_PASSPHRASE"].nil?
      redirect_to new_user_password_path, alert: "COMFYDNS_UI_PASSPHRASE is not set. Password resetting is disabled."
      return
    end

    unless params[:user][:passphrase].eql? ENV["COMFYDNS_UI_PASSPHRASE"]
      redirect_to new_user_password_path, alert: "You need to provide the correct passphrase."
      return
    end

    self.resource = User.find_by(email: params[:user][:email])

    if resource.reset_password(params[:user][:password], params[:user][:password_confirmation])
      resource.after_database_authentication
      sign_in(resource_name, resource)
      redirect_to "/", notice: "Password reset!"
      return
    else
      redirect_to new_user_password_path, alert: "Passwords don't match."
      return
    end
  end

  # protected

  # def after_resetting_password_path_for(resource)
  #   super(resource)
  # end

  # The path used after sending reset password instructions
  # def after_sending_reset_password_instructions_path_for(resource_name)
  #   super(resource_name)
  # end
end
