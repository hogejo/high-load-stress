package me.hogejo.highload.stress;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class VehicleTests {

	@Test
	void generateRandomRegistration() {
		assertEquals("AAA-000", VehicleGenerator.generateRandomRegistration(0));
		assertEquals("AAA-001", VehicleGenerator.generateRandomRegistration(1));
		assertEquals("AAA-999", VehicleGenerator.generateRandomRegistration(999));
		assertEquals("AAB-000", VehicleGenerator.generateRandomRegistration(1_000));
		assertEquals("AAB-001", VehicleGenerator.generateRandomRegistration(1_001));
		assertEquals("AAB-999", VehicleGenerator.generateRandomRegistration(1_999));
		assertEquals("ZZZ-000", VehicleGenerator.generateRandomRegistration(17_575_000));
		assertEquals("ZZZ-999", VehicleGenerator.generateRandomRegistration(17_575_999));
		assertThrowsExactly(IndexOutOfBoundsException.class, () -> VehicleGenerator.generateRandomRegistration(17_576_000));
		assertThrowsExactly(IndexOutOfBoundsException.class, () -> VehicleGenerator.generateRandomRegistration(Integer.MAX_VALUE));
		assertThrowsExactly(IndexOutOfBoundsException.class, () -> VehicleGenerator.generateRandomRegistration(-1));
		assertThrowsExactly(IndexOutOfBoundsException.class, () -> VehicleGenerator.generateRandomRegistration(Integer.MIN_VALUE));
	}

	@Test
	public void generateUniqueRandomVehicles() {
		Set<Vehicle> vehicles = new HashSet<>();
		for (int i = 0; i < 100_000; i++) {
			Vehicle vehicle = VehicleGenerator.generateRandom(i);
			vehicle.requireValid();
			vehicles.add(vehicle);
			assertEquals(i + 1, vehicles.size());
		}
		Set<String> vehicleRegistrations = vehicles.stream().map(Vehicle::registration).collect(Collectors.toSet());
		assertEquals(vehicles.size(), vehicleRegistrations.size());
	}

	@Test
	public void serialiseDeserialise() throws JsonProcessingException {
		final ObjectMapper objectMapper = new ObjectMapper();
		for (int i = 0; i < 1999; i++) {
			Vehicle vehicleA = VehicleGenerator.generateRandom(i);
			vehicleA.requireValid();
			Vehicle vehicleB = objectMapper.readValue(vehicleA.toJsonString(), Vehicle.class);
			vehicleB.requireValid();
			assert vehicleA.equals(vehicleB);
		}
	}

	@Test
	public void equalsCheck() throws JsonProcessingException {
		UUID uuid = UUID.fromString("fc3ec2fa-43ce-4165-992d-f7ac899838fa");
		String registration = "AAA-123";
		String owner = "foobar";
		String validity = "2025-05-05";
		final Vehicle a = new Vehicle(uuid, registration, owner, validity, Set.of("a", "b", "c"));
		final Vehicle b = new Vehicle(uuid, registration, owner, validity, Set.of("c", "a", "b"));
		assert Objects.equals(a, b);
		final String aJson = "{\"uuid\":\"fc3ec2fa-43ce-4165-992d-f7ac899838fa\",\"rendszam\":\"AAA-123\",\"tulajdonos\":\"foobar\",\"forgalmi_ervenyes\":\"2025-05-05\",\"adatok\":[\"a\",\"b\",\"c\"]}";
		final String bJson = """
			{
				"uuid": "fc3ec2fa-43ce-4165-992d-f7ac899838fa",
				"tulajdonos": "foobar",
				"adatok": ["c", "a", "b"],
				"forgalmi_ervenyes": "2025-05-05",
				"rendszam": "AAA-123"
			}
			""";
		assert !Objects.equals(aJson, bJson);
		final ObjectMapper objectMapper = new ObjectMapper();
		final Vehicle sa = objectMapper.readValue(aJson, Vehicle.class);
		final Vehicle sb = objectMapper.readValue(bJson, Vehicle.class);
		assert Objects.equals(sa, sb);
	}

}
