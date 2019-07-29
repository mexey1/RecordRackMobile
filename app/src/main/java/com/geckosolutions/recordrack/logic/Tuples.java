package com.geckosolutions.recordrack.logic;

/**
 * Created by anthony1 on 5/18/16.
 */
public class Tuples<T,U>
{
    public T t;
    public U u;

    public void setValues(T t, U u)
    {
        this.t = t;
        this.u = u;
    }

    public T getFirst()
    {
        return t;
    }

    public U getSecond()
    {
        return u;
    }
}
