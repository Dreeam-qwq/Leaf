package org.leavesmc.leaves.protocol.chatimage;

public class ChatImageIndex {

    public int index;
    public int total;
    public String url;
    public String bytes;

    public ChatImageIndex(int index, int total, String url, String bytes) {
        this.index = index;
        this.total = total;
        this.url = url;
        this.bytes = bytes;
    }
}
