package io.github.xandewe.pricewatch.store;

public class InvalidStoreWebsiteUrlException extends RuntimeException {

	public InvalidStoreWebsiteUrlException() {
		super("websiteUrl must be a valid HTTP or HTTPS URL");
	}
}
