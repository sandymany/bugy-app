package com.leticija.bugy.net;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;

import com.leticija.bugy.R;
import com.leticija.bugy.concurrent.TaskQueue;
import com.leticija.bugy.activities.ResponseCheck;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


public class Requester {

    private static String response;
    private static String baseUrl;
    private static byte[] certBytes;
    private static Context context;


    public static void setUrl(Context cntxt) throws IOException {
        certBytes = IOUtils.toByteArray(cntxt.getResources().openRawResource(R.raw.bugycert));
        System.out.println(new String(certBytes));
        baseUrl = cntxt.getString(R.string.base_ip);
        context = cntxt;

    }

    //RAZMISLI O ZAMJENI HTTPSURLCONNECTION OBJEKTA SA STATIC OBJEKTOM HTTPCLIENT
    //ILI NEKAJ TOG TIPA GDI JEDNOM LOADAŠ SSL CERTIFIKAT I MOREŠ VIŠE PUT
    //KORISTITI ISTI OBJEKT ZA RADITI REQUESTOVE!
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String request(String endpoint, final Map<String,String> headerToSend, final String bodyToSend) {
        try {
            String url = baseUrl + endpoint;
            //open connection
            //URL urlToOpen = new URL(url);

            URL urlToOpen = new URL(url);
            System.out.println("URL to request: "+url);


            trustEveryone();
            HttpsURLConnection conn = (HttpsURLConnection) urlToOpen.openConnection();

            //POTVRDITI DA BUDE ZAPISIVAL
            //secureConn(conn);

            //trustCertificate(conn,context);


            conn.setDoOutput(true);
            conn.setRequestMethod(bodyToSend==null?"GET":"POST");

            // SLANJE HEADERA
            for (Map.Entry<String, String> e: headerToSend.entrySet()) {
                conn.setRequestProperty(e.getKey(),e.getValue());
            }

            //3.SLANJE RESPONSE BODYJA
            if (bodyToSend != null) {
                //System.out.println("SENDING BODY: "+bodyToSend);
                byte[] postData = bodyToSend.getBytes(StandardCharsets.UTF_8);
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.write(postData);
            }
            return getResponse(conn);
        } catch (ProtocolException e1) {
            e1.printStackTrace();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("SOME EXCEPTIOOn OCCCURED !!!!!");
        return null;
    }

    private static String getResponse (HttpURLConnection conn) {
        String response;
        try {
            System.out.println("IM IN GETRESPONSE (SCANNING.)");
            Scanner in = new Scanner(conn.getInputStream());
            in.useDelimiter("\\A");
            if (in.hasNext()) {
                response = in.next();
                System.out.println("GOT RESPONSE IN .request: " + response);
                return response;
            } else {
                System.out.println("NOTHING IN RESPONSE");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void wrapInThread(final String endpoint, final Map<String,String> headerToSend, final String bodyToSend, final FragmentManager fragmentManager,final Context context) {

        TaskQueue.subscribe(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void run() {
                response = Requester.request(endpoint,headerToSend,bodyToSend);
                System.out.println("IN THREAD: "+response);

                //kod svakog requesta se provjeri jel serverov odgovor "false"
                if(!(ResponseCheck.isResponseValid(response,context,fragmentManager))) {
                    // ako je response false, automatski se pojavi dialog da je istekel session cookie.
                    System.out.println("I GOT RESPONSE FALSE, DIALOGUE !");
                    //InterfaceFeatures.sessionCookieDialogue(context,fragmentManager);
                }
                // ako nije, u pozadini (dok korisnik nekaj radi), postavi se određena varijabla na vrijednost responsa.
                else {
                    System.out.println("SETTING VARIABLE !");
                    ResponseCheck.setVariable(endpoint,response);
                }
            }
        });
    }

    private static void trustCertificate(HttpsURLConnection conn,Context context) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException, NoSuchProviderException {

        ByteArrayInputStream derInputStream = new ByteArrayInputStream(context.getResources().openRawResource(R.raw.bugycert).toString().getBytes());

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509","BC");
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(derInputStream);

        String alias = cert.getSubjectX500Principal().getName();

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null,null);
        trustStore.setCertificateEntry("ca", cert);

        /*
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509","BC");
        kmf.init(trustStore, null);
        KeyManager[] keyManagers = kmf.getKeyManagers();
         */

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(trustStore);
        TrustManager[] trustManagers = tmf.getTrustManagers();

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        /*

        SSLContext sslContext = SSLContext.getInstance("TLS","BC");
        sslContext.init(keyManagers, trustManagers, null);

         */

        conn.setSSLSocketFactory(sslContext.getSocketFactory());

    }

    private static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }

    //TODO: ISTESTIRAJ DAL RADI
    private static HttpsURLConnection secureConn(HttpsURLConnection base) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, NoSuchProviderException {
        /*
        try {
            X509DynamicTrustManager tm = new X509DynamicTrustManager();
            Certificate c = CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(certBytes));
            tm.addCertificates(c);
            SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(null, new TrustManager[] { tm }, new java.security.SecureRandom());
            base.setSSLSocketFactory(ctx.getSocketFactory());
            return base;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream keystoreStream = SSLClasspathTrustStoreLoader.class.getResourceAsStream(trustStore);
        keystore.load(keystoreStream, "".toCharArray());
        trustManagerFactory.init(keystore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustManagers, null);
        return null;
        return null;

        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = null;






        cf = CertificateFactory.getInstance("X.509","BC");
        // From https://www.washington.edu/itconnect/security/ca/load-der.crt
        ByteArrayInputStream caInput = new ByteArrayInputStream(context.getResources().openRawResource(R.raw.bugycert).toString().getBytes());
        //InputStream caInput = new BufferedInputStream(new FileInputStream("load-der.crt"));
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

        // Create a KeyStore containing our trusted CAs
        KeyStore keyStore = KeyStore.getInstance("BKS");
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        try {
            context.init(null, tmf.getTrustManagers(), null);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        // Tell the URLConnection to use a SocketFactory from our SSLContext
        base.setSSLSocketFactory(context.getSocketFactory());
        return base;
    */
        return null;
    }

}
