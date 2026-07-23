package io.github.xandewe.pricewatch.store;

public class InactiveStoreException extends RuntimeException {

	public InactiveStoreException() {
		super("Inactive stores cannot receive new listings or prices");
	}
}
