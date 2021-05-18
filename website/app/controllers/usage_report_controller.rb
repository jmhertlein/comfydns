class UsageReportController < ActionController::API
  def usage_report
    UsageReport.create!(ip: request.remote_ip, fingerprint: params[:fingerprint])

    render status: 200, json: {}
  end
end
