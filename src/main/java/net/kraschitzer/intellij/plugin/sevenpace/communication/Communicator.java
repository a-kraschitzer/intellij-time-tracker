package net.kraschitzer.intellij.plugin.sevenpace.communication;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.util.net.HTTPMethod;
import lombok.extern.slf4j.Slf4j;
import net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions.*;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.request.SelectWorkItemRequest;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.request.StartTrackingRequest;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.request.UpdateTrackRequest;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.Error;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.*;
import net.kraschitzer.intellij.plugin.sevenpace.model.enums.Reason;
import net.kraschitzer.intellij.plugin.sevenpace.utils.SettingKeys;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

@Slf4j
public class Communicator implements ICommunicator {

    private static final String PROTOCOL = "https://";

    private PropertiesComponent props;
    private Client client;

    private TrackingStateModel currentState;
    private String protocolAddress;

    private boolean initialized = false;
    private boolean authenticated = false;

    private Communicator() {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    private final NotificationGroup localNotificationGroup = new NotificationGroup(
            "7Pace Timetracker Notifications", NotificationDisplayType.BALLOON, true);

    public void initialize() throws CommunicatorException {
        if (!initialized) {
            authenticated = false;

            this.props = PropertiesComponent.getInstance();
            String apiAddress = props.getValue(SettingKeys.URL);

            log.debug("Initializing with apiAddress: {}", apiAddress);

            if (StringUtils.isBlank(apiAddress)) {
                throw new CommunicatorNotInitializedException("Missing apiAddress");
            }

            client = ClientBuilder.newClient();
            protocolAddress = PROTOCOL + apiAddress;
            initialized = true;
        }
    }

    public void authenticate() throws CommunicatorException {
        if (!authenticated) {
            String accessToken = props.getValue(SettingKeys.ACCESS_TOKEN);
            if (StringUtils.isBlank(accessToken)) {
                throw new CommunicatorNotAuthenticatedException();
            }
            try {
                refreshTokenIfNecessary();
                currentState = getCurrentStateInitialized(true);
                authenticated = true;
            } catch (CommunicatorNotInitializedException e) {
                authenticated = false;
            }
        }
    }

    private void refreshTokenIfNecessary() throws CommunicatorException {
        String refreshToken = Optional.ofNullable(props.getValue(SettingKeys.REFRESH_TOKEN)).orElseThrow(CommunicatorNotInitializedException::new);
        String expiresString = Optional.ofNullable(props.getValue(SettingKeys.EXPIRES)).orElseThrow(CommunicatorNotInitializedException::new);
        LocalDateTime expires = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(expiresString)), ZoneId.of("UTC"));

        if (LocalDateTime.now().plus(1, ChronoUnit.DAYS).isAfter(expires)) {
            Token token = refreshToken(refreshToken);
            props.setValue(SettingKeys.ACCESS_TOKEN, token.getAccess_token());
            props.setValue(SettingKeys.REFRESH_TOKEN, token.getRefresh_token());
            props.setValue(SettingKeys.EXPIRES, String.valueOf(token.getExpires().toInstant(ZoneOffset.UTC).toEpochMilli()));
        }
    }

    @Override
    public PinContext pinCreate() {
        Invocation.Builder invBuilder = client.target(protocolAddress)
                .path("/api/pin/create")
                .queryParam("api-version", "2.1").request();
        Invocation in = invBuilder
                .header(HttpHeaders.CONTENT_LENGTH, 0)
                .accept(MediaType.APPLICATION_JSON)
                .buildPost(Entity.text(""));
        Response response = in.invoke();
        if (response.getStatus() - 200 < 100) {
            return response.readEntity(PinContext.class);
        } else {
            try {
                System.out.println(response.readEntity(net.kraschitzer.intellij.plugin.sevenpace.model.api.response.Response.class));
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
        System.exit(1);
        return null;
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
                System.out.println(response.readEntity(net.kraschitzer.intellij.plugin.sevenpace.model.api.response.Response.class));
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
        form.put("refresh_token", Collections.singletonList(props.getValue(SettingKeys.REFRESH_TOKEN)));

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

    private <T> T apiGet(String url, Class<T> responseType) throws CommunicationErrorException, CommunicationErrorParseException, CommunicationException, CommunicationHostUnknownException {
        return request(HTTPMethod.GET, "/api" + url, null, responseType, true);
    }

    private <T> T apiPostJson(String url, Object body, Class<T> responseType) throws CommunicationErrorException, CommunicationErrorParseException, CommunicationException, CommunicationHostUnknownException {
        return request(HTTPMethod.POST, "/api" + url, Entity.json(body), responseType, true);
    }

    private <T> T apiPostToken(String url, MultivaluedMap<String, String> form, Class<T> responseType) throws CommunicationErrorException, CommunicationErrorParseException, CommunicationException, CommunicationHostUnknownException {
        return request(HTTPMethod.POST, url, Entity.form(form), responseType, false);
    }

    private <T> T request(HTTPMethod method, String url, Entity entity, Class<T> responseType, boolean authenticated) throws CommunicationErrorException, CommunicationErrorParseException, CommunicationException, CommunicationHostUnknownException {
        Invocation.Builder ib = client.target(protocolAddress + url + "?api-version=2.1")
                .request(MediaType.APPLICATION_JSON_TYPE);
        if (authenticated) {
            ib.header(HttpHeaders.AUTHORIZATION, "Bearer " + props.getValue(SettingKeys.ACCESS_TOKEN));
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
                throw new CommunicationException("Given Method " + method + " not supported");
        }
        Response response = in.invoke();
        if (response != null && response.getStatus() - 200 < 100) {
            return response.readEntity(responseType);
        } else {
            try {
                throw new CommunicationErrorException(response.readEntity(Error.class));
            } catch (Exception e) {
                throw new CommunicationErrorParseException("Failed to parse error communication error", e);
            }
        }
    }

    private void checkInitialization() throws CommunicatorException {
        if (!initialized || !authenticated) {
            initialize();
        }
    }

}
