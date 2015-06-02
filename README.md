# spring-oauth2-samples
Some sample projects using Spring OAuth 2

#Resource owner password-based grant
To obtain an access token usign `password` grant type do the following command:

    curl -u my-trusted-client:secret http://localhost:8080/oauth/token -d grant_type=password -d username=user -d password=password -X POST --verbose

You shoud see something like that:

    * Connected to localhost (::1) port 8080 (#0)
    * Server auth using Basic with user 'my-trusted-client'
    > POST /oauth/token HTTP/1.1
    > Authorization: Basic bXktdHJ1c3RlZC1jbGllbnQ6c2VjcmV0
    > User-Agent: curl/7.37.1
    > Host: localhost:8080
    > Accept: */*
    > Content-Length: 51
    > Content-Type: application/x-www-form-urlencoded
    >     
    * upload completely sent off: 51 out of 51 bytes
    < HTTP/1.1 200 OK
    * Server Apache-Coyote/1.1 is not blacklisted
    < Server: Apache-Coyote/1.1
    < X-Content-Type-Options: nosniff
    < X-XSS-Protection: 1; mode=block
    < Cache-Control: no-cache, no-store, max-age=0, must-revalidate
    < Pragma: no-cache
    < Expires: 0
    < X-Frame-Options: DENY
    < X-Application-Context: bootstrap
    < Cache-Control: no-store
    < Pragma: no-cache
    < Content-Type: application/json;charset=UTF-8
    < Transfer-Encoding: chunked
    < Date: Tue, 02 Jun 2015 20:41:46 GMT
    < 
    * Connection #0 to host localhost left intact
    {"access_token":"428dbdb6-bab6-468f-a905-e48355b6e5b9","token_type":"bearer","expires_in":599,"scope":"read write trust"}Juans-MacBook-Pro:Downloads jfcorugedo$ 

#Implicit grant

To obtain an access token using `token` reponse type execute this command:

    curl user:password@localhost:8080/oauth/authorize -d response_type=token -d client_id=my-trusted-client -d redirect_uri=http://yourredirect.es -X POST --verbose



You shoud obtain this response:

    * Connected to localhost (::1) port 8080 (#0)
    * Server auth using Basic with user 'user'
    > POST /oauth/authorize HTTP/1.1
    > Authorization: Basic dXNlcjpwYXNzd29yZA==
    > User-Agent: curl/7.37.1
    > Host: localhost:8080
    > Accept: */*
    > Content-Length: 75
    > Content-Type: application/x-www-form-urlencoded
    > 
    * upload completely sent off: 75 out of 75 bytes
    < HTTP/1.1 302 Found
    * Server Apache-Coyote/1.1 is not blacklisted
    < Server: Apache-Coyote/1.1
    < X-Content-Type-Options: nosniff
    < X-XSS-Protection: 1; mode=block
    < Cache-Control: no-cache
    < Pragma: no-cache
    < Expires: Thu, 01 Jan 1970 00:00:00 GMT
    < X-Frame-Options: DENY
    < Set-Cookie: JSESSIONID=EBEA7FC2011DA129439C3E919326C53D; Path=/; HttpOnly
    < X-Application-Context: bootstrap
    < Cache-Control: no-store
    < Location: http://yourredirect.es#access_token=930fd6ef-c0fc-4267-89c9-89de3be1c339&token_type=bearer&expires_in=599&scope=read%20write%20trust
    < Content-Language: en-US
    < Content-Length: 0
    < Date: Tue, 02 Jun 2015 21:16:07 GMT
    
