package io.github.xandewe.pricewatch.store;

public class DuplicateStoreException extends RuntimeException {

	public DuplicateStoreException() {
		super("A store with this name already exists");
	}
}
