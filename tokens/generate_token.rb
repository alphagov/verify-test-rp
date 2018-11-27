#!/usr/bin/env ruby

require 'jwt'
require 'openssl'

rsa_private = OpenSSL::PKey::RSA.new File.read 'dev-keys/sample_rp_signing_primary.private.pem'
rsa_public = rsa_private.public_key

payload = { epoch: 1,
            valid_until: '2118-11-27T11:30:00.000Z',
            issued_to: 'willp-bl' }

payload = {}

token = JWT.encode payload, rsa_private, 'RS512'

puts "token: #{token}"

begin
decoded_token = JWT.decode token, rsa_public, true, { algorithm: 'RS512' }
puts "decoded_token: #{decoded_token}"
rescue JWT::VerificationError => e
puts "error! could not validate token"
end



