/*
 * Copyright (c) 2019 ArhivPRO
 * All rights reserved.
 */

package com.leticija.bugy.net;

import android.os.Build;
import android.support.annotation.RequiresApi;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.function.Consumer;

class X509DynamicTrustManager implements X509TrustManager {

	private X509TrustManager trustManager;
	private ArrayList<Certificate> certList;

	public X509DynamicTrustManager() throws Exception {
		certList = new ArrayList<>();
		reloadTrustManager();
	}

	/**
	 * Instantiates a new X509TrustManager with the latest certificates
	 */
	private void reloadTrustManager() throws Exception{
		KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
		ts.load(null, new char[0]); // Initializing empty trust store
		for (Certificate cert : certList) { // Add queued certificates to the keystore
			ts.setCertificateEntry(UUID.randomUUID().toString(), cert); // Filling trust store with cert list
		}
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ts); // Initialize a new TrustManagerFactory with the truststore we created
		TrustManager[] tms = tmf.getTrustManagers();
		for (int i = 0; i < tms.length; i++) {
			if (tms[i] instanceof X509TrustManager) {
				trustManager = (X509TrustManager) tms[i]; // Get the X509TrustManager from the Factory.
				return;
			}
		}

		throw new NoSuchAlgorithmException("No X509TrustManager in TrustManagerFactory");
	}

	/**
	 * Removes a certificate from the pending list. Automatically reloads the TrustManager
	 * @param cert is not null and was already added
	 * @throws Exception if cannot be reloaded
	 */
	public void removeCertificates(Certificate... cert) throws Exception {
		certList.removeAll(Arrays.asList(cert));
		reloadTrustManager();
	}

	/**
	 * Adds a list of certificates to the manager. Automatically reloads the TrustManager
	 * @param certs is not null
	 * @throws Exception if cannot be reloaded
	 */
	public void addCertificates(Certificate... certs) throws Exception {
		certList.addAll(Arrays.asList(certs));
		reloadTrustManager();
	}

	/**
	 * @param certURLs URLs to SSL certificates to be added to trust manager.
	 * @throws Exception probably IOException when reading from resource or when bytes are of invalid format.
	 */
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public void addCertificates(URL... certURLs) throws Exception {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		for(URL url : certURLs) {
			try (InputStream in = url.openStream()) {
				certList.add(cf.generateCertificate(in));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		reloadTrustManager();
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public void removeCertificates(URL... certURLs) throws Exception {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		for(URL url : certURLs) {
			try (InputStream in = url.openStream()) {
				certList.remove(cf.generateCertificate(in));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		reloadTrustManager();
	}

	// if ever necessary
	// basically will get all certificates from these stores and add them to this main store.
	// NEEDS TESTING !!!
	// DOESNT WORK

	@RequiresApi(api = Build.VERSION_CODES.N)
	public void mergWithKeyStores(Map.Entry<URL, char[]>... trustStores) throws Exception {
		Arrays.stream(trustStores).forEach(new Consumer<Map.Entry<URL, char[]>>() {
			@Override
			public void accept(Map.Entry<URL, char[]> t) {
				try {
					X509DynamicTrustManager.this.mergeWithKeyStores(X509DynamicTrustManager.this.getKeyStoreFromURL(t.getKey(), t.getValue()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public void removeCertsFromKeyStores(Map.Entry<URL, char[]>... trustStores) throws Exception {
		Arrays.stream(trustStores).forEach(new Consumer<Map.Entry<URL, char[]>>() {
			@Override
			public void accept(Map.Entry<URL, char[]> t) {
				try {
					X509DynamicTrustManager.this.removeCertsFromKeyStores(X509DynamicTrustManager.this.getKeyStoreFromURL(t.getKey(), t.getValue()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private KeyStore getKeyStoreFromURL(URL store, char[] password) throws Exception {
		String[] supportedKeyStores = new String[] {"JKS", "PKCS12"};
		for(String keyStoreType : supportedKeyStores) {
			try {
				KeyStore ts = KeyStore.getInstance(keyStoreType);
				ts.load(store.openStream(), password);
				return ts;
			} catch (Exception e) { }
		}
		return null;
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public void mergeWithKeyStores(KeyStore... kss ) throws Exception {
		Arrays.stream(kss).forEach(new Consumer<KeyStore>() {
			@Override
			public void accept(KeyStore ks) {
				try {
					X509DynamicTrustManager.this.addCertsFromKeyStore(ks);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		reloadTrustManager();
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public void removeCertsFromKeyStores(KeyStore... kss ) throws Exception {
		Arrays.stream(kss).forEach(new Consumer<KeyStore>() {
			@Override
			public void accept(KeyStore ks) {
				try {
					X509DynamicTrustManager.this.removeCertsFromKeyStore(ks);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		reloadTrustManager();
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	private void addCertsFromKeyStore(final KeyStore ks) throws Exception{
		Collections.list(ks.aliases()).forEach(new Consumer<String>() {
			@Override
			public void accept(String s) {
				try {
					System.out.println(ks + " ALIAS.");
					certList.add(ks.getCertificate(s));
				} catch (KeyStoreException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	private void removeCertsFromKeyStore(final KeyStore ks) throws Exception {
		Collections.list(ks.aliases()).forEach(new Consumer<String>() {
			@Override
			public void accept(String s) {
				try {
					certList.remove(ks.getCertificate(s));
				} catch (KeyStoreException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		trustManager.checkClientTrusted(chain, authType);
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		try {
			trustManager.checkServerTrusted(chain, authType);
		} catch (CertificateException cx) {
			// DO NOT USE THIS SECTION
			// Development logic only. Trusts the incoming untrusted certificate.
			// addCertificate(chain[0]);
			// reloadTrustManager();
			// trustManager.checkServerTrusted(chain, authType);
		}
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return trustManager.getAcceptedIssuers();
	}
}