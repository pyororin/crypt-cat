package pyororin.cryptcat.repository;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsCloseResponse;
import com.slack.api.methods.response.conversations.ConversationsOpenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pyororin.cryptcat.config.SlackConfig;

import java.util.Collections;

@Repository
@RequiredArgsConstructor
public class SlackRepository {
    private final SlackConfig config;

    public void sendDirectMessage(String text) {
        try (Slack slack = Slack.getInstance()) {
            MethodsClient client = slack.methods(config.getToken());
            ConversationsOpenResponse openResponse =
                    client.conversationsOpen(req -> req.users(Collections.singletonList(config.getMentionTarget())));
            if (!openResponse.isOk()) {
                throw new Exception("DMを開くことができませんでした");
            }

            String message = String.format(
                    """
                                    [
                                        {
                                            "type": "section",
                                            "text": {
                                                "type": "mrkdwn",
                                                "text": "%s"
                                            }
                                        }
                                    ]
                            """, text);


            ChatPostMessageResponse messageResponse = client.chatPostMessage(req ->
                    req.channel(openResponse.getChannel().getId())
                            .blocksAsString(message)
            );
            if (!messageResponse.isOk()) {
                throw new Exception("DMを送ることができませんでした." + messageResponse.getError());
            }

            ConversationsCloseResponse closeResponse = client.conversationsClose(req ->
                    req.channel(openResponse.getChannel().getId()));
            if (!closeResponse.isOk()) {
                throw new Exception("DMを閉じることができませんでした");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
