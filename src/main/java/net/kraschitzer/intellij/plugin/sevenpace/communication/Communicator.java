package net.kraschitzer.intellij.plugin.sevenpace.communication;

import com.intellij.ide.util.PropertiesComponent;
import net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions.CommunicatorNotInitializedException;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.request.SelectWorkItemRequest;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.request.StartTrackingRequest;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.request.UpdateTrackRequest;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.*;
import net.kraschitzer.intellij.plugin.sevenpace.utils.SettingKeys;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.*;
import java.util.Collections;

public class Communicator implements ICommunicator {

    private static Communicator communicator;

    private PropertiesComponent props;
    private String apiAddress;
    private Client client;

    private boolean initialized;
    private boolean authenticated;

    private Communicator() {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    public static Communicator getInstance() {
        if (communicator == null) {
            communicator = new Communicator();
        }
        if (!communicator.initialized) {
            communicator.initialize();
        }
        return communicator;
    }

    public void initialize() {
        this.apiAddress = PropertiesComponent.getInstance().getValue(SettingKeys.URL);
        if (StringUtils.isNotBlank(apiAddress)) {
            this.client = ClientBuilder.newClient();
            this.initialized = true;
        }
    }

    @Override
    public PinContext pinCreate() {
        Invocation.Builder invBuilder = client.target(apiAddress)
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
        Invocation.Builder invBuilder = client.target(apiAddress)
                .path("/api/pin/status")
                .queryParam("api-version", "2.1").request();
        Invocation in = invBuilder
                .accept(MediaType.APPLICATION_JSON)
//                .header("Content-Type", MediaType.APPLICATION_JSON)
//                .header("Content-Length", secret.length() + 2)
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
    public Token token(String secret) {
        MultivaluedMap<String, String> form = new MultivaluedHashMap<>();
        form.put("client_id", Collections.singletonList("OpenApi"));
        form.put("grant_type", Collections.singletonList("authorization_code"));
        form.put("code", Collections.singletonList(secret));

        WebTarget webTarget = client.target(apiAddress + "/token?api-version=2.1");
        Invocation in = webTarget.request(MediaType.APPLICATION_JSON_TYPE).buildPost(Entity.form(form));
        Response response = in.invoke();
        if (response.getStatus() - 200 < 100) {
            return response.readEntity(Token.class);
        } else {
            try {
                System.out.println(response.readEntity(net.kraschitzer.intellij.plugin.sevenpace.model.api.response.Response.class));
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
        return null;
    }

    @Override
    public Token refreshToken(String refreshToken) {
        MultivaluedMap<String, String> form = new MultivaluedHashMap<>();
        form.put("client_id", Collections.singletonList("OpenApi"));
        form.put("grant_type", Collections.singletonList("refresh_token"));
        form.put("refresh_token", Collections.singletonList(props.getValue(SettingKeys.REFRESH_TOKEN)));

        WebTarget webTarget = client.target(apiAddress + "/token?api-version=2.1");
        Invocation in = webTarget.request(MediaType.APPLICATION_JSON_TYPE).buildPost(Entity.form(form));
        Response response = in.invoke();
        if (response.getStatus() - 200 < 100) {
            return response.readEntity(Token.class);
        } else {
            try {
                System.out.println(response.readEntity(net.kraschitzer.intellij.plugin.sevenpace.model.api.response.Response.class));
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
        return null;
    }

    @Override
    public TrackingStateModel getCurrentState(boolean expand) throws CommunicatorNotInitializedException {
        if (!initialized || !authenticated) {
            throw new CommunicatorNotInitializedException();
        }
        Invocation.Builder ib = client.target(apiAddress + "/api/tracking/client/current/" + expand + "?api-version=2.1")
                .request(MediaType.APPLICATION_JSON_TYPE);
        ib.header(HttpHeaders.AUTHORIZATION, "Bearer " + props.getValue(SettingKeys.ACCESS_TOKEN));
        Invocation in = ib.buildGet();
        Response response = in.invoke();
        if (response.getStatus() - 200 < 100) {
            return response.readEntity(TrackingStateModel.class);
        } else {
            try {
                System.out.println(response.readEntity(net.kraschitzer.intellij.plugin.sevenpace.model.api.response.Response.class));
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
        return null;
    }

    @Override
    public Track selectWorkItem(SelectWorkItemRequest selectWorkItemRequest) {
        return null;
    }

    @Override
    public LatestWorkLogsModel getLatestWorkLogs(int count) {
        return null;
    }

    @Override
    public TrackingStateModel startTracking(StartTrackingRequest startTrackingRequest) {
        return null;
    }

    @Override
    public TrackingStateModel stopTracking(int reason) {
        return null;
    }

    @Override
    public TrackingStateModel notifyNextIdleCheck(int selectedOption) {
        return null;
    }

    @Override
    public TrackingStateModel notifyOfActivity() {
        return null;
    }

    @Override
    public TrackingStateModel updateTrack(UpdateTrackRequest updateTrackRequest) {
        return null;
    }

    @Override
    public SearchResultModel searchWorkItem(String workItemIdOrTitle) {
        return null;
    }

    @Override
    public SearchResultModel searchWokrItemByModel(String workItemIdOrTitle) {
        return null;
    }
}
