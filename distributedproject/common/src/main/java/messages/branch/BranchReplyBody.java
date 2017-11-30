package messages.branch;

import com.message.MessageBody;

import java.util.List;

public class BranchReplyBody implements MessageBody {

    private String reply;
    private List<String> replyList;

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<String> getReplyList() {
        return replyList;
    }

    public void setReplyList(List<String> replyList) {
        this.replyList = replyList;
    }
}