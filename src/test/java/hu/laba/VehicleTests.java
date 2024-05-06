package hu.laba;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
	public void generateRandomVehicle() {
		for (int i = 0; i < 1999; i++) {
			Vehicle vehicle = VehicleGenerator.generateRandom(i);
			vehicle.requireValid();
		}
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

}
