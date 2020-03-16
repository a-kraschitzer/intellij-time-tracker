package net.kraschitzer.intellij.plugin.time_tracker.communication;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.util.net.HTTPMethod;
import lombok.extern.slf4j.Slf4j;
import net.kraschitzer.intellij.plugin.time_tracker.communication.exceptions.*;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.request.SelectWorkItemRequest;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.request.StartTrackingRequest;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.request.UpdateTrackRequest;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.response.*;
import net.kraschitzer.intellij.plugin.time_tracker.model.enums.Reason;
import net.kraschitzer.intellij.plugin.time_tracker.persistence.SettingsState;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

@Slf4j
public class CommunicatorSevenPaceAPI implements ICommunicator {

    private static final String PROTOCOL = "https://";

    private SettingsState settings = ServiceManager.getService(SettingsState.class);
    private Client client;

    private TrackingStateModel currentState;
    private String protocolAddress;

    private boolean initialized = false;
    private boolean authenticated = false;

    private CommunicatorSevenPaceAPI() {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    private final NotificationGroup localNotificationGroup = new NotificationGroup(
            "Timetracker Notifications", NotificationDisplayType.BALLOON, true);

    public void initialize() throws CommunicatorException {
        if (!initialized) {
            authenticated = false;

            String apiAddress = settings.url;

            log.debug("Initializing with apiAddress: {}", apiAddress);

            if (StringUtils.isBlank(apiAddress)) {
                throw new ComNotInitializedException("Missing apiAddress");
            }

            client = ClientBuilder.newClient();
            protocolAddress = PROTOCOL + apiAddress;
            initialized = true;
        }
    }

    public void authenticate() throws CommunicatorException {
        if (!authenticated) {
            String accessToken = settings.accessToken;
            if (StringUtils.isBlank(accessToken)) {
                throw new ComNotAuthenticatedException();
            }
            try {
                refreshTokenIfNecessary();
                currentState = getCurrentStateInitialized(true);
                authenticated = true;
            } catch (ComNotInitializedException e) {
                authenticated = false;
            }
        }
    }

    private void refreshTokenIfNecessary() throws CommunicatorException {
        String refreshToken = Optional.ofNullable(settings.refreshToken).orElseThrow(ComNotInitializedException::new);
        String expiresString = Optional.ofNullable(settings.expires).orElseThrow(ComNotInitializedException::new);
        LocalDateTime expires = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(expiresString)), ZoneId.of("UTC"));

        if (LocalDateTime.now().plus(1, ChronoUnit.DAYS).isAfter(expires)) {
            Token token = refreshToken(refreshToken);
            settings.accessToken = token.getAccess_token();
            settings.refreshToken = token.getRefresh_token();
            settings.expires = String.valueOf(token.getExpires().toInstant(ZoneOffset.UTC).toEpochMilli());
        }
    }

    @Override
    public PinContext pinCreate() throws CommunicatorException {
        return apiPostJson("/pin/create", "", PinContext.class);
    }

    @Override
    public PinStatus pinStatus(String secret) {
        secret = '"' + secret + '"';
        Invocation.Builder invBuilder = client.target(protocolAddress)
                .path("/api/pin/status")
                .queryParam("api-version", "2.1").request();
        Invocation in = invBuilder
                .accept(MediaType.APPLICATION_JSON)
                .buildPost(Entity.json(secret));
        Response response = in.invoke();
        if (response.getStatus() - 200 < 100) {
            return response.readEntity(PinStatus.class);
        } else {
            try {
                System.out.println(response.readEntity(TimetrackerResponse.class));
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
        System.exit(1);
        return null;
    }

    @Override
    public Token token(String secret) throws CommunicatorException {
        MultivaluedMap<String, String> form = new MultivaluedHashMap<>();
        form.put("client_id", Collections.singletonList("OpenApi"));
        form.put("grant_type", Collections.singletonList("authorization_code"));
        form.put("code", Collections.singletonList(secret));

        return apiPostToken("/token", form, Token.class);
    }

    @Override
    public Token refreshToken(String refreshToken) throws CommunicatorException {
        MultivaluedMap<String, String> form = new MultivaluedHashMap<>();
        form.put("client_id", Collections.singletonList("OpenApi"));
        form.put("grant_type", Collections.singletonList("refresh_token"));
        form.put("refresh_token", Collections.singletonList(settings.refreshToken));

        return apiPostToken("/token", form, Token.class);
    }

    @Override
    public TrackingStateModel getCurrentState(boolean expand) throws CommunicatorException {
        checkInitialization();
        return getCurrentStateInitialized(expand);
    }

    private TrackingStateModel getCurrentStateInitialized(boolean expand) throws CommunicatorException {
        return apiGet("/tracking/client/current/" + expand, TrackingStateModel.class);
    }

    @Override
    public Track selectWorkItem(SelectWorkItemRequest selectWorkItemRequest) throws CommunicatorException {
        checkInitialization();
        return apiPostJson("/tracking/client/selected", selectWorkItemRequest, Track.class);
    }

    @Override
    public LatestWorkLogsModel getLatestWorkLogs(int count) throws CommunicatorException {
        checkInitialization();
        return apiGet("/tracking/client/latest/10", LatestWorkLogsModel.class);
    }

    @Override
    public TrackingStateModel startTracking(StartTrackingRequest startTrackingRequest) throws CommunicatorException {
        checkInitialization();
        return apiPostJson("/tracking/client/startTracking", startTrackingRequest, TrackingStateModel.class);
    }

    @Override
    public TrackingStateModel stopTracking(Reason reason) throws CommunicatorException {
        checkInitialization();
        return apiPostJson("/tracking/client/stopTracking/" + reason.ordinal(), "", TrackingStateModel.class);
    }

    @Override
    public TrackingStateModel notifyNextIdleCheck(int selectedOption) throws CommunicatorException {
        checkInitialization();
        return null;
    }

    @Override
    public TrackingStateModel notifyOfActivity() throws CommunicatorException {
        checkInitialization();
        return null;
    }

    @Override
    public TrackingStateModel updateTrack(UpdateTrackRequest updateTrackRequest) throws CommunicatorException {
        checkInitialization();
        return null;
    }

    @Override
    public SearchResultModel searchWorkItem(String workItemIdOrTitle) throws CommunicatorException {
        checkInitialization();
        return apiPostJson("/tracking/client/search", workItemIdOrTitle, SearchResultModel.class);
    }

    @Override
    public SearchResultModel searchWorkItemByModel(String workItemIdOrTitle) throws CommunicatorException {
        checkInitialization();
        return apiPostJson("/tracking/client/searchByQuery", "{\"query\": \"" + workItemIdOrTitle + "\"}", SearchResultModel.class);
    }

    @Override
    public void resetInitialization() {
        initialized = false;
        authenticated = false;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    private <T> T apiGet(String url, Class<T> responseType) throws ComErrorException, ComErrorParseException, ComException, ComHostNotFoundException, ComParseException {
        return request(HTTPMethod.GET, "/api" + url, null, responseType, true);
    }

    private <T> T apiPostJson(String url, Object body, Class<T> responseType) throws ComErrorException, ComErrorParseException, ComException, ComHostNotFoundException, ComParseException {
        return request(HTTPMethod.POST, "/api" + url, Entity.json(body), responseType, true);
    }

    private <T> T apiPostToken(String url, MultivaluedMap<String, String> form, Class<T> responseType) throws ComErrorParseException, ComException, ComHostNotFoundException, ComErrorException, ComParseException {
        return request(HTTPMethod.POST, url, Entity.form(form), responseType, false);
    }

    private <T> T request(HTTPMethod method, String url, Entity entity, Class<T> responseType, boolean authenticated) throws ComErrorParseException, ComException, ComHostNotFoundException, ComErrorException, ComParseException {
        Invocation.Builder ib = client.target(protocolAddress + url + "?api-version=2.1")
                .request(MediaType.APPLICATION_JSON_TYPE);
        if (authenticated) {
            ib.header(HttpHeaders.AUTHORIZATION, "Bearer " + settings.accessToken);
        }
        Invocation in;
        switch (method) {
            case GET:
                in = ib.buildGet();
                break;
            case POST:
                in = ib.buildPost(entity);
                break;
            default:
                throw new ComException("Given Method " + method + " not supported");
        }
        Response response = in.invoke();
        if (response != null && response.getStatus() - 200 < 100) {
            response.bufferEntity();
            try {
                return response.readEntity(responseType);
            } catch (ProcessingException e) {
                throw new ComParseException("Failed to parse response:'" + response.readEntity(String.class) + "'", e);
            }
        } else if (response != null && response.getStatus() == HttpStatus.SC_NOT_FOUND) {
            throw new ComHostNotFoundException();
        } else {
            if (response == null) {
                throw new ComErrorParseException("Response was null.");
            }
            response.bufferEntity();
            if (response.getEntity() == null) {
                throw new ComErrorParseException("Response entity was null.");
            }
            try {
                throw new ComErrorException(response.readEntity(TimetrackerResponse.class).getError());
            } catch (ProcessingException e) {
                throw new ComErrorParseException("Failed to parse error communication error '" + response.readEntity(String.class) + "'", e);
            }
        }
    }

    private void checkInitialization() throws CommunicatorException {
        if (!initialized || !authenticated) {
            initialize();
        }
    }

}
