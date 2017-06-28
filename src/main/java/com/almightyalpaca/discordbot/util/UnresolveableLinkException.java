package com.almightyalpaca.discordbot.util;

public class UnresolveableLinkException extends Exception
{

    private static final long serialVersionUID = -7829415354037796584L;

    public UnresolveableLinkException()
    {
        super();
    }

    public UnresolveableLinkException(String s)
    {
        super(s);
    }

    public UnresolveableLinkException(Throwable t)
    {
        super(t);
    }

    public UnresolveableLinkException(String s, Throwable t)
    {
        super(s, t);
    }

}
