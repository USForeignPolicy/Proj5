

import java.io.Serializable;

/**
 *
 * [Add your documentation here]
 *
 * @author your name and section
 * @version date
 */
final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.
    private String message;
    private int type;


    //TODO THIS IS A TEST METHOD TO CREATE BLANK OBJECTS with new ChatMessage()
    public ChatMessage()    {
        this.message = "";
        this.type = 0;
    }
    public ChatMessage(String message, int type)    {
        this.message = message;
        this.type = type;
    }

    public String getMessage()    {
        return message;
    }

    public int getType()    {
        return type;
    }

}
