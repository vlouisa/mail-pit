# TLS CERTIFICATES FOR MAILPIT SMTP SERVER
### GENERAL INFO
Some integration tests need an SMTP server to test the e-mail sending mechanism. For this purpose, we use MailPit.
The MailPit server is configured to use TLS, so we need to provide the server with a certificate and a key. The certificate and key are stored in the `test/resources/mail-pit-certs` directory.
Not only do we need to provide cert and key, but we also need to trust the certificate (because we are using a self-signed certificate). This is done by importing the certificate into the Java truststore `cacerts.jks`.

This README explains how to generate the certificate and key and how to import the certificate into the truststore.

### GENERATE CERTIFICATE, KEY AND TRUSTSTORE
The end result of this step is a certificate (server-cert.pem), a key (server-key.pem) and a truststore (cacerts.jks) with the certificate in it. 

First we need to create a CA certificate and key (for signing the server cert), you can use the following commands:
* ```openssl genrsa 2048 > ca-key.pem```
* ```openssl req -new -x509 -nodes -days 3600 -key ca-key.pem -out ca.pem```

Then we can go ahead and create the server certificate and key:
* ```openssl req -newkey rsa:2048 -days 3600 -nodes -keyout server-key.pem -out server-req.pem```
* ```openssl rsa -in server-key.pem -out server-key.pem```
* ```openssl x509 -req -in server-req.pem -days 3600 -CA ca.pem -CAkey ca-key.pem -set_serial 01 -out server-cert.pem```

Finally, we also need to import the server certificate into the truststore: 
* ```keytool -import -v -trustcacerts -alias mail-pit-ca -file server-cert.pem -keystore cacerts.jks -keypass changeit -storepass changeit```

In the end you should have the following files, in directory `test/resources/mail-pit-certs`:
* server-cert.pem
* server-key.pem
* cacerts.jks

### CONFIGURE MAILPIT
In class MailPitContainer, the SMTP server is configured to use the certificate and key. The truststore is also loaded each time the container starts.
