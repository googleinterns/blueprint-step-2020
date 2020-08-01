# blueprint-step-2020

**This is not an officially supported Google product.**

### Authentication Flow
This application uses Google sign-in to authenticate and authorize users.
The flow looks like this (all functions found in `auth.js`):

1) `init()` is called when the page loads.
2) The OAuth ClientID is retrieved from the server
3) The Google sign-in button is loaded from the 
[Google Platform Library](https://github.com/google/google-api-javascript-client)
4) The button is configured to collect the proper permissions (read access to Gmail, 
read/write access to Tasks, read/write access to Calendar), and to call `onSignIn()` when
the user successfully authenticates. Note this is automatically called if the user is already 
signed in (this is handled automatically by the Platform library)
5) `onSignIn()` is called, and stores two cookies on the client: `idToken` and `accessToken`. 
These are used to authenticate and authorize users, respectively. The  UI then appears, and 
the sign in button is removed.
6) When the user signs out, `onSignOut()` is called and the cookies are deleted. The UI then 
disappears and the Google sign-in button reappears. 
