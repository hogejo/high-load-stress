package hu.laba;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.UUID;

@JsonInclude(Include.NON_NULL)
public record Vehicle(
	@JsonProperty("uuid") @Nullable UUID uuid,
	@JsonProperty("rendszam") String registration,
	@JsonProperty("tulajdonos") String owner,
	@JsonProperty("forgalmi_ervenyes") String validity,
	@JsonProperty("adatok") Set<String> data
) {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	public static final String dateFormat = "yyyy-MM-dd";

	public void requireValid() {
		if (registration == null || owner == null || validity == null || data == null) {
			throw new IllegalArgumentException("fields of vehicle must not be null");
		}
		if (registration.isBlank() || owner.isBlank() || validity.isBlank()) {
			throw new IllegalArgumentException("fields of vehicle can not be empty");
		}
		if (registration.length() > 20 || owner.length() > 200) {
			throw new IllegalArgumentException("fields are too long");
		}
		if (data.stream().anyMatch(String::isBlank)) {
			throw new IllegalArgumentException("data fields of vehicle can not be blank");
		}
		try {
			new SimpleDateFormat(dateFormat).parse(validity);
		} catch (Exception exception) {
			throw new IllegalArgumentException("validity date must be YYYY-MM-DD: " + validity, exception);
		}
	}

	public String toJsonString() {
		try {
			return objectMapper.writeValueAsString(this);
		} catch (JsonProcessingException jsonProcessingException) {
			throw new RuntimeException(jsonProcessingException);
		}
	}

	public String toCreateJsonString() {
		try {
			return objectMapper.writeValueAsString(new Vehicle(
				null, registration(), owner(), validity(), data()
			));
		} catch (JsonProcessingException jsonProcessingException) {
			throw new RuntimeException(jsonProcessingException);
		}
	}

}
