package io.github.xandewe.pricewatch.store;

import java.util.UUID;

public class StoreNotFoundException extends RuntimeException {

	public StoreNotFoundException(UUID storeId) {
		super("Store not found: " + storeId);
	}
}
