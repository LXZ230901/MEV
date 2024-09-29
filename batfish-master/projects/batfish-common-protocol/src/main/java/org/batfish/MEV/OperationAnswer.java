package org.batfish.MEV;

public class OperationAnswer {
    private boolean _filter;
    private Message.Builder _message;
    public OperationAnswer() {}

    public OperationAnswer(boolean filter, Message.Builder message)
    {
        this._filter = filter;
        this._message = message;
    }

    public void setFilter(boolean filter)
    {
        this._filter = filter;
    }

    public void setMessage(Message.Builder message)
    {
        this._message = message;
    }

    public boolean getFilter()
    {
        return this._filter;
    }

    public Message.Builder getMessage()
    {
        return this._message;
    }
}
