package com.almightyalpaca.discordbot.config;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Config
{

    protected final JsonObject config;
    private final Config parent;
    protected boolean autoSave;

    protected Config(final Config parent, final JsonObject config) throws WrongTypeException, KeyNotFoundException
    {
        this.parent = parent;
        this.config = config;
    }

    public void clear()
    {
        this.config.entrySet().clear();
    }

    public Object get(final String key) throws KeyNotFoundException, WrongTypeException
    {
        return this.getJsonElement(key);
    }

    public BigDecimal getBigDecimal(final String key) throws WrongTypeException, KeyNotFoundException
    {
        try
        {
            return this.getJsonObject(key).getAsBigDecimal();
        } catch (final Exception e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        }
    }

    public BigInteger getBigInteger(final String key) throws WrongTypeException, KeyNotFoundException
    {
        try
        {
            return this.getJsonObject(key).getAsBigInteger();
        } catch (final Exception e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean getBoolean(final String key) throws WrongTypeException, KeyNotFoundException
    {
        try
        {
            return this.getJsonPrimitive(key).getAsBoolean();
        } catch (final Exception e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        }
    }

    public byte getByte(final String key) throws WrongTypeException, KeyNotFoundException
    {
        try
        {
            return this.getJsonPrimitive(key).getAsByte();
        } catch (final Exception e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        }
    }

    public char getCharacter(final String key) throws WrongTypeException, KeyNotFoundException
    {
        try
        {
            return this.getJsonPrimitive(key).getAsCharacter();
        } catch (final Exception e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        }
    }

    public Config getChildConfig(final String key) throws WrongTypeException, KeyNotFoundException
    {
        return new Config(this, this.getJsonObject(key));
    }

    public double getDouble(final String key) throws WrongTypeException, KeyNotFoundException
    {
        try
        {
            return this.getJsonPrimitive(key).getAsDouble();
        } catch (final Exception e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        }
    }

    public Map<String, Object> getEntries()
    {
        final Map<String, Object> map = new HashMap<>();
        for (final Entry<String, JsonElement> entry : this.config.getAsJsonObject().entrySet())
        {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public float getFloat(final String key) throws WrongTypeException, KeyNotFoundException
    {
        try
        {
            return this.getJsonPrimitive(key).getAsFloat();
        } catch (final Exception e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        }
    }

    public int getInt(final String key) throws WrongTypeException, KeyNotFoundException
    {
        try
        {
            return this.getJsonPrimitive(key).getAsInt();
        } catch (final Exception e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        }
    }

    public JsonArray getJsonArray(final String key) throws WrongTypeException, KeyNotFoundException
    {
        try
        {
            return this.getJsonElement(key).getAsJsonArray();
        } catch (final IllegalStateException e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        }
    }

    private JsonElement getJsonElement(final String key) throws KeyNotFoundException, WrongTypeException
    {
        final String[] path = key.split("\\.");
        JsonElement value = this.config;
        try
        {
            for (String element : path)
            {
                if (element.endsWith("]") && element.contains("["))
                {
                    final int i = element.lastIndexOf("[");
                    int index;
                    try
                    {
                        index = Integer.parseInt(element.substring(i).replace("[", "").replace("]", ""));
                    } catch (final Exception e)
                    {
                        index = 0;
                    }
                    element = element.substring(0, i);

                    value = value.getAsJsonObject().get(element);
                    value = value.getAsJsonArray().get(index);

                } else
                {
                    value = value.getAsJsonObject().get(element);
                }
            }
            return value;
        } catch (final IllegalStateException e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        } catch (final NullPointerException e)
        {
            final KeyNotFoundException exception = new KeyNotFoundException();
            exception.initCause(e);
            throw exception;
        }
    }

    public JsonObject getJsonObject(final String key) throws WrongTypeException, KeyNotFoundException
    {
        try
        {
            return this.getJsonElement(key).getAsJsonObject();
        } catch (final IllegalStateException e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        }
    }

    private JsonPrimitive getJsonPrimitive(final String key) throws KeyNotFoundException, WrongTypeException
    {
        try
        {
            return this.getJsonElement(key).getAsJsonPrimitive();
        } catch (final IllegalStateException e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        }
    }

    public long getLong(final String key) throws WrongTypeException, KeyNotFoundException
    {
        try
        {
            return this.getJsonPrimitive(key).getAsLong();
        } catch (final Exception e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        }
    }

    public Number getNumber(final String key) throws WrongTypeException, KeyNotFoundException
    {
        try
        {
            return this.getJsonPrimitive(key).getAsNumber();
        } catch (final Exception e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        }
    }

    public ConfigOrigin getOrigin()
    {
        return this.parent.getOrigin();
    }

    public short getShort(final String key) throws WrongTypeException, KeyNotFoundException
    {
        try
        {
            return this.getJsonPrimitive(key).getAsShort();
        } catch (final Exception e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        }
    }

    public String getString(final String key) throws WrongTypeException, KeyNotFoundException
    {
        try
        {
            return this.getJsonPrimitive(key).getAsString();
        } catch (final Exception e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean hasKey(final String key)
    {
        try
        {
            this.getJsonElement(key);
        } catch (final Exception e)
        {
            return false;
        }
        return true;
    }

    public boolean isArray(final String key)
    {
        return this.isJsonArray(key);
    }

    public boolean isConfig(final String key)
    {
        return this.isJsonObject(key);
    }

    public boolean isEmpty()
    {
        return this.config.entrySet().isEmpty();
    }

    protected boolean isJsonArray(final String key)
    {
        return this.config.isJsonArray();
    }

    protected boolean isJsonNull(final String key)
    {
        return this.config.isJsonNull();
    }

    protected boolean isJsonObject(final String key)
    {
        return this.config.isJsonObject();
    }

    protected boolean isJsonPrimitive(final String key)
    {
        return this.config.isJsonPrimitive();
    }

    public boolean isNull(final String key)
    {
        return this.isJsonNull(key);
    }

    public boolean isNumber(final String key)
    {
        if (this.isJsonPrimitive(key))
        {
            return this.getJsonPrimitive(key).isNumber();
        } else
        {
            return false;
        }

    }

    public boolean isString(final String key)
    {
        if (this.isJsonPrimitive(key))
        {
            return this.getJsonPrimitive(key).isString();
        } else
        {
            return false;
        }
    }

    public void put(final String key, final boolean value)
    {
        this.put(key, new JsonPrimitive(value));
    }

    public void put(final String key, final Character value)
    {
        this.put(key, new JsonPrimitive(value));
    }

    public Config put(final String key, final Config value)
    {
        this.put(key, value.config);
        return this.getChildConfig(key);
    }

    public void put(String key, final JsonElement value) throws WrongTypeException
    {
        final String finalKey = key.substring(key.lastIndexOf(".") + 1);
        key = key.substring(0, key.lastIndexOf("."));
        final String[] path = key.split("\\.");
        JsonObject current = this.config;

        try
        {
            for (String element : path)
            {
                if (element.endsWith("]") && element.contains("["))
                {
                    final int i = element.lastIndexOf("[");
                    int index;
                    try
                    {
                        index = Integer.parseInt(element.substring(i).replace("[", "").replace("]", ""));
                    } catch (final Exception e)
                    {
                        index = -1;
                    }
                    element = element.substring(0, i);

                    if (!current.has(element))
                    {
                        current.add(element, new JsonArray());
                    }
                    final JsonArray array = current.get(element).getAsJsonArray();
                    if (index == -1)
                    {
                        final JsonObject object = new JsonObject();
                        array.add(object);
                        current = object;
                    } else
                    {
                        if (index == array.size())
                        {
                            array.add(new JsonObject());
                        }
                        current = array.get(index).getAsJsonObject();
                    }

                } else
                {
                    if (!current.has(element))
                    {
                        current.add(element, new JsonObject());
                    }
                    current = current.get(element).getAsJsonObject();
                }
            }
            current.add(finalKey, value);

        } catch (final IllegalStateException e)
        {
            final WrongTypeException exception = new WrongTypeException();
            exception.initCause(e);
            throw exception;
        } catch (final IndexOutOfBoundsException e)
        {
            throw e;
        }

    }

    public void put(final String key, final Number value)
    {
        this.put(key, new JsonPrimitive(value));
    }

    public void put(final String key, final String value)
    {
        this.put(key, new JsonPrimitive(value));
    }

    public void remove(final String key)
    {
        this.config.remove(key);
    }

    public boolean save()
    {
        return this.parent.save();
    }

    public Config setAutoSave(final boolean autoSave)
    {
        this.autoSave = autoSave;
        return this;
    }

    @Override
    public String toString()
    {
        return this.config.toString();
    }

    // public Config getOrCreateChildConfig(String key) {
    // if (!hasKey(key)) {
    // put(key, new JsonObject());
    // }
    // return getChildConfig(key);
    // }

}
