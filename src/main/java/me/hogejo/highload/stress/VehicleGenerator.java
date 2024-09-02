package me.hogejo.highload.stress;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VehicleGenerator {

	public static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public static final String registrationCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; // 26 letters
	private static final int maximumId = 17_576_000 - 1;
	private static final List<String> ownerWords = List.of(
		"Abraka", "Bab", "Consulting", "Digital", "Enterprájz",
		"Forgalmi", "Gépjármű", "Hotel", "Intézet", "Javító",
		"Kerületi", "Lakossági", "Market", "New", "Olló",
		"Pénzügyi", "Q4", "R6", "Sárospataki", "Társulat",
		"U9", "Vitorlás", "W12", "X1", "Yolo",
		"Zab"
	);
	private static final List<String> dataWords = List.of(
		"piros", "zöld", "kék", "fehér", "szürke", "sárga", "vörös", "fekete", "lila", "rózsaszín"
	);

	private static UUID generateRandomUUID(int id) {
		return UUID.nameUUIDFromBytes(String.valueOf(id).getBytes());
	}

	public static String generateRandomRegistration(int id) {
		if (id > maximumId || id < 0) {
			throw new IndexOutOfBoundsException();
		}
		int numbers = id % 1_000;
		id /= 1_000;
		StringBuilder letters = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			int remainder = id % 26;
			id /= 26;
			letters.insert(0, registrationCharacters.charAt(remainder));
		}
		return String.format("%s-%03d", letters, numbers);
	}

	public static String generateRandomOwner(int id) {
		String suffix = switch (id % 4) {
			case 2 -> " Kft.";
			case 1 -> " EV.";
			default -> "";
		};
		StringBuilder owner = new StringBuilder();
		owner.append(ownerWords.get(id % ownerWords.size()));
		id /= ownerWords.size();
		while (id > 0) {
			owner.append(" ").append(ownerWords.get(id % ownerWords.size()));
			id /= ownerWords.size();
		}
		owner.append(suffix);
		return owner.toString();
	}

	public static String generateRandomDate(int id) {
		return dateFormat.format(LocalDate.of(1900 + (id % 200), (id % 12) + 1, (id % 25) + 1));
	}

	public static Set<String> generateRandomData(int id) {
		return Set.of(
			dataWords.get(id % dataWords.size()),
			"alváz: " + "RD2000A" + id,
			switch (id % 4) {
				case 2 -> "rozsdás";
				case 1 -> "kopott szélvédőmatrica";
				default -> "kiégett lámpa";
			}
		);
	}

	public static Vehicle generateRandom(int id) {
		return new Vehicle(
			generateRandomUUID(id),
			generateRandomRegistration(id),
			generateRandomOwner(id),
			generateRandomDate(id),
			generateRandomData(id)
		);
	}

}
