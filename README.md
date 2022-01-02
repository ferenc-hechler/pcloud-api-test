# pcloud-api-test
Test application for the pCloud Java SDK https://docs.pcloud.com/sdks/java.html

# How to get the client-ID?

create a new app under: https://docs.pcloud.com/my_apps/



# How to get an access token (oauth2)

see https://docs.pcloud.com/methods/oauth_2.0/authorize.html


## query code

In a webbrowser enter the url with the client id of the application:

```
https://my.pcloud.com/oauth2/authorize?client_id=<client_id>&response_type=code
```

Response after login:

```
Your access code is:
<access_code>
Hostname:
eapi.pcloud.com
Please enter this code into the application.
This code will expire after 600 seconds.
```


## query access token for code

```
https://eapi.pcloud.com/oauth2_token?client_id=<client_di>&client_secret=<client_secret>&code=<code> 
```

Response:

```
{
  "result": 0,
  "userid": 1273851,
  "locationid": 2,
  "token_type": "bearer",
  "access_token": "..."
}
```

# checksum file


```
<fileid> = remoteFile.id().substr(1) ["f8631891410" -> "8631891410"]
```

```
https://eapi.pcloud.com/checksumfile?fileid=8631891410
  Authorization: Bearer <access_token>
  Accept: application/json
```

response:

```
{
"result": 0,
"sha256": "a197aca63cf3da125648f26b795a2ea300622422433d8e369c2d7ab2ec48b4aa",
"sha1": "25ed7cacb92df5ad84485488b99b4d2d881a0101",
"metadata":{
"name": "Getting started with pCloud.pdf",
"created": "Sun, 12 Dec 2021 18:47:18 +0000",
"thumb": false,
"modified": "Sun, 12 Dec 2021 18:47:18 +0000",
"isfolder": false,
"fileid": 8631891410,
"hash": 5484734392585590647,
"comments": 0,
"category": 4,
"id": "f8631891410",
"isshared": false,
"ismine": true,
"size": 16371465,
"parentfolderid": 0,
"contenttype": "application/pdf",
"icon": "document"
}
}
```


## about simple hash

https://docs.pcloud.com/structures/metadata.html

```
hash int 64 bit integer representing hash of the contents of the file 
            can be used to determine if two files are the same or to 
            monitor file contents for changes. Present only for files.
```
