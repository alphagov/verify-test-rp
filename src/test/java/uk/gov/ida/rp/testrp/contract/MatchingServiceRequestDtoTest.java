package uk.gov.ida.rp.testrp.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.FixtureHelpers;
import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.rp.testrp.builders.UniversalAddressDtoBuilder;
import uk.gov.ida.rp.testrp.builders.SimpleMdsValueDtoBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static uk.gov.ida.rp.testrp.builders.UniversalMatchingDatasetDtoBuilder.aUniversalMatchingDatasetDto;

//
// These tests exist to prevent accidentally breaking our contract with the matching service. If they fail, ensure you
// are making changes in such a way that will not break our contract (i.e. use the expand/contract pattern); don't
// simply fix the test.
//
public class MatchingServiceRequestDtoTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        objectMapper = Jackson.newObjectMapper().setDateFormat(StdDateFormat.getDateInstance());
        objectMapper.registerModule(new Jdk8Module());
    }

    @Test
    public void shouldDeserializeFromLegacyJson() throws Exception {
        MatchingServiceRequestDto deserializedValue =
            objectMapper.readValue(jsonFixture("legacy-matching-service-request.json"), MatchingServiceRequestDto.class);

        MatchingServiceRequestDto expectedValue = getMatchingServiceRequestDto(false);

        Assertions.assertThat(deserializedValue).isEqualTo(expectedValue);
    }

    @Test
    public void shouldSerializeUniversalDataToJson() throws IOException {
        MatchingServiceRequestDto matchingServiceRequestDto = getMatchingServiceRequestDto(true);

        String jsonString = objectMapper.writeValueAsString(matchingServiceRequestDto);

        Assertions.assertThat(jsonString).isEqualTo(jsonFixture("universal-matching-service-request.json"));
    }

    @Test
    public void shouldDeserializeUniversalDataFromJson() throws Exception {
        MatchingServiceRequestDto deserializedValue =
            objectMapper.readValue(jsonFixture("universal-matching-service-request.json"), MatchingServiceRequestDto.class);

        MatchingServiceRequestDto expectedValue = getMatchingServiceRequestDto(true);

        Assertions.assertThat(deserializedValue).isEqualTo(expectedValue);
    }

    private MatchingServiceRequestDto getMatchingServiceRequestDto(boolean withNonLatinScriptNames) {
        LevelOfAssuranceDto levelOfAssurance = LevelOfAssuranceDto.LEVEL_1;
        Cycle3DatasetDto cycle3DatasetDto = Cycle3DatasetDto.createFromData(ImmutableMap.of("NI", "1234"));
        DateTime dateTime = DateTime.parse("2014-02-01T01:02:03.567Z");

        UniversalMatchingDatasetDto universalDataset = aUniversalMatchingDatasetDto()
                .withFirstName(getTransliterableMdsValue("walker", withNonLatinScriptNames))
                .withMiddleNames(getTransliterableMdsValue("walker", withNonLatinScriptNames))
                .withSurnames(Arrays.asList(getTransliterableMdsValue("smith", withNonLatinScriptNames), getTransliterableMdsValue("walker", withNonLatinScriptNames)))
                .withDateOfBirth(getSimpleMdsValue(LocalDate.fromDateFields(dateTime.toDate()), dateTime))
                .withAddresses(Arrays.asList(getAddressDto("NW6", dateTime), getAddressDto("SW11", dateTime)))
                .withGender(getSimpleMdsValue(GenderDto.FEMALE, dateTime))
                .build();
        String hashedPid = "8f2f8c23-f767-4590-aee9-0842f7f1e36d";
        String matchId = "cda6126c-9695-4051-ba6f-27a8938a0b03";
        return new MatchingServiceRequestDto(
            universalDataset,
            Optional.of(cycle3DatasetDto),
            hashedPid,
            matchId,
            levelOfAssurance);
    }

    private String jsonFixture(String filename) throws IOException {
        return objectMapper.writeValueAsString(objectMapper.readValue(FixtureHelpers.fixture(filename), JsonNode.class));
    }

    private UniversalAddressDto getAddressDto(String postcode, DateTime dateTime) {
        return new UniversalAddressDtoBuilder()
                .withFrom(dateTime)
                .withInternationalPostCode("123")
                .withLines(ImmutableList.of("a", "b")).withPostCode(postcode)
                .withTo(dateTime)
                .withUPRN("urpn")
                .withVerified(true)
                .build();
    }

    private <T> SimpleMdsValueDto<T> getSimpleMdsValue(T value, DateTime dateTime) {
        return new SimpleMdsValueDtoBuilder<T>()
            .withFrom(dateTime)
            .withTo(dateTime)
            .withValue(value)
            .withVerifiedStatus(true)
            .build();
    }

    private TransliterableMdsValueDto getTransliterableMdsValue(String value, boolean withNonLatinScriptValue) {
        DateTime to = DateTime.parse("2014-02-01T01:02:03.567Z");
        DateTime from = DateTime.parse("2014-02-01T01:02:03.567Z");

        return new TransliterableMdsValueDto(value, withNonLatinScriptValue ? "Ωαλκερ" : null, from, to, true);
    }

}
