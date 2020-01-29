package net.kraschitzer.intellij.plugin.sevenpace.communication;

import net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions.CommunicatorNotInitializedException;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.request.SelectWorkItemRequest;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.request.StartTrackingRequest;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.request.UpdateTrackRequest;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.*;

public interface ICommunicator {

    PinContext pinCreate();

    PinStatus pinStatus(String secret);

    Token token(String secret);

    Token refreshToken(String refreshToken);

    TrackingStateModel getCurrentState(boolean expand) throws CommunicatorNotInitializedException;

    Track selectWorkItem(SelectWorkItemRequest selectWorkItemRequest);

    LatestWorkLogsModel getLatestWorkLogs(int count);

    TrackingStateModel startTracking(StartTrackingRequest startTrackingRequest);

    TrackingStateModel stopTracking(int reason);

    TrackingStateModel notifyNextIdleCheck(int selectedOption);

    TrackingStateModel notifyOfActivity();

    TrackingStateModel updateTrack(UpdateTrackRequest updateTrackRequest);

    SearchResultModel searchWorkItem(String workItemIdOrTitle);

    SearchResultModel searchWokrItemByModel(String workItemIdOrTitle);

}
