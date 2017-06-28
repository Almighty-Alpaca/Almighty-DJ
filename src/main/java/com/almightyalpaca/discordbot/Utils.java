package com.almightyalpaca.discordbot;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.common.base.Preconditions;

public class Utils
{
    private static CloseableHttpClient client = HttpClientBuilder.create().disableRedirectHandling().build();

    public static String expand(String urlArg) throws IOException
    {
        String originalUrl = urlArg;
        String newUrl = expandSingleLevel(originalUrl);
        while (!originalUrl.equals(newUrl))
        {
            originalUrl = newUrl;
            newUrl = expandSingleLevel(originalUrl);
        }
        return newUrl;
    }

    public static String expandSingleLevel(String url) throws IOException
    {
        HttpHead request = null;
        try
        {
            request = new HttpHead(url);
            HttpResponse httpResponse = client.execute(request);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != 301 && statusCode != 302)
            {
                return url;
            }
            Header[] headers = httpResponse.getHeaders(HttpHeaders.LOCATION);
            Preconditions.checkState(headers.length == 1);
            String newUrl = headers[0].getValue();
            return newUrl;
        }
        catch (IllegalArgumentException uriEx)
        {
            return url;
        }
        finally
        {
            if (request != null)
            {
                request.releaseConnection();
            }
        }
    }
}
