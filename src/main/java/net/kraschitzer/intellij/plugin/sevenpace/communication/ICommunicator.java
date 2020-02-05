package net.kraschitzer.intellij.plugin.sevenpace.communication;

import com.intellij.openapi.components.ServiceManager;
import net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions.CommunicatorException;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.request.SelectWorkItemRequest;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.request.StartTrackingRequest;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.request.UpdateTrackRequest;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.*;
import net.kraschitzer.intellij.plugin.sevenpace.model.enums.Reason;

public interface ICommunicator {

    static ICommunicator getInstance() {
        return ServiceManager.getService(ICommunicator.class);
    }

    void initialize() throws CommunicatorException;

    void authenticate() throws CommunicatorException;

    PinContext pinCreate();

    PinStatus pinStatus(String secret);

    Token token(String secret) throws CommunicatorException;

    Token refreshToken(String refreshToken) throws CommunicatorException;

    TrackingStateModel getCurrentState(boolean expand) throws CommunicatorException;

    Track selectWorkItem(SelectWorkItemRequest selectWorkItemRequest) throws CommunicatorException;

    LatestWorkLogsModel getLatestWorkLogs(int count) throws CommunicatorException;

    TrackingStateModel startTracking(StartTrackingRequest startTrackingRequest) throws CommunicatorException;

    TrackingStateModel stopTracking(Reason reason) throws CommunicatorException;

    TrackingStateModel notifyNextIdleCheck(int selectedOption) throws CommunicatorException;

    TrackingStateModel notifyOfActivity() throws CommunicatorException;

    TrackingStateModel updateTrack(UpdateTrackRequest updateTrackRequest) throws CommunicatorException;

    SearchResultModel searchWorkItem(String workItemIdOrTitle) throws CommunicatorException;

    SearchResultModel searchWorkItemByModel(String workItemIdOrTitle) throws CommunicatorException;

    void resetInitialization();
}
