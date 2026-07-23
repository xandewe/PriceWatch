package io.github.xandewe.pricewatch.store;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class StoreNameNormalizer {

	public String clean(String name) {
		return name.trim().replaceAll("\\s+", " ");
	}

	public String normalize(String name) {
		return clean(name).toLowerCase(Locale.ROOT);
	}
}
