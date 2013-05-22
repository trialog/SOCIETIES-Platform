package org.societies.android.platform.useragent.feedback;

import android.util.Log;
import org.jivesoftware.smack.packet.IQ;
import org.societies.android.api.comms.xmpp.*;
import org.societies.android.platform.comms.helper.ClientCommunicationMgr;
import org.societies.android.platform.useragent.feedback.model.NotificationHistoryItem;
import org.societies.api.identity.INetworkNode;
import org.societies.api.identity.InvalidFormatException;
import org.societies.api.schema.useragent.feedback.HistoryRequestType;
import org.societies.api.schema.useragent.feedback.UserFeedbackBean;
import org.societies.api.schema.useragent.feedback.UserFeedbackHistoryRequest;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NotificationHistoryRepository implements INotificationHistoryRepository {
    private static class RequestCallback implements ICommCallback, Future<List<NotificationHistoryItem>> {
        private static final String LOG_TAG = RequestCallback.class.getCanonicalName();

        private List<NotificationHistoryItem> result;
        private boolean complete = false;

        @Override
        public List<String> getXMLNamespaces() {
            return NAMESPACES;
        }

        @Override
        public List<String> getJavaPackages() {
            return PACKAGES;
        }

        @Override
        public void receiveResult(Stanza stanza, Object payload) {
            Log.d(LOG_TAG, String.format("receiveResult() \nStanza=%s\nPayload=%s",
                    stanza != null ? stanza.toString() : "null",
                    payload != null ? payload.toString() : "null"));

            if (payload == null) {
                Log.w(LOG_TAG, "Received null payload in receiveResult()");
                return;
            }

            UserFeedbackHistoryRequest request = (UserFeedbackHistoryRequest) payload;

            List<NotificationHistoryItem> historyItems = new ArrayList<NotificationHistoryItem>();

            // wrap the beans in NotificationHistoryItem objects
            for (UserFeedbackBean bean : request.getResponse()) {
                NotificationHistoryItem item = new NotificationHistoryItem(bean.getRequestId(),
                        new Date(), // TODO: store this in the bean
                        bean);
                historyItems.add(item);
            }

            Log.i(LOG_TAG, "Received a response containing " + historyItems.size() + " NHIs");

            result = historyItems;
            complete = true;
            this.notifyAll();
        }

        @Override
        public void receiveError(Stanza stanza, XMPPError error) {
            Log.d(LOG_TAG, String.format("receiveError() \nStanza=%s\nerror=%s",
                    stanza != null ? stanza.toString() : "null",
                    error != null ? error.toString() : "null"));

        }

        @Override
        public void receiveInfo(Stanza stanza, String node, XMPPInfo info) {
            Log.d(LOG_TAG, String.format("receiveInfo() \nStanza=%s\nNode=%s\nInfo=%s",
                    stanza != null ? stanza.toString() : "null",
                    node != null ? node : "null",
                    info != null ? info.toString() : "null"));

        }

        @Override
        public void receiveItems(Stanza stanza, String node, List<String> items) {
            Log.d(LOG_TAG, String.format("receiveInfo() \nStanza=%s\nNode=%s\nitems=%s",
                    stanza != null ? stanza.toString() : "null",
                    node != null ? node : "null",
                    items != null ? Arrays.toString(items.toArray()) : "null"));

        }

        @Override
        public void receiveMessage(Stanza stanza, Object payload) {
            Log.d(LOG_TAG, String.format("receiveMessage() \nStanza=%s\nPayload=%s",
                    stanza != null ? stanza.toString() : "null",
                    payload != null ? payload.toString() : "null"));


        }

        @Override
        public boolean cancel(boolean b) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return complete;
        }

        @Override
        public List<NotificationHistoryItem> get() throws InterruptedException, ExecutionException {
            return result;
        }

        @Override
        public List<NotificationHistoryItem> get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            return result;
        }
    }

    private static final String LOG_TAG = NotificationHistoryRepository.class.getCanonicalName();

    public static final List<String> ELEMENT_NAMES = Collections.unmodifiableList(
            Arrays.asList("userFeedbackHistoryRequest", "requestCallback"));
    public static final List<String> NAMESPACES = Collections.unmodifiableList(
            Arrays.asList("http://societies.org/api/schema/useragent/monitoring",
                    "http://societies.org/api/schema/useragent/feedback"));
    public static final List<String> PACKAGES = Collections.unmodifiableList(
            Arrays.asList("org.societies.api.schema.useragent.monitoring",
                    "org.societies.api.schema.useragent.feedback"));


    private final ClientCommunicationMgr commsManager;

    public NotificationHistoryRepository(ClientCommunicationMgr commsManager) {
        this.commsManager = commsManager;
    }


    @Override
    public Future<List<NotificationHistoryItem>> listPrevious(int howMany) {
        try {
            if (commsManager == null) {
                Log.e(LOG_TAG, "commsManager was null when trying to list previous");
                return null;
            }
            if (commsManager.getIdManager() == null) {
                Log.e(LOG_TAG, "commsManager.getIdManager() was null when trying to list previous");
                return null;
            }

            Log.d(LOG_TAG, "listPrevious(int)");

            INetworkNode cloudNode = commsManager.getIdManager().getCloudNode();

            RequestCallback requestCallback = new RequestCallback();


            UserFeedbackHistoryRequest bean = new UserFeedbackHistoryRequest();
            bean.setRequestType(HistoryRequestType.BY_COUNT);
            bean.setHowMany(howMany);

            Stanza stanza = new Stanza(cloudNode);

            Log.d(LOG_TAG, "Sending IQ...");
            commsManager.sendIQ(stanza, IQ.Type.GET, bean, requestCallback);
            Log.d(LOG_TAG, "IQ sent");

            return requestCallback;

        } catch (InvalidFormatException e) {
            Log.e(LOG_TAG, "Error listing previous notification history items", e);
        } catch (CommunicationException e) {
            Log.e(LOG_TAG, "Error listing previous notification history items", e);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error listing previous notification history items", e);
        }

        return null;
    }

    @Override
    public Future<List<NotificationHistoryItem>> listSince(Date sinceWhen) {
        return null;
    }
}
