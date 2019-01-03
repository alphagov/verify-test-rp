package uk.gov.ida.rp.testrp.controllogic;

import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.common.SessionId;
import uk.gov.ida.rp.testrp.contract.Cycle3DatasetDto;
import uk.gov.ida.rp.testrp.contract.LevelOfAssuranceDto;
import uk.gov.ida.rp.testrp.builders.UniversalMatchingDatasetDtoBuilder;
import uk.gov.ida.rp.testrp.contract.TransliterableMdsValueDto;
import uk.gov.ida.rp.testrp.contract.UniversalMatchingDatasetDto;
import uk.gov.ida.rp.testrp.contract.MatchingServiceRequestDto;
import uk.gov.ida.rp.testrp.contract.MatchingServiceResponseDto;
import uk.gov.ida.rp.testrp.contract.UnknownUserCreationRequestDto;
import uk.gov.ida.rp.testrp.contract.UnknownUserCreationResponseDto;
import uk.gov.ida.rp.testrp.repositories.Session;
import uk.gov.ida.rp.testrp.repositories.SessionRepository;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.rp.testrp.builders.Cycle3DatasetDtoBuilder.aCycle3DatasetDto;
import static uk.gov.ida.rp.testrp.builders.MatchingServiceRequestDtoBuilder.aMatchingServiceRequestDto;
import static uk.gov.ida.rp.testrp.builders.UniversalMatchingDatasetDtoBuilder.aUniversalMatchingDatasetDto;
import static uk.gov.ida.rp.testrp.contract.MatchingServiceResponseDto.MATCH;
import static uk.gov.ida.rp.testrp.contract.MatchingServiceResponseDto.NO_MATCH;

@RunWith(MockitoJUnitRunner.class)
public class TestRpMatchingServiceRequestHandlerTest {

    @BeforeClass
    public static void doALittleHackToMakeGuicierHappyForSomeReason() {
        JerseyGuiceUtils.reset();
    }

    @Mock
    private SessionRepository sessionRepository = mock(SessionRepository.class);

    private MatchingServiceRequestHandler matchingServiceRequestHandler;

    @Before
    public void setUp() {
        matchingServiceRequestHandler = new MatchingServiceRequestHandler(sessionRepository);
    }

    @Test
    public void handleRequest_shouldReturnUnsuccessfulResponseWhenMatchingDatasetMatchesSpecialCycle3User() {
        final MatchingServiceRequestDto matchingServiceRequestDto = aMatchingServiceRequestDto()
                .withMatchingDataset(UniversalMatchingDatasetDtoBuilder.theSpecialCycle3UserMatchingDataset())
                .withCycle3Dataset(null)
                .build();

        when(sessionRepository.getSessionForRequestId(any())).thenReturn(Optional.empty());

        final MatchingServiceResponseDto response =
                matchingServiceRequestHandler.handleMatchingRequest(matchingServiceRequestDto);

        assertThat(response.getResult()).isEqualTo(NO_MATCH);
    }

    @Test
    public void handleRequest_shouldReturnSuccessResponseWhenMatchingDatasetDoesNotMatchSpecialCycle3User() {
        final MatchingServiceRequestDto matchingServiceRequestDto = aMatchingServiceRequestDto()
                .withMatchingDataset(aPopulatedMatchingDataset())
                .withCycle3Dataset(null)
                .build();

        when(sessionRepository.getSessionForRequestId(any())).thenReturn(Optional.empty());

        final MatchingServiceResponseDto response =
                matchingServiceRequestHandler.handleMatchingRequest(matchingServiceRequestDto);

        assertThat(response.getResult()).isEqualTo(MATCH);
    }

    @Test
    public void handleRequest_shouldLinkHashedPidToSomeUserWhenMatchingDatasetDoesNotMatchSpecialCycle3User() {
        String hashedPid = UUID.randomUUID().toString();
        final MatchingServiceRequestDto matchingServiceRequestDto = aMatchingServiceRequestDto()
                .withHashedPid(hashedPid)
                .withMatchingDataset(aPopulatedMatchingDataset())
                .withCycle3Dataset(null)
                .build();

        final Optional<Session> session = Optional.ofNullable(newSession(false, false));
        when(sessionRepository.getSessionForRequestId(any())).thenReturn(session);

        matchingServiceRequestHandler.handleMatchingRequest(matchingServiceRequestDto);

        final ArgumentCaptor<Session> argumentCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository, times(1)).updateSession(eq(session.get().getSessionId()), argumentCaptor.capture());
        for (Session capturedSession: argumentCaptor.getAllValues()) {
            assertThat(capturedSession.getMatchedHashedPidForSession().isPresent()).isTrue();
            assertThat(capturedSession.getMatchedHashedPidForSession().get()).isEqualTo(hashedPid);
        }
    }

    @Test
    public void handleRequest_shouldReturnSuccessResponseWhenCycle3DatasetIsPresent() {
        final MatchingServiceRequestDto matchingServiceRequestDto = aMatchingServiceRequestDto()
                .withCycle3Dataset(aPopulatedCycle3Dataset())
                .build();

        when(sessionRepository.getSessionForRequestId(any())).thenReturn(Optional.empty());

        final MatchingServiceResponseDto response =
                matchingServiceRequestHandler.handleMatchingRequest(matchingServiceRequestDto);

        assertThat(response.getResult()).isEqualTo(MATCH);
    }

    @Test
    public void handleRequest_shouldLinkHashedPidToSomeUserWhenCycle3DatasetIsPresent() {
        String hashedPid = UUID.randomUUID().toString();
        final MatchingServiceRequestDto matchingServiceRequestDto = aMatchingServiceRequestDto()
                .withCycle3Dataset(aPopulatedCycle3Dataset())
                .withHashedPid(hashedPid)
                .build();

        final Optional<Session> session = Optional.ofNullable(newSession(false, false));
        when(sessionRepository.getSessionForRequestId(any())).thenReturn(session);

        matchingServiceRequestHandler.handleMatchingRequest(matchingServiceRequestDto);

        final ArgumentCaptor<Session> argumentCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository, times(1)).updateSession(eq(session.get().getSessionId()), argumentCaptor.capture());
        for (Session capturedSession: argumentCaptor.getAllValues()) {
            assertThat(capturedSession.getMatchedHashedPidForSession().isPresent()).isTrue();
            assertThat(capturedSession.getMatchedHashedPidForSession().get()).isEqualTo(hashedPid);
        }
    }

    @Test
    public void handleRequest_shouldReturnNoMatchResponseWhenJourneyIdIsInRepository() {
        String matchingId = "matching-id";
        final MatchingServiceRequestDto matchingServiceRequestDto = aMatchingServiceRequestDto()
                .withMatchId(matchingId)
                .build();

        final Session session = newSession(true, false);
        when(sessionRepository.getSessionForRequestId(matchingServiceRequestDto.getMatchId())).thenReturn(Optional.ofNullable(session));

        final MatchingServiceResponseDto response =
                matchingServiceRequestHandler.handleMatchingRequest(matchingServiceRequestDto);

        assertThat(response.getResult()).isEqualTo(NO_MATCH);
    }

    @Test
    public void shouldMapHashedPidToFailedAccountCreation() {
        String matchId = "a-match-id";
        String hashedPid = "a-hashed-pid";

        final MatchingServiceRequestDto matchingServiceRequestDto = aMatchingServiceRequestDto()
                .withMatchId(matchId)
                .withHashedPid(hashedPid)
                .build();

        final Session session = newSession(true, true);
        when(sessionRepository.getSessionForRequestId(matchingServiceRequestDto.getMatchId())).thenReturn(Optional.ofNullable(session));

        final MatchingServiceResponseDto response = matchingServiceRequestHandler.handleMatchingRequest(matchingServiceRequestDto);

        final ArgumentCaptor<Session> argumentCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository, times(1)).updateSession(eq(session.getSessionId()), argumentCaptor.capture());
        for (Session capturedSession: argumentCaptor.getAllValues()) {
            assertThat(capturedSession.getMatchedHashedPidForSession().isPresent()).isTrue();
            assertThat(capturedSession.getMatchedHashedPidForSession().get()).isEqualTo(hashedPid);
            assertThat(capturedSession.forceLMSNoMatch()).isTrue();
            assertThat(capturedSession.forceLMSUserAccountCreationFail()).isTrue();
        }
        assertThat(response.getResult()).isEqualTo(NO_MATCH);

        when(sessionRepository.getResponseFromSessionForHashedPid(hashedPid)).thenReturn(Optional.ofNullable(UnknownUserCreationResponseDto.FAILURE_RESPONSE));
        final UnknownUserCreationRequestDto unknownUserCreationRequestDto = new UnknownUserCreationRequestDto(hashedPid, LevelOfAssuranceDto.LEVEL_2);

        final UnknownUserCreationResponseDto unknownUserCreationResponseDto = matchingServiceRequestHandler.handleUserAccountCreationRequest(unknownUserCreationRequestDto);

        assertThat(unknownUserCreationResponseDto.getResult()).isEqualTo(UnknownUserCreationResponseDto.FAILURE);

    }

    private Cycle3DatasetDto aPopulatedCycle3Dataset() {
        return aCycle3DatasetDto().addCycle3Data("foo", "bar").build();
    }

    private UniversalMatchingDatasetDto aPopulatedMatchingDataset() {
        return aUniversalMatchingDatasetDto().withFirstName(
                new TransliterableMdsValueDto("Boo boo", null, null, null, true)
        ).build();
    }

    private Session newSession(boolean forceNoMatch, boolean forceUserAccountCreationFail) {
        return new Session(
                SessionId.createNewSessionId(),
                "requestId",
                URI.create("pathUserWasTryingToAccess"),
                "issuerId",
                Optional.ofNullable(1),
                Optional.empty(),
                false,
                forceNoMatch,
                forceUserAccountCreationFail);
    }
}
