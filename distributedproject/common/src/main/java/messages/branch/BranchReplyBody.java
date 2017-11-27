package messages.branch;

import com.message.MessageBody;

public class BranchReplyBody implements MessageBody{

    private String reply;

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}