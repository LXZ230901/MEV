package org.batfish.MEV;




public class IsolationOperationAnswer {
    private boolean _filter;
    private IsoaltionMessage.Builder _message;
    public IsolationOperationAnswer() {}

    public IsolationOperationAnswer(boolean filter, IsoaltionMessage.Builder message)
    {
        this._filter = filter;
        this._message = message;
    }

    public void setFilter(boolean filter)
    {
        this._filter = filter;
    }

    public void setMessage(IsoaltionMessage.Builder message)
    {
        this._message = message;
    }

    public boolean getFilter()
    {
        return this._filter;
    }

    public IsoaltionMessage.Builder getMessage()
    {
        return this._message;
    }
}
