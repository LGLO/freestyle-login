# freestyle-login
Evaluating Freestyle (https://frees.io) with simple application

Target was to learn how to work with _Freestyle_.

Blog post is expected in July 2017.

Running this application requires `GH_CLIENT_ID` and `GH_CLIENT_SECRET` variables to be set in your environment.  
If you don't want to use GitHub login, please set some dummy values.  
To use GitHub OAuth 2.0 please setup application in your GH account:  
"Settings" -> "Developer settings" -> "OAuth Applications", click "Register a new application".  
You should copy "Client ID" and "Client Secret" from that page to environment variables.  
Important thing to set is: "Authorization callback URL" to `http://localhost:9000/login/github-callback`

Once application is running, please go to: http://localhost:9000/register .  
There you can use "email" (which doesn't need to be an email) and password or GitHub to register new account.  
You can try again with the same GitHub account or email to see an error.  
To use newly registered account please go to http://localhost:9000/login .  
If everything goes well, blank page should be displayed and JWT should be present in `Authorization` header of the response.
