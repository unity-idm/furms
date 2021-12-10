/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.utils;

import java.util.Random;
import java.util.function.Predicate;

public class ExternalIdGenerator {
	/**
	 * Strings are generated based on alphabet w/o vowels and w/o few other
	 * chars. The array is an alphabet without the following: <br>
	 * <code>0 1 2 5   A E I O U   L N S Z</code> <br>
	 * This is what Microsoft omits to generate their product keys. It is still
	 * possible to come up with "fck" or "fvck", but this probably still falls
	 * under "don't optimize for insane".
	 */
	private static final char[] CHARS = "346789bcdfghjkmpqrtvwxy".toCharArray();
	private static final int GENERATED_ID_SIZE = 5;
	private static final Random RANDOM = new Random();

	public static String generate(Predicate<String> uniquenessPred) {
		do {
			String candidate = generate();
			if (uniquenessPred.test(candidate))
				return candidate;
		} while (true);
	}

	private static String generate() {
		return generate(GENERATED_ID_SIZE, RANDOM, CHARS);
	}

	private static String generate(int size, Random random, char[] alphabet) {
		char[] chars = new char[size];
		for (int i = 0; i < size; i++)
			chars[i] = alphabet[random.nextInt(alphabet.length)];
		return String.valueOf(chars);
	}
}
